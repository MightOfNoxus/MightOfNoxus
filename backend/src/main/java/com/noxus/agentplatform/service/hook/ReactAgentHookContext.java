package com.noxus.agentplatform.service.hook;

import com.noxus.agentplatform.model.AgentConfig;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ReactAgentHookContext {

    private final String traceId = UUID.randomUUID().toString();
    private final AgentConfig agent;
    private String finalPrompt;
    private String userMessage;
    private boolean blocked;
    private String blockReason;
    private final Map<String, Object> runtimeParams = new LinkedHashMap<>();

    public ReactAgentHookContext(AgentConfig agent, String finalPrompt, String userMessage, Map<String, Object> params) {
        this.agent = agent;
        this.finalPrompt = finalPrompt;
        this.userMessage = userMessage;
        if (params != null) {
            runtimeParams.putAll(params);
        }
    }

    public String traceId() { return traceId; }
    public AgentConfig agent() { return agent; }
    public String finalPrompt() { return finalPrompt; }
    public void finalPrompt(String finalPrompt) { this.finalPrompt = finalPrompt; }
    public String userMessage() { return userMessage; }
    public void userMessage(String userMessage) { this.userMessage = userMessage; }
    public Map<String, Object> runtimeParams() { return runtimeParams; }
    public boolean blocked() { return blocked; }
    public String blockReason() { return blockReason; }

    public void block(String reason) {
        this.blocked = true;
        this.blockReason = reason;
    }
}
