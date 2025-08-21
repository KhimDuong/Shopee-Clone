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
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setUsername(email); // you can also use name if you prefer
            u.setEmail(email);
            // generate a random password so it's not null
            u.setPassword(java.util.UUID.randomUUID().toString());
            // if your User entity has roles, set a default one here
            return userRepository.save(u);
        });
    }
}
