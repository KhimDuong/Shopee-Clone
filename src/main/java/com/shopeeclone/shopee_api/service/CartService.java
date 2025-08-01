package com.shopeeclone.shopee_api.service;

import com.shopeeclone.shopee_api.model.Cart;
import com.shopeeclone.shopee_api.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    public List<Cart> getCartsByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public Cart addToCart(Cart cart) {
        return cartRepository.save(cart);
    }

    public void removeFromCart(Long id) {
        cartRepository.deleteById(id);
    }
}
