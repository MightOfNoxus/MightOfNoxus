package com.noxus.agentplatform.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record SkillConfigRequest(
        @NotBlank String name,
        @NotBlank String category,
        @NotBlank String version,
        List<String> tags,
        @NotBlank String instructions
) {
}
