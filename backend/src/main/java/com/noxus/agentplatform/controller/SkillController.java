package com.noxus.agentplatform.controller;

import com.noxus.agentplatform.dto.SkillConfigRequest;
import com.noxus.agentplatform.model.SkillConfig;
import com.noxus.agentplatform.service.ConfigPlatformService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@CrossOrigin
public class SkillController {

    private final ConfigPlatformService service;

    public SkillController(ConfigPlatformService service) {
        this.service = service;
    }

    @GetMapping
    public List<SkillConfig> list() {
        return service.listSkills();
    }

    @PostMapping
    public SkillConfig create(@Valid @RequestBody SkillConfigRequest request) {
        return service.createSkill(request);
    }
}
