package com.puppytalk.ai.client.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record HealthCheckResponse(
    String status,
    LocalDateTime timestamp,
    String version,
    Map<String, String> dependencies
) {
    public HealthCheckResponse {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }
    }
}