package com.shopeeclone.shopee_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopeeclone.shopee_api.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {
  List<Cart> findByUserId(Long userId);
  Optional<Cart> findByUserIdAndProductId(Long userId, Long productId);
  void deleteByUserIdAndProductId(Long userId, Long productId);
  void deleteByUserId(Long userId);
}
