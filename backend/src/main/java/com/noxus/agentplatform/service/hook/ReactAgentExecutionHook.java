package com.noxus.agentplatform.service.hook;

public interface ReactAgentExecutionHook {

    default String name() {
        return getClass().getSimpleName();
    }

    default void beforeBuild(ReactAgentHookContext context) {
    }

    default void beforeCall(ReactAgentHookContext context) {
    }

    default void afterCall(ReactAgentHookContext context, String answer) {
    }

    default void onError(ReactAgentHookContext context, Exception error) {
    }
}
