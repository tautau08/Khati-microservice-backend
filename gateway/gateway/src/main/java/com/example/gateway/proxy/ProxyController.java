package com.example.gateway.proxy;

import com.example.gateway.config.ServiceConfig;
import com.example.gateway.service.ProxyService;
import com.example.gateway.util.PathExtractor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProxyController {

    private final ProxyService proxyService;
    private final ServiceConfig serviceConfig;

    @RequestMapping("/api/auth/**")
    public ResponseEntity<byte[]> authProxy(
            HttpMethod method,
            HttpServletRequest request,
            @RequestHeader HttpHeaders headers,
            @RequestParam MultiValueMap<String, String> query,
            @RequestBody(required = false) byte[] body) {

        String path = PathExtractor.extractRemainingPath(request);
        log.info("Auth proxy received {} request for path: {}", method, path);
        return proxyService.forwardRequest(
                serviceConfig.getAuthServiceUrl(), path, method, headers, query, body);
    }

    @RequestMapping("/api/users/**")
    public ResponseEntity<byte[]> userProxy(
            HttpMethod method,
            HttpServletRequest request,
            @RequestHeader HttpHeaders headers,
            @RequestParam MultiValueMap<String, String> query,
            @RequestBody(required = false) byte[] body) {

        String path = PathExtractor.extractRemainingPath(request);
        log.info("User proxy received {} request for path: {}", method, path);

        // Block access to internal endpoints
        if (path.startsWith("/internal")) {
            log.warn("Blocked access to internal endpoint: {}", path);
            return ResponseEntity.status(403).body("Access to internal endpoints is forbidden".getBytes());
        }

        return proxyService.forwardRequest(
                serviceConfig.getUserServiceUrl(), path, method, headers, query, body);
    }

    @RequestMapping("/api/inventory/**")
    public ResponseEntity<byte[]> inventoryProxy(
            HttpMethod method,
            HttpServletRequest request,
            @RequestHeader HttpHeaders headers,
            @RequestParam MultiValueMap<String, String> query,
            @RequestBody(required = false) byte[] body) {

        String path = PathExtractor.extractRemainingPath(request);
        log.info("Inventory proxy received {} request for path: {}", method, path);

        // Block access to internal endpoints
        if (path.startsWith("/internal")) {
            log.warn("Blocked access to internal endpoint: {}", path);
            return ResponseEntity.status(403).body("Access to internal endpoints is forbidden".getBytes());
        }

        return proxyService.forwardRequest(
                serviceConfig.getInventoryServiceUrl(), path, method, headers, query, body);
    }

    @RequestMapping("/api/orders/**")
    public ResponseEntity<byte[]> orderProxy(
            HttpMethod method,
            HttpServletRequest request,
            @RequestHeader HttpHeaders headers,
            @RequestParam MultiValueMap<String, String> query,
            @RequestBody(required = false) byte[] body) {

        String path = PathExtractor.extractRemainingPath(request);
        log.info("Order proxy received {} request for path: {}", method, path);
        return proxyService.forwardRequest(
                serviceConfig.getOrderServiceUrl(), path, method, headers, query, body);
    }
}
