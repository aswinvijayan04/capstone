package com.finsecure.transaction_validator.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController = "this class handles HTTP requests and returns JSON"
// (like an Express router file)
@RestController
public class HealthController {

    // @GetMapping("/health") = "when someone sends GET /health, run this method"
    // (like app.get('/health', handler))
    @GetMapping("/health")
    public Map<String, String> health() {
        // Spring converts this Map to JSON automatically:
        // {"status":"UP","service":"transaction-validator"}
            return Map.of("status", "UP", "service", "transaction-validator", "version", "v2");
    }
}