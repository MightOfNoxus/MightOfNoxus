package com.noxus.agentplatform.service;

import com.noxus.agentplatform.dto.ReactAgentCreateRequest;
import com.noxus.agentplatform.dto.ReactAgentCreateResponse;
import com.noxus.agentplatform.dto.ReactAgentHookOptions;
import com.noxus.agentplatform.model.AgentConfig;
import com.noxus.agentplatform.model.McpToolConfig;
import com.noxus.agentplatform.model.SkillConfig;
import com.noxus.agentplatform.service.hook.ReactAgentExecutionHook;
import com.noxus.agentplatform.service.hook.ReactAgentHookContext;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
public class ReactAgentOrchestrationService {

    private final ConfigPlatformService configService;
    private final ApplicationContext applicationContext;
    private final List<ReactAgentExecutionHook> executionHooks;

    public ReactAgentOrchestrationService(
            ConfigPlatformService configService,
            ApplicationContext applicationContext,
            List<ReactAgentExecutionHook> executionHooks
    ) {
        this.configService = configService;
        this.applicationContext = applicationContext;
        this.executionHooks = executionHooks;
    }

    public ReactAgentCreateResponse createByConfig(ReactAgentCreateRequest request) {
        AgentConfig agent = configService.getAgent(request.agentId())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Agent 配置不存在"));

        String finalPrompt = buildPrompt(agent);
        ReactAgentHookContext hookContext = new ReactAgentHookContext(
                agent,
                finalPrompt,
                request.userMessage(),
                request.runtimeParams()
        );

        List<String> appliedHooks = executionHooks.stream().map(ReactAgentExecutionHook::name).toList();
        executionHooks.forEach(h -> h.beforeBuild(hookContext));

        if (hookContext.blocked()) {
            return new ReactAgentCreateResponse(
                    agent.id(),
                    agent.name(),
                    "BLOCKED",
                    hookContext.finalPrompt(),
                    hookContext.blockReason(),
                    appliedHooks,
                    hookContext.traceId()
            );
        }

        try {
            String answer = invokeReactAgent(hookContext, request.hookOptions());
            executionHooks.forEach(h -> h.afterCall(hookContext, answer));
            return new ReactAgentCreateResponse(
                    agent.id(),
                    agent.name(),
                    answer == null ? "CREATED" : "CREATED_AND_CALLED",
                    hookContext.finalPrompt(),
                    answer,
                    appliedHooks,
                    hookContext.traceId()
            );
        } catch (Exception e) {
            executionHooks.forEach(h -> h.onError(hookContext, e));
            throw e;
        }
    }

    private String buildPrompt(AgentConfig agent) {
        List<SkillConfig> linkedSkills = agent.linkedSkillIds().stream()
                .map(configService::getSkill)
                .flatMap(Optional::stream)
                .toList();

        List<McpToolConfig> linkedTools = agent.linkedToolIds().stream()
                .map(configService::getTool)
                .flatMap(Optional::stream)
                .toList();

        String skillsPrompt = linkedSkills.isEmpty() ? "无" : linkedSkills.stream()
                .map(skill -> "- " + skill.name() + "(" + skill.category() + "): " + skill.instructions())
                .collect(Collectors.joining("\n"));

        String toolsPrompt = linkedTools.isEmpty() ? "无" : linkedTools.stream()
                .map(tool -> "- " + tool.name() + "(" + tool.protocol() + "): " + tool.endpoint())
                .collect(Collectors.joining("\n"));

        return """
                你是通过配置平台构建的企业级 ReactAgent。
                Agent名称：%s
                Agent描述：%s

                基础系统提示词：
                %s

                可用Skills：
                %s

                可用MCP工具：
                %s

                工作要求：
                1) 优先使用技能规范进行推理。
                2) 需要外部信息时调用MCP工具。
                3) 输出结构清晰、结论明确。
                """.formatted(
                agent.name(),
                agent.description() == null ? "无" : agent.description(),
                agent.promptTemplate(),
                skillsPrompt,
                toolsPrompt
        );
    }

