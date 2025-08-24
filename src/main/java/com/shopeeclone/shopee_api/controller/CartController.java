package com.shopeeclone.shopee_api.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopeeclone.shopee_api.dto.AddItemRequest;
import com.shopeeclone.shopee_api.dto.CartItemDto;
import com.shopeeclone.shopee_api.dto.UpdateQtyRequest;
import com.shopeeclone.shopee_api.service.CartService;

@RestController
@RequestMapping("/cart")
public class CartController {
  private final CartService cartService;
  public CartController(CartService cartService) { this.cartService = cartService; }

  @GetMapping
  public List<CartItemDto> getCart(Principal principal) {
    return cartService.list(principal);
  }

  @PostMapping
  public CartItemDto add(@RequestBody AddItemRequest req, Principal principal) {
    return cartService.add(principal, req.getProductId(), req.getQuantity());
  }

  @PutMapping("/{productId}")
  public CartItemDto update(@PathVariable Long productId, @RequestBody UpdateQtyRequest req, Principal principal) {
    return cartService.updateQty(principal, productId, req.getQuantity());
  }

  @DeleteMapping("/{productId}")
  public ResponseEntity<Void> remove(@PathVariable Long productId, Principal principal) {
    cartService.remove(principal, productId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> clear(Principal principal) {
    cartService.clear(principal);
    return ResponseEntity.noContent().build();
  }
}