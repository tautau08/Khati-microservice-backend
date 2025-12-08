package com.example.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class UserClient {

    private final @Qualifier("userRestClient") RestClient userRestClient;

    public Long getUserIdByUsername(String username) {
        return userRestClient.get()
                .uri("/internal/id?username={username}", username)
                .retrieve()
                .body(Long.class);
    }

    public void incrementTotalOrders(Long userId) {
        ResponseEntity<Void> ignored = userRestClient.post()
                .uri("/internal/{userId}/increment-orders", userId)
                .retrieve()
                .toBodilessEntity();
    }
}
