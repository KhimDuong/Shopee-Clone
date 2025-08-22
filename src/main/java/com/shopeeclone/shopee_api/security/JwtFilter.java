package com.shopeeclone.shopee_api.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "jwt";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1) Try Authorization: Bearer ...
        String token = resolveBearerToken(request);

        // 2) Fallback to HttpOnly cookie "jwt"
        if (token == null) {
            token = resolveTokenFromCookie(request, COOKIE_NAME);
        }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String username = extractUsernameSafe(token);

            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtUtil.isTokenValid(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private String resolveTokenFromCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    private String extractUsernameSafe(String token) {
        try {
            return jwtUtil.extractUsername(token);
        } catch (Exception e) {
            // invalid/expired token → ignore
            return null;
        }
    }
}