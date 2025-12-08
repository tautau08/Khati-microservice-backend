package com.example.user.controller;

import com.example.user.dto.UpdateRolesRequest;
import com.example.user.dto.UserDTO;
import com.example.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Internal API endpoints for auth-service communication
 * Not exposed to external clients - protected at API Gateway level
 */
@RestController
@RequestMapping("/internal")
@Slf4j
public class InternalController {

    @Autowired
    private UserService userService;

    @PostMapping("/users")
    public UserDTO createOrUpdateUser(@RequestBody UserDTO userDTO) {
        log.info("Received request to create or update user: {}", userDTO.getUsername());
        UserDTO result = userService.createOrUpdateUser(userDTO);
        log.info("User created or updated successfully: {}", result.getUsername());
        return result;
    }

    @PutMapping("/users/{userId}/roles")
    public void updateRoles(@PathVariable Long userId, @RequestBody UpdateRolesRequest request) {
        log.info("Received request to update roles for userId: {}", userId);
        userService.updateRoles(userId, request.getRoles());
        log.info("Roles updated for userId: {}", userId);
    }

    @GetMapping("/id")
    public Long getUserIdByUsername(@RequestParam String username) {
        log.info("Received request to get userId for username: {}", username);
        Long userId = userService.getUserIdByUsername(username);
        log.info("UserId retrieved: {}", userId);
        return userId;
    }

    @PostMapping("/{userId}/increment-orders")
    public void incrementTotalOrders(@PathVariable Long userId) {
        log.info("Received request to increment total orders for userId: {}", userId);
        userService.incrementTotalOrders(userId);
        log.info("Total orders incremented for userId: {}", userId);
    }
}
