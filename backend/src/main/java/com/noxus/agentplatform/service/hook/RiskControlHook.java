package com.noxus.agentplatform.service.hook;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RiskControlHook implements ReactAgentExecutionHook {

    private static final List<String> BLOCK_TERMS = List.of("泄露密钥", "导出全部密码", "rm -rf /");

    @Override
    public void beforeCall(ReactAgentHookContext context) {
        String msg = context.userMessage();
        if (msg == null || msg.isBlank()) {
            return;
        }
        boolean blocked = BLOCK_TERMS.stream().anyMatch(msg::contains);
        if (blocked) {
            context.block("命中风险控制策略，已阻断请求");
        }
    }
}
