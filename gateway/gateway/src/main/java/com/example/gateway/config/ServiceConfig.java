package com.example.gateway.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ServiceConfig {

    @Value("${services.auth}")
    private String authServiceUrl;

    @Value("${services.user}")
    private String userServiceUrl;

    @Value("${services.inventory}")
    private String inventoryServiceUrl;

    @Value("${services.order}")
    private String orderServiceUrl;
}

