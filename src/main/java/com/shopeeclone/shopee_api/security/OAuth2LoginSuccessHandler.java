package com.shopeeclone.shopee_api.security;

import com.shopeeclone.shopee_api.model.User;
import com.shopeeclone.shopee_api.security.JwtUtil;
import com.shopeeclone.shopee_api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.auth.frontend-callback}")
    private String frontendCallback;

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

        // Upsert local user (create if not exists; assign default role, e.g., BUYER)
        User user = userService.upsertFromOAuth(email, name);

        // Issue your existing API JWT
        String jwt = jwtUtil.generateToken(user.getUsername());

        String redirect = frontendCallback + "#access_token=" +
                URLEncoder.encode(jwt, StandardCharsets.UTF_8);

        res.sendRedirect(redirect);
    }
}