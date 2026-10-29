package com.finsecure.transaction_validator.controller;

import com.finsecure.transaction_validator.model.TransactionRequest;
import com.finsecure.transaction_validator.model.ValidationResult;
import com.finsecure.transaction_validator.service.TransactionValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ValidationController {

    private final TransactionValidationService service;

    // Constructor injection: Spring passes in the service automatically (dependency injection)
    public ValidationController(TransactionValidationService service) {
        this.service = service;
    }

    // @PostMapping = handle POST /validate; @RequestBody = turn the JSON body into a TransactionRequest
    @PostMapping("/validate")
    public ResponseEntity<ValidationResult> validate(@RequestBody TransactionRequest request) {
        ValidationResult result = service.validate(request);
        // 200 if valid, 400 if not
        return result.valid()
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }
}