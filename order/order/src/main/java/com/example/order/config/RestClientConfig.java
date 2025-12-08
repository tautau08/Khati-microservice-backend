package com.example.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    @Value("${user.service.url}")
    private String userServiceUrl;

    private RestClient.Builder builderWithAuthHeader() {
        return RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    if (auth instanceof JwtAuthenticationToken jwtAuth) {
                        String tokenValue = jwtAuth.getToken().getTokenValue();
                        request.getHeaders().setBearerAuth(tokenValue);
                    }
                    return execution.execute(request, body);
                });
    }

    @Bean
    public RestClient inventoryRestClient() {
        return builderWithAuthHeader()
                .baseUrl(inventoryServiceUrl)
                .build();
    }

    @Bean
    public RestClient userRestClient() {
        return builderWithAuthHeader()
                .baseUrl(userServiceUrl)
                .build();
    }
}
