package com.noxus.agentplatform.controller;

import com.noxus.agentplatform.dto.ReactAgentCreateRequest;
import com.noxus.agentplatform.dto.ReactAgentCreateResponse;
import com.noxus.agentplatform.service.ReactAgentOrchestrationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/react-agents")
@CrossOrigin
public class ReactAgentController {

    private final ReactAgentOrchestrationService orchestrationService;

    public ReactAgentController(ReactAgentOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @PostMapping("/create")
    public ReactAgentCreateResponse create(@Valid @RequestBody ReactAgentCreateRequest request) {
        return orchestrationService.createByConfig(request);
    }
}
