package com.example.gateway.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ProxyService {

    private WebClient webClient;

    // Headers that should not be forwarded
    private static final List<String> EXCLUDED_HEADERS = Arrays.asList(
            "host", "connection", "keep-alive", "transfer-encoding",
            "te", "trailer", "proxy-authorization", "proxy-authenticate", "upgrade"
    );

    @PostConstruct
    void init() {
        this.webClient = WebClient.builder().build();
    }

    public ResponseEntity<byte[]> forwardRequest(
            String targetUrl,
            String path,
            HttpMethod method,
            HttpHeaders headers,
            MultiValueMap<String, String> queryParams,
            byte[] body) {

        URI uri = buildUri(targetUrl, path, queryParams);

        log.info("Forwarding {} request to: {}", method, uri);
        if (body != null && body.length > 0) {
            log.debug("Request body size: {} bytes", body.length);
        }

        WebClient.RequestBodySpec requestSpec = webClient.method(method)
                .uri(uri)
                .headers(h -> h.addAll(filterHeaders(headers)));

        return executeRequest(requestSpec, method, headers, body);
    }

    private URI buildUri(String baseUrl, String path, MultiValueMap<String, String> queryParams) {
        String cleanPath = cleanPath(path);
        return UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path(cleanPath)
                .queryParams(queryParams)
                .build(true)
                .toUri();
    }

    private String cleanPath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private ResponseEntity<byte[]> executeRequest(
            WebClient.RequestBodySpec requestSpec,
            HttpMethod method,
            HttpHeaders headers,
            byte[] body) {

        // Use exchangeToMono so 4xx/5xx responses are passed through instead of thrown as exceptions.
        try {
            WebClient.RequestHeadersSpec<?> headersSpec;

            if (shouldIncludeBody(method, body)) {
                MediaType contentType = getContentType(headers);
                headersSpec = requestSpec
                        .contentType(contentType)
                        .body(BodyInserters.fromValue(body));
            } else {
                headersSpec = requestSpec;
            }

            ResponseEntity<byte[]> response = headersSpec
                    .exchangeToMono(resp -> resp.toEntity(byte[].class))
                    .block();

            // Filter response headers to avoid chunked encoding issues
            if (response != null) {
                log.info("Received response with status: {}", response.getStatusCode());
                HttpHeaders filteredResponseHeaders = filterResponseHeaders(response.getHeaders());
                return ResponseEntity
                        .status(response.getStatusCode())
                        .headers(filteredResponseHeaders)
                        .body(response.getBody());
            }

            return response;
        } catch (Exception e) {
            // Transport / unexpected client error; map to 502 Bad Gateway so client knows it's a gateway failure.
            log.error("Gateway error while forwarding request: {}", e.getMessage(), e);
            String msg = ("Gateway error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(msg.getBytes(StandardCharsets.UTF_8));
        }
    }

    private boolean shouldIncludeBody(HttpMethod method, byte[] body) {
        return method != HttpMethod.GET
                && method != HttpMethod.DELETE
                && body != null
                && body.length > 0;
    }

    private HttpHeaders filterHeaders(HttpHeaders incoming) {
        HttpHeaders outgoing = new HttpHeaders();

        incoming.forEach((name, values) -> {
            if (!EXCLUDED_HEADERS.contains(name.toLowerCase())) {
                outgoing.addAll(name, values);
            }
        });

        return outgoing;
    }

    private HttpHeaders filterResponseHeaders(HttpHeaders incoming) {
        HttpHeaders outgoing = new HttpHeaders();

        incoming.forEach((name, values) -> {
            String lowerName = name.toLowerCase();
            // Exclude problematic headers that cause chunked encoding issues
            if (!lowerName.equals("transfer-encoding") &&
                !lowerName.equals("connection") &&
                !lowerName.equals("keep-alive")) {
                outgoing.addAll(name, values);
            }
        });

        return outgoing;
    }

    private MediaType getContentType(HttpHeaders headers) {
        MediaType contentType = headers.getContentType();
        return contentType != null ? contentType : MediaType.APPLICATION_JSON;
    }
}
