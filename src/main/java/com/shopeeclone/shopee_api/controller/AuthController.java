package com.shopeeclone.shopee_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopeeclone.shopee_api.dto.AuthResponse;
import com.shopeeclone.shopee_api.dto.LoginRequest;
import com.shopeeclone.shopee_api.dto.RegisterRequest;
import com.shopeeclone.shopee_api.model.User;
import com.shopeeclone.shopee_api.security.JwtUtil;
import com.shopeeclone.shopee_api.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ✅ Đăng ký
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Mã hoá mật khẩu

        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(savedUser);
    }
//    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
//        if (userService.existsByUsername(request.getUsername())) {
//            return ResponseEntity.badRequest().body("Username already exists");
//        }
//
//        User user = new User();
//        user.setUsername(request.getUsername());
//        user.setEmail(request.getEmail());
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//        userService.saveUser(user);
//
//        // Tự động login và trả về token
//        String token = jwtUtil.generateToken(user.getUsername());
//        return ResponseEntity.ok(new AuthResponse(token));
//    }

    // ✅ Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtUtil.generateToken(request.getUsername());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}