package com.noxus.agentplatform.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record ReactAgentCreateRequest(
        @NotBlank String agentId,
        String userMessage,
        Map<String, Object> runtimeParams,
        ReactAgentHookOptions hookOptions
) {
}
