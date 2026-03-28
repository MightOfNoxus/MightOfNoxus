package com.noxus.agentplatform.model;

import java.time.Instant;
import java.util.Map;

public record McpToolConfig(
        String id,
        String name,
        String endpoint,
        String protocol,
        Map<String, String> headers,
        String description,
        Instant updatedAt
) {
}
