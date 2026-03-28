package com.noxus.agentplatform.service.hook;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RuntimeParamInjectHook implements ReactAgentExecutionHook {

    @Override
    public void beforeBuild(ReactAgentHookContext context) {
        if (context.runtimeParams().isEmpty()) {
            return;
        }

        String runtimeVars = context.runtimeParams().entrySet().stream()
                .map(e -> "- " + e.getKey() + "=" + String.valueOf(e.getValue()))
                .collect(Collectors.joining("\n"));

        context.finalPrompt(context.finalPrompt() + "\n\n运行时参数：\n" + runtimeVars);
    }

    @Override
    public void beforeCall(ReactAgentHookContext context) {
        Object requestId = context.runtimeParams().get("requestId");
        if (requestId != null && context.userMessage() != null && !context.userMessage().isBlank()) {
            context.userMessage("[requestId=" + requestId + "] " + context.userMessage());
        }
    }
}
