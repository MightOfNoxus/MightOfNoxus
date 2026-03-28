package com.noxus.agentplatform.controller;

import com.noxus.agentplatform.service.ConfigPlatformService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ConfigPlatformService service;

    public DashboardController(ConfigPlatformService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return service.summary();
    }
}
