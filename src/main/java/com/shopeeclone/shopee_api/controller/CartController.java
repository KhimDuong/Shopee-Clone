package com.shopeeclone.shopee_api.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.*;

import com.shopeeclone.shopee_api.dto.AddItemRequest;
import com.shopeeclone.shopee_api.dto.CartItemDto;
import com.shopeeclone.shopee_api.dto.UpdateQtyRequest;
import com.shopeeclone.shopee_api.model.User;
import com.shopeeclone.shopee_api.service.CartService;
import com.shopeeclone.shopee_api.service.UserService;

@RestController
@RequestMapping("/api/cart")
public class CartController {

  private final CartService cartService;
  private final UserService userService;

  public CartController(CartService cartService, UserService userService) {
    this.cartService = cartService;
    this.userService = userService;
  }

  // --- helper ---------------------------------------------------------------

  /**
   * Ensure the Principal name we pass to CartService is the **app username**.
   * - For normal login: principal.getName() is already the username → return as-is.
   * - For Google OAuth2: principal is OAuth2AuthenticationToken (name = Google "sub"),
   *   so extract email, upsert local user, and return a Principal whose name = app username.
   */
  private Principal resolveAppPrincipal(Principal principal) {
    if (principal instanceof OAuth2AuthenticationToken oauth) {
      Object p = oauth.getPrincipal();

      String email = null;
      String name  = null;

      if (p instanceof OidcUser oidc) {
        email = oidc.getEmail();
        name  = oidc.getFullName() != null ? oidc.getFullName() : oidc.getGivenName();
      } else if (p instanceof DefaultOAuth2User ou) {
        Object e = ou.getAttributes().get("email");
        Object n = ou.getAttributes().get("name");
        if (e != null) email = e.toString();
        if (n != null) name  = n.toString();
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
  public List<CartItemDto> getCart(Principal principal) {
    Principal app = resolveAppPrincipal(principal);
    return cartService.list(app);
  }

  @PostMapping
  public CartItemDto add(@RequestBody AddItemRequest req, Principal principal) {
    Principal app = resolveAppPrincipal(principal);
    return cartService.add(app, req.getProductId(), req.getQuantity());
  }

  @PutMapping("/{productId}")
  public CartItemDto update(@PathVariable Long productId, @RequestBody UpdateQtyRequest req, Principal principal) {
    Principal app = resolveAppPrincipal(principal);
    return cartService.updateQty(app, productId, req.getQuantity());
  }

  @DeleteMapping("/{productId}")
  public ResponseEntity<Void> remove(@PathVariable Long productId, Principal principal) {
    Principal app = resolveAppPrincipal(principal);
    cartService.remove(app, productId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> clear(Principal principal) {
    Principal app = resolveAppPrincipal(principal);
    cartService.clear(app);
    return ResponseEntity.noContent().build();
  }
}