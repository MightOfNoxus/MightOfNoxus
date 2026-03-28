package com.noxus.agentplatform.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AgentConfigRequest(
        @NotBlank String name,
        String description,
        @NotBlank String modelProvider,
        @NotBlank String modelName,
        List<String> linkedToolIds,
        List<String> linkedSkillIds,
        @NotBlank String promptTemplate
) {
}
