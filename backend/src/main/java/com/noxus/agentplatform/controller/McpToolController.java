package com.noxus.agentplatform.controller;

import com.noxus.agentplatform.dto.McpToolConfigRequest;
import com.noxus.agentplatform.model.McpToolConfig;
import com.noxus.agentplatform.service.ConfigPlatformService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mcp-tools")
@CrossOrigin
public class McpToolController {

    private final ConfigPlatformService service;

    public McpToolController(ConfigPlatformService service) {
        this.service = service;
    }

    @GetMapping
    public List<McpToolConfig> list() {
        return service.listTools();
    }

    @PostMapping
    public McpToolConfig create(@Valid @RequestBody McpToolConfigRequest request) {
        return service.createTool(request);
    }
}
