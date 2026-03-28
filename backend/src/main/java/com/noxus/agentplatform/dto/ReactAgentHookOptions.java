package com.noxus.agentplatform.dto;

public record ReactAgentHookOptions(
        Integer maxModelCalls,
        Boolean enableToolRetry,
        Integer toolRetryTimes,
        Boolean enableContextEditing,
        Boolean enablePiiRedaction
) {
}
