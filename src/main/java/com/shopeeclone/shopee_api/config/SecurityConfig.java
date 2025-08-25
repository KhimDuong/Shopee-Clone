package com.shopeeclone.shopee_api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.shopeeclone.shopee_api.security.JwtFilter;
import com.shopeeclone.shopee_api.security.OAuth2LoginSuccessHandler;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           OAuth2LoginSuccessHandler oauth2SuccessHandler,
                                           AuthenticationFailureHandler oauth2FailureHandler) throws Exception {
        http
            // OAuth2 handshake needs a short-lived session
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .csrf(AbstractHttpConfigurer::disable)
            .cors(c -> {}) // use beans below

            // IMPORTANT: Do NOT redirect APIs to Google when unauthenticated.
            // For any endpoint that still requires auth, return 401 instead of a redirect.
            .exceptionHandling(e -> e
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )

            .authorizeHttpRequests(auth -> auth
                // Preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Public routes
                .requestMatchers(
                    "/api/auth/**",
                    "/oauth2/**", "/login/**",
                    "/", "/actuator/health", "/public/**"
                ).permitAll()

                // PUBLIC product listing (support both paths, if any old code still hits /products)
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/products/**").permitAll()

                // everything else needs auth
                .anyRequest().authenticated()
            )

            .oauth2Login(oauth -> oauth
                .successHandler(oauth2SuccessHandler)
                .failureHandler(oauth2FailureHandler)
            )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationFailureHandler oauth2FailureHandler() {
        return (req, res, ex) -> {
            res.setStatus(401);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"oauth2_login_failed\"}");
        };
    }

    // CORS for credentials across 3000 ↔ 8080
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOriginPatterns(
                        "http://localhost:3000",
                        "http://127.0.0.1:3000",
                        "http://192.168.*.*:3000",
                        "https://shopee-web-clone-wine.vercel.app",
                        "https://*.postman.co",
                        "https://postman.com"
                    )
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }

    // Optional duplicate bean (same settings); harmless to keep:
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "http://192.168.*.*:3000",
            "https://shopee-web-clone-wine.vercel.app",
            "https://*.postman.co",
            "https://postman.com"
        ));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}