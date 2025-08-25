package com.shopeeclone.shopee_api.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopeeclone.shopee_api.model.Order;
import com.shopeeclone.shopee_api.model.User;
import com.shopeeclone.shopee_api.service.OrderService;
import com.shopeeclone.shopee_api.service.UserService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    // --- helper ---------------------------------------------------------------
    /**
     * Ensure the Principal name we pass to CartService is the **app username**.
     * - For normal login: principal.getName() is already the username → return
     * as-is. - For Google OAuth2: principal is OAuth2AuthenticationToken (name
     * = Google "sub"), so extract email, upsert local user, and return a
     * Principal whose name = app username.
     */
    private Principal resolveAppPrincipal(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken oauth) {
            Object p = oauth.getPrincipal();

            String email = null;
            String name = null;

            if (p instanceof OidcUser oidc) {
                email = oidc.getEmail();
                name = oidc.getFullName() != null ? oidc.getFullName() : oidc.getGivenName();
            } else if (p instanceof DefaultOAuth2User ou) {
                Object e = ou.getAttributes().get("email");
                Object n = ou.getAttributes().get("name");
                if (e != null) {
                    email = e.toString();
                }
                if (n != null) {
                    name = n.toString();
                }
            }

            if (email != null) {
                // Idempotent upsert – same as your OAuth success handler logic
                User user = userService.upsertFromOAuth(email, name);
                final String appUsername = user.getUsername();
                // Return a tiny Principal that reports our app username
                return () -> appUsername;
            }
        }
        // Normal login or no mapping needed
        return principal;
    }

    // --- endpoints ------------------------------------------------------------
    @GetMapping
    public List<Order> getOrders(Principal principal) throws Exception {
        Principal app = resolveAppPrincipal(principal);
        return orderService.getOrders(app);
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order, Principal principal) throws Exception {
        Principal app = resolveAppPrincipal(principal);
        return orderService.createOrder(app, order);
    }
}
