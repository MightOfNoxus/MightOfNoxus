package com.noxus.agentplatform.service;

import com.noxus.agentplatform.dto.AgentConfigRequest;
import com.noxus.agentplatform.dto.McpToolConfigRequest;
import com.noxus.agentplatform.dto.SkillConfigRequest;
import com.noxus.agentplatform.model.AgentConfig;
import com.noxus.agentplatform.model.McpToolConfig;
import com.noxus.agentplatform.model.SkillConfig;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ConfigPlatformService {

    private final Map<String, AgentConfig> agentStore = new LinkedHashMap<>();
    private final Map<String, McpToolConfig> toolStore = new LinkedHashMap<>();
    private final Map<String, SkillConfig> skillStore = new LinkedHashMap<>();

    public List<AgentConfig> listAgents() {
        return new ArrayList<>(agentStore.values());
    }

    public AgentConfig createAgent(AgentConfigRequest request) {
        AgentConfig config = new AgentConfig(
                UUID.randomUUID().toString(),
                request.name(),
                request.description(),
                request.modelProvider(),
                request.modelName(),
                request.linkedToolIds() == null ? List.of() : request.linkedToolIds(),
                request.linkedSkillIds() == null ? List.of() : request.linkedSkillIds(),
                request.promptTemplate(),
                Instant.now()
        );
        agentStore.put(config.id(), config);
        return config;
    }

    public List<McpToolConfig> listTools() {
        return new ArrayList<>(toolStore.values());
    }

    public McpToolConfig createTool(McpToolConfigRequest request) {
        McpToolConfig config = new McpToolConfig(
                UUID.randomUUID().toString(),
                request.name(),
                request.endpoint(),
                request.protocol(),
                request.headers() == null ? Map.of() : request.headers(),
                request.description(),
                Instant.now()
        );
        toolStore.put(config.id(), config);
        return config;
    }

    public List<SkillConfig> listSkills() {
        return new ArrayList<>(skillStore.values());
    }

    public SkillConfig createSkill(SkillConfigRequest request) {
        SkillConfig config = new SkillConfig(
                UUID.randomUUID().toString(),
                request.name(),
                request.category(),
                request.version(),
                request.tags() == null ? List.of() : request.tags(),
                request.instructions(),
                Instant.now()
        );
        skillStore.put(config.id(), config);
        return config;
    }

    public Map<String, Object> summary() {
        return Map.of(
                "agents", agentStore.size(),
                "tools", toolStore.size(),
                "skills", skillStore.size(),
                "readyToBuild", !agentStore.isEmpty() && (!toolStore.isEmpty() || !skillStore.isEmpty())
        );
    }
}
