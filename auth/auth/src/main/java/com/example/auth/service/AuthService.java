package com.example.auth.service;

import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.exception.AuthenticationException;
import com.example.auth.exception.RegistrationException;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserServiceClient userServiceClient;

    public User registerUser(String username, String password, String email) {
        log.info("Attempting to register user with username: {} and email: {}", username, email);

        if (userRepository.existsByUsername(username)) {
            log.warn("Registration failed: Username already exists - {}", username);
            throw new RegistrationException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed: Email already exists - {}", email);
            throw new RegistrationException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);

        // assign a default role (ensure this role exists in DB)
        Role defaultRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.getRoles().add(defaultRole);

        User savedUser = userRepository.save(user);
        log.info("User saved to database with id: {}", savedUser.getId());

        // Sync with user-service
        userServiceClient.syncUser(savedUser);

        return savedUser;
    }

    public String loginUser(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Invalid username or password");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        try {
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new AuthenticationException("Invalid username or password");
            }
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Invalid username or password");
        }

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        // call the token generator that includes roles
        return jwtUtil.generateToken(username, roles);
    }

    public void updateUserRoles(Long userId, List<String> newRoles) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<String> currentRoles = user.getRoles().stream().map(Role::getName).toList();

        if (currentRoles.equals(newRoles)) {
            throw new RuntimeException("Roles are already up to date");
        }

        Set<Role> roles = newRoles.stream()
                .map(name -> roleRepository.findByName(name).orElseThrow(() -> new RuntimeException("Role not found")))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        userRepository.save(user);

        // Sync roles with user-service
        userServiceClient.syncUserRoles(userId, newRoles);
    }
}
