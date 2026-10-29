package com.finsecure.transaction_validator.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class HealthControllerTest {

    @Test
    void healthReturnsUp() {
        Map<String, String> body = new HealthController().health();

        assertEquals("UP", body.get("status"));
        assertEquals("transaction-validator", body.get("service"));
    }
}