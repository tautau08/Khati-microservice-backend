package com.example.gateway.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

public class PathExtractor {

    public static String extractRemainingPath(HttpServletRequest request) {
        // Get the full path within handler mapping
        String pathWithin = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        if (pathWithin == null) {
            pathWithin = request.getRequestURI();
        }

        return pathWithin;
    }
}

