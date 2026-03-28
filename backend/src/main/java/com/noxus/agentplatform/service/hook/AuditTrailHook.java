package com.noxus.agentplatform.service.hook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditTrailHook implements ReactAgentExecutionHook {

    private static final Logger log = LoggerFactory.getLogger(AuditTrailHook.class);

    @Override
    public void beforeBuild(ReactAgentHookContext context) {
        log.info("[{}] start build react-agent: {}", context.traceId(), context.agent().name());
    }

    @Override
    public void afterCall(ReactAgentHookContext context, String answer) {
        log.info("[{}] react-agent call finished, answerLength={}", context.traceId(), answer == null ? 0 : answer.length());
    }

    @Override
    public void onError(ReactAgentHookContext context, Exception error) {
        log.error("[{}] react-agent call error: {}", context.traceId(), error.getMessage());
    }
}
