package com.example.gateway.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to block all requests to /internal/* endpoints
 * Provides an additional security layer to prevent external access to service-to-service communication endpoints
 */
@Component
@Order(1)
@Slf4j
public class InternalEndpointFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        // Block any request that contains /internal/ after the service prefix
        // Examples: /api/users/internal/*, /api/inventory/internal/*
        if (path.matches(".*/internal(/.*)?")) {
            log.warn("Blocked attempt to access internal endpoint: {} from IP: {}",
                    path, httpRequest.getRemoteAddr());
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Access to internal endpoints is forbidden\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}

