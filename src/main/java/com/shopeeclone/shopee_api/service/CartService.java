// src/main/java/com/shopeeclone/shopee_api/service/CartService.java
package com.shopeeclone.shopee_api.service;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopeeclone.shopee_api.dto.CartItemDto;
import com.shopeeclone.shopee_api.model.Cart;
import com.shopeeclone.shopee_api.model.Product;
import com.shopeeclone.shopee_api.model.User;
import com.shopeeclone.shopee_api.repository.CartRepository;
import com.shopeeclone.shopee_api.repository.ProductRepository;
import com.shopeeclone.shopee_api.repository.UserRepository;

@Service
@Transactional // ✅ all methods are in a transaction by default
public class CartService {

  private final CartRepository cartRepo;
  private final UserRepository userRepo;
  private final ProductRepository productRepo;

  public CartService(CartRepository cartRepo, UserRepository userRepo, ProductRepository productRepo) {
    this.cartRepo = cartRepo;
    this.userRepo = userRepo;
    this.productRepo = productRepo;
  }

  private Long currentUserId(Principal principal) {
    String username = principal.getName();
    User u = userRepo.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found: " + username));
    return u.getId();
  }

  @Transactional(readOnly = true) // ✅ read-only for queries
  public List<CartItemDto> list(Principal principal) {
    final Long uid = currentUserId(principal);
    return cartRepo.findByUserId(uid).stream().map(c -> {
      Product p = productRepo.findById(c.getProductId())
          .orElseThrow(() -> new RuntimeException("Product not found: " + c.getProductId()));
      return new CartItemDto(p.getId(), p.getName(),
          // use whichever getter you have:
          p.getImage_url() != null ? p.getImage_url() : p.getImage_url(),
          p.getPrice(), c.getQuantity());
    }).collect(Collectors.toList());
  }

  public CartItemDto add(Principal principal, Long productId, Integer qty) {
    final int q = (qty == null || qty < 1) ? 1 : qty;
    final Long uid = currentUserId(principal);
    final Long pid = productId;

    Product p = productRepo.findById(pid)
        .orElseThrow(() -> new RuntimeException("Product not found: " + pid));

    Cart item = cartRepo.findByUserIdAndProductId(uid, pid)
        .map(ci -> { ci.setQuantity(ci.getQuantity() + q); return ci; })
        .orElseGet(() -> {
          Cart ci = new Cart();
          ci.setUserId(uid);
          ci.setProductId(pid);
          ci.setQuantity(q);
          return ci;
        });

    Cart saved = cartRepo.save(item);
    return new CartItemDto(p.getId(), p.getName(),
        p.getImage_url() != null ? p.getImage_url() : p.getImage_url(),
        p.getPrice(), saved.getQuantity());
  }

  public CartItemDto updateQty(Principal principal, Long productId, Integer qty) {
    final int q = (qty == null || qty < 1) ? 1 : qty;
    final Long uid = currentUserId(principal);
    final Long pid = productId;

    Cart item = cartRepo.findByUserIdAndProductId(uid, pid)
        .orElseThrow(() -> new RuntimeException("Not in cart"));

    item.setQuantity(q);
    cartRepo.save(item);

    Product p = productRepo.findById(pid)
        .orElseThrow(() -> new RuntimeException("Product not found: " + pid));

    return new CartItemDto(p.getId(), p.getName(),
        p.getImage_url() != null ? p.getImage_url() : p.getImage_url(),
        p.getPrice(), item.getQuantity());
  }

  public void remove(Principal principal, Long productId) {
    final Long uid = currentUserId(principal);
    cartRepo.deleteByUserIdAndProductId(uid, productId);
  }

  public void clear(Principal principal) {
    final Long uid = currentUserId(principal);
    cartRepo.deleteByUserId(uid); 
  }
}