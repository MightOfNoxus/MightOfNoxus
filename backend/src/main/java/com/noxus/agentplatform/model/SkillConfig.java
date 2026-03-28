package com.noxus.agentplatform.model;

import java.time.Instant;
import java.util.List;

public record SkillConfig(
        String id,
        String name,
        String category,
        String version,
        List<String> tags,
        String instructions,
        Instant updatedAt
) {
}
