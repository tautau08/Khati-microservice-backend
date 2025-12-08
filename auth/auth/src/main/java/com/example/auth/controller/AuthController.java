package com.example.auth.controller;

import com.example.auth.dto.AuthResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.entity.User;
import com.example.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());
        User user = authService.registerUser(
                request.getUsername(),
                request.getPassword(),
                request.getEmail()
        );
        log.info("User registered successfully: {}", user.getUsername());
        return ResponseEntity.ok(new AuthResponse(
                null,
                user.getUsername(),
                "User registered successfully"
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());
        String token = authService.loginUser(
                request.getUsername(),
                request.getPassword()
        );
        log.info("Login successful for username: {}", request.getUsername());
        return ResponseEntity.ok(new AuthResponse(
                token,
                request.getUsername(),
                "Login successful"
        ));
    }

    @PutMapping("/update-role")
    public ResponseEntity<String> updateUserRole(@RequestParam Long userId, @RequestBody List<String> newRoles) {
        log.info("Updating roles for userId: {} to {}", userId, newRoles);
        authService.updateUserRoles(userId, newRoles);
        log.info("Roles updated successfully for userId: {}", userId);
        return ResponseEntity.ok("Roles updated successfully");
    }
}
