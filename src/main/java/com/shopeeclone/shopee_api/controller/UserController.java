package com.shopeeclone.shopee_api.controller;

import com.shopeeclone.shopee_api.model.User;
import com.shopeeclone.shopee_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {           // No admin yet.
        return userService.getAllUsers();       // No need for get all users function.
    }

    @PostMapping
    public User createUser(@RequestBody User user) {    // Already done in register in Auth controller
        return userService.createUser(user);            // No need for create user function. 
    }
}