    private String invokeReactAgent(ReactAgentHookContext context, ReactAgentHookOptions options) {
        try {
            Class<?> reactAgentClass = Class.forName("com.alibaba.cloud.ai.graph.agent.ReactAgent");
            Object builder = reactAgentClass.getMethod("builder").invoke(null);

            builder = invokeBuilder(builder, "name", new Class[]{String.class}, new Object[]{context.agent().name()});
            Object chatModelBean = findChatModelBean();
            builder = invokeSingleArgBuilder(builder, "model", chatModelBean);

            // 兼容不同版本字段名
            builder = invokeBuilderIfExists(builder, "systemPrompt", new Class[]{String.class}, new Object[]{context.finalPrompt()});
            builder = invokeBuilderIfExists(builder, "instructions", new Class[]{String.class}, new Object[]{context.finalPrompt()});

            // Spring AI Alibaba 官方 hooks/interceptors，按请求参数动态启用
            builder = applyFrameworkHookOptions(builder, options);

            Object reactAgent = builder.getClass().getMethod("build").invoke(builder);

            executionHooks.forEach(h -> h.beforeCall(context));
            if (context.blocked()) {
                return context.blockReason();
            }

            if (context.userMessage() == null || context.userMessage().isBlank()) {
                return null;
            }

            Method callMethod = reactAgent.getClass().getMethod("call", String.class);
            Object assistantMessage = callMethod.invoke(reactAgent, context.userMessage());
            try {
                return String.valueOf(assistantMessage.getClass().getMethod("getText").invoke(assistantMessage));
            } catch (NoSuchMethodException ex) {
                return String.valueOf(assistantMessage);
            }
        } catch (ClassNotFoundException e) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE,
                    "当前后端未引入 spring-ai-alibaba-agent 模块，请补充依赖后重试", e);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE,
                    "ReactAgent 创建失败，请检查模型Bean、依赖版本与配置", e);
        }
    }

    private Object applyFrameworkHookOptions(Object builder, ReactAgentHookOptions options) throws Exception {
        if (options == null) {
            return builder;
        }

        List<Object> hooks = new ArrayList<>();
        List<Object> interceptors = new ArrayList<>();

        if (options.maxModelCalls() != null && options.maxModelCalls() > 0) {
            Object modelCallLimitHook = buildObjectByBuilder(
                    "com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook",
                    Map.of("runLimit", options.maxModelCalls(), "exitBehavior", "ERROR")
            );
            if (modelCallLimitHook != null) {
                hooks.add(modelCallLimitHook);
            }
        }

        if (Boolean.TRUE.equals(options.enableToolRetry())) {
            Object toolRetryInterceptor = buildObjectByBuilder(
                    "com.alibaba.cloud.ai.graph.agent.interceptor.toolretry.ToolRetryInterceptor",
                    Map.of("maxRetries", options.toolRetryTimes() == null ? 2 : options.toolRetryTimes())
            );
            if (toolRetryInterceptor != null) {
                interceptors.add(toolRetryInterceptor);
            }
        }

        if (Boolean.TRUE.equals(options.enableContextEditing())) {
            Object contextEditingInterceptor = buildObjectByBuilder(
                    "com.alibaba.cloud.ai.graph.agent.interceptor.context.ContextEditingInterceptor",
                    Map.of("triggerTokenThreshold", 120000L, "clearTokenThreshold", 80000L)
            );
            if (contextEditingInterceptor != null) {
                interceptors.add(contextEditingInterceptor);
            }
        }

        if (!hooks.isEmpty()) {
            builder = invokeCollectionAsVarArgs(builder, "hooks", hooks);
        }
        if (!interceptors.isEmpty()) {
            builder = invokeCollectionAsVarArgs(builder, "interceptors", interceptors);
        }

        return builder;
    }

    private Object buildObjectByBuilder(String className, Map<String, Object> setters) {
        try {
            Class<?> cls = Class.forName(className);
            Object builder = cls.getMethod("builder").invoke(null);
            for (Map.Entry<String, Object> entry : setters.entrySet()) {
                builder = invokeSetterIfExists(builder, entry.getKey(), entry.getValue());
            }
            return builder.getClass().getMethod("build").invoke(builder);
        } catch (Exception ignore) {
            return null;
        }
    }

    private Object invokeSetterIfExists(Object builder, String methodName, Object arg) throws Exception {
        for (Method method : builder.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
                Class<?> paramType = method.getParameterTypes()[0];
                if (arg instanceof String s && paramType.isEnum()) {
                    Object enumValue = Enum.valueOf((Class<Enum>) paramType, s);
                    return method.invoke(builder, enumValue);
                }
                if (isCompatible(paramType, arg)) {
                    return method.invoke(builder, arg);
                }
            }
        }
        return builder;
    }

    private boolean isCompatible(Class<?> targetType, Object arg) {
        if (arg == null) {
            return true;
        }
        Class<?> src = arg.getClass();
        if (targetType.isAssignableFrom(src)) {
            return true;
        }
        if (targetType == int.class && src == Integer.class) {
            return true;
        }
        if (targetType == long.class && src == Long.class) {
            return true;
        }
        if (targetType == boolean.class && src == Boolean.class) {
            return true;
        }
        return false;
    }

    private Object invokeCollectionAsVarArgs(Object builder, String methodName, List<Object> values) throws Exception {
        for (Method method : builder.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 1 && method.getParameterTypes()[0].isArray()) {
                Class<?> componentType = method.getParameterTypes()[0].componentType();
                Object arr = Array.newInstance(componentType, values.size());
                for (int i = 0; i < values.size(); i++) {
                    Array.set(arr, i, values.get(i));
                }
                return method.invoke(builder, arr);
            }
        }
        return builder;
    }

    private Object findChatModelBean() throws Exception {
        Class<?> chatModelClass = Class.forName("org.springframework.ai.chat.model.ChatModel");
        Map<?, ?> beans = applicationContext.getBeansOfType((Class) chatModelClass);
        if (beans.isEmpty()) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE,
                    "未找到 ChatModel Bean，请先配置 Spring AI Alibaba 模型（如 DashScope）");
        }
        return beans.values().iterator().next();
    }

    private Object invokeBuilder(Object target, String methodName, Class<?>[] paramTypes, Object[] args) throws Exception {
        Method method = target.getClass().getMethod(methodName, paramTypes);
        return method.invoke(target, args);
    }

    private Object invokeBuilderIfExists(Object target, String methodName, Class<?>[] paramTypes, Object[] args) throws Exception {
        try {
            Method method = target.getClass().getMethod(methodName, paramTypes);
            return method.invoke(target, args);
        } catch (NoSuchMethodException ignore) {
            return target;
        }
    }

    private Object invokeSingleArgBuilder(Object builder, String methodName, Object arg) throws Exception {
        for (Method method : builder.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 1
                    && method.getParameterTypes()[0].isAssignableFrom(arg.getClass())) {
                return method.invoke(builder, arg);
            }
        }
        throw new ResponseStatusException(SERVICE_UNAVAILABLE, "ReactAgent builder 未找到可用的 model(...) 方法");
    }
}
