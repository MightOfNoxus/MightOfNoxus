package com.noxus.agentplatform.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record McpToolConfigRequest(
        @NotBlank String name,
        @NotBlank String endpoint,
        @NotBlank String protocol,
        Map<String, String> headers,
        String description
) {
}
