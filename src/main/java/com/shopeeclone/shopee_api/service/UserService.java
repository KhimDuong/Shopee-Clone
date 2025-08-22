package com.shopeeclone.shopee_api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shopeeclone.shopee_api.model.User;
import com.shopeeclone.shopee_api.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User upsertFromOAuth(String email, String name) {
    return userRepository.findByEmail(email)
        .map(u -> {
            boolean dirty = false;

            // ensure Google users do NOT keep any password
            if (u.getPassword() != null) {
                u.setPassword(null);
                dirty = true;
            }

            // ensure username exists
            if (u.getUsername() == null || u.getUsername().isBlank()) {
                u.setUsername(email); // or derive from email local-part
                dirty = true;
            }


            return dirty ? userRepository.save(u) : u;
        })
        .orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setUsername(email);
            u.setPassword(null);

            return userRepository.save(u);
        });
    }
}
