package com.noxus.agentplatform.controller;

import com.noxus.agentplatform.dto.AgentConfigRequest;
import com.noxus.agentplatform.model.AgentConfig;
import com.noxus.agentplatform.service.ConfigPlatformService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
@CrossOrigin
public class AgentConfigController {

    private final ConfigPlatformService service;

    public AgentConfigController(ConfigPlatformService service) {
        this.service = service;
    }

    @GetMapping
    public List<AgentConfig> list() {
        return service.listAgents();
    }

    @PostMapping
    public AgentConfig create(@Valid @RequestBody AgentConfigRequest request) {
        return service.createAgent(request);
    }
}
