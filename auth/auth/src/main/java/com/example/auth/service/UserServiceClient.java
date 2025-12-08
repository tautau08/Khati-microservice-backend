package com.example.auth.service;

import com.example.auth.dto.UpdateRolesRequestDTO;
import com.example.auth.dto.UserSyncDTO;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final RestClient restClient;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Async
    public void syncUser(User user) {
        try {
            String url = userServiceUrl + "/internal/users";

            UserSyncDTO dto = new UserSyncDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setRoles(user.getRoles().stream().map(Role::getName).toList());
            dto.setJoiningDate(user.getCreatedAt().toLocalDate());

            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto)
                    .retrieve()
                    .toBodilessEntity();

            log.info("User synced with user-service: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Error syncing user with user-service: {}", e.getMessage());
        }
    }

    @Async
    public void syncUserRoles(Long userId, List<String> roles) {
        try {
            String url = userServiceUrl + "/internal/users/" + userId + "/roles";

            UpdateRolesRequestDTO requestDTO = new UpdateRolesRequestDTO();
            requestDTO.setRoles(roles);

            restClient.put()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestDTO)
                    .retrieve()
                    .toBodilessEntity();

            log.info("User roles synced with user-service for userId: {}", userId);
        } catch (Exception e) {
            log.error("Error syncing user roles with user-service: {}", e.getMessage());
        }
    }
}
