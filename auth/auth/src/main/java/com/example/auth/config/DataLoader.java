package com.example.auth.config;

import com.example.auth.entity.Role;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    @Override
    public void run(String... args) throws Exception {
        // Initialize default roles if they don't exist
        createRoleIfNotExists("ROLE_ADMIN");
        createRoleIfNotExists("ROLE_MANAGER");
        createRoleIfNotExists("ROLE_CUSTOMER");

        // Seed users
        seedUsers();

        log.info("Data seeding completed successfully");
    }

    private void createRoleIfNotExists(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            log.info("Created role: {}", roleName);
        }
    }

    private void seedUsers() {
        // Seed admin user
        if (userRepository.findByUsername("admin").isEmpty()) {
            authService.registerUser("admin", "admin123", "admin@khati.local");
            authService.updateUserRoles(userRepository.findByUsername("admin").get().getId(), List.of("ROLE_ADMIN"));
            log.info("Seeded admin user");
        }

        // Seed manager user
        if (userRepository.findByUsername("manager").isEmpty()) {
            authService.registerUser("manager", "manager123", "manager@khati.local");
            authService.updateUserRoles(userRepository.findByUsername("manager").get().getId(), List.of("ROLE_MANAGER"));
            log.info("Seeded manager user");
        }

        // Seed some customer users
        seedCustomer("customer1", "cust123", "customer1@khati.local");
        seedCustomer("customer2", "cust123", "customer2@khati.local");
        seedCustomer("customer3", "cust123", "customer3@khati.local");
    }

    private void seedCustomer(String username, String password, String email) {
        if (userRepository.findByUsername(username).isEmpty()) {
            authService.registerUser(username, password, email);
            log.info("Seeded customer user: {}", username);
        }
    }
}
