package com.noxus.agentplatform.dto;

import java.util.List;

public record ReactAgentCreateResponse(
        String agentId,
        String agentName,
        String status,
        String finalPrompt,
        String answer,
        List<String> appliedHooks,
        String traceId
) {
}
