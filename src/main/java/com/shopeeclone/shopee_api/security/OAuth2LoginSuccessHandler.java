package com.shopeeclone.shopee_api.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.shopeeclone.shopee_api.model.User;
import com.shopeeclone.shopee_api.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.auth.frontend-callback}")
    private String frontendCallback; // e.g. http://localhost:3000/products

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public OAuth2LoginSuccessHandler(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
                                        Authentication authentication) throws IOException {
        Object principal = authentication.getPrincipal();

        String email = null;
        String name  = null;

        if (principal instanceof OidcUser oidc) { // Google OIDC
            email = oidc.getEmail();
            name  = oidc.getFullName() != null ? oidc.getFullName() : oidc.getGivenName();
        } else if (principal instanceof DefaultOAuth2User ou) { // fallback
            email = (String) ou.getAttributes().get("email");
            name  = (String) ou.getAttributes().getOrDefault("name", email);
        }

        if (email == null) {
            res.sendRedirect(frontendCallback + "#error=missing_email");
            return;
        }

        // Upsert local user
        User user = userService.upsertFromOAuth(email, name);

        // Issue API JWT (same token type as normal login)
        String jwt = jwtUtil.generateToken(user.getUsername());

        // (Optional) Also set HttpOnly cookie – harmless to keep for server-side checks
        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(false)          // true in prod (HTTPS)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax")        // dev: localhost:3000 ↔ 8080
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // IMPORTANT: include the token in the URL so SPA can store it in localStorage
        // Use fragment (#token=...) to avoid Referer leaking the JWT
        String redirect = frontendCallback
                + "#token=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8);
        res.sendRedirect(redirect);
    }
}