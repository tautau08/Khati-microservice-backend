package com.example.user.service;

import com.example.user.dto.UserDTO;
import com.example.user.entity.User;
import com.example.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserDTO createOrUpdateUser(UserDTO dto) {
        log.info("Creating or updating user: {}", dto.getUsername());
        User user = userRepository.findByUsername(dto.getUsername()).orElse(new User());
        // Remove setting id from dto to avoid issues with auto-generation
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRoles(dto.getRoles());
        user.setJoiningDate(dto.getJoiningDate());

        if (user.getTotalOrders() == null) {
            user.setTotalOrders(0);
        }

        User savedUser = userRepository.save(user);
        log.info("User saved with id: {}", savedUser.getId());
        return toDTO(savedUser, true);
    }

    public void updateRoles(Long userId, List<String> roles) {
        User user = getUserById(userId);
        user.setRoles(roles);
        userRepository.save(user);
    }

    public UserDTO getUserProfile(String username, Authentication auth) {
        User user = getUserByUsername(username);
        boolean showAllFields = isAdmin(auth) || (isOwner(user, auth) && hasRole(user, "MANAGER"));
        return toDTO(user, showAllFields);
    }

    public UserDTO getUserProfile(Long userId, Authentication auth) {
        User user = getUserById(userId);
        boolean showAllFields = isAdmin(auth) || (isOwner(user, auth) && hasRole(user, "MANAGER"));
        return toDTO(user, showAllFields);
    }

    public List<UserDTO> getCustomersForManager(String username, Authentication auth) {
        return userRepository.findByRolesContaining("ROLE_CUSTOMER")
                .stream()
                .map(user -> toDTO(user, false))
                .collect(Collectors.toList());
    }

    public UserDTO updateManagerInfo(Long userId, UserDTO dto) {
        User user = getUserById(userId);

        if (hasRole(user, "MANAGER")) {
            user.setSalary(dto.getSalary());
            user.setJoiningDate(dto.getJoiningDate());
            user.setAttendance(dto.getAttendance());
        } else if (hasRole(user, "CUSTOMER")) {
            user.setSalary(null);
            user.setJoiningDate(null);
            user.setAttendance(null);
        }

        return toDTO(userRepository.save(user), true);
    }

    public boolean canManagerAccessCustomer(Long customerId, String managerUsername) {
        try {
            User customer = getUserById(customerId);
            return hasRole(customer, "CUSTOMER");
        } catch (Exception e) {
            return false;
        }
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> toDTO(user, true))
                .collect(Collectors.toList());
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Long getUserIdByUsername(String username) {
        User user = getUserByUsername(username);
        return user.getId();
    }

    public void incrementTotalOrders(Long userId) {
        User user = getUserById(userId);
        user.setTotalOrders(user.getTotalOrders() + 1);
        userRepository.save(user);
    }

    private UserDTO toDTO(User user, boolean includeAllFields) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles());
        dto.setTotalOrders(user.getTotalOrders());

        if (includeAllFields) {
            dto.setSalary(user.getSalary());
            dto.setJoiningDate(user.getJoiningDate());
            dto.setAttendance(user.getAttendance());
        }

        return dto;
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isOwner(User user, Authentication auth) {
        return user.getUsername().equals(auth.getName());
    }

    private boolean hasRole(User user, String role) {
        return user.getRoles().contains(role);
    }
}
