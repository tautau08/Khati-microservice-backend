package com.example.user.controller;

import com.example.user.dto.UserDTO;
import com.example.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserDTO getCurrentUser(Authentication auth) {
        return userService.getUserProfile(auth.getName(), auth);
    }

    @GetMapping("/customers")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<UserDTO> getCustomers(Authentication auth) {
        return userService.getCustomersForManager(auth.getName(), auth);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @userService.getUserById(#id).username == authentication.name")
    public UserDTO getUserById(@PathVariable Long id, Authentication auth) {
        return userService.getUserProfile(id, auth);
    }

    @PutMapping("/{id}/manager-info")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public UserDTO updateManagerInfo(@PathVariable Long id, @RequestBody UserDTO dto) {
        return userService.updateManagerInfo(id, dto);
    }


}
