package com.noxus.agentplatform.model;

import java.time.Instant;
import java.util.List;

public record AgentConfig(
        String id,
        String name,
        String description,
        String modelProvider,
        String modelName,
        List<String> linkedToolIds,
        List<String> linkedSkillIds,
        String promptTemplate,
        Instant updatedAt
) {
}
