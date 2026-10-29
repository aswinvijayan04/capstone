package com.finsecure.transaction_validator.service;

import com.finsecure.transaction_validator.model.TransactionRequest;
import com.finsecure.transaction_validator.model.ValidationResult;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

// @Service tells Spring "create one instance of this class and let other classes use it"
@Service
public class TransactionValidationService {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000");
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("INR", "USD", "EUR");

    public ValidationResult validate(TransactionRequest request) {
        if (request == null) {
            return new ValidationResult(false, List.of("Request body is required"));
        }

        // Collect ALL failures instead of stopping at the first one
        List<String> errors = new ArrayList<>();

        if (isBlank(request.transactionId())) {
            errors.add("transactionId is required");
        }
        if (isBlank(request.accountId())) {
            errors.add("accountId is required");
        }

        if (request.amount() == null) {
            errors.add("amount is required");
        } else if (request.amount().signum() <= 0) {   // signum() is -1, 0 or 1
            errors.add("amount must be greater than 0");
        } else if (request.amount().compareTo(MAX_AMOUNT) > 0) {   // BigDecimal uses compareTo, not >
            errors.add("amount must not exceed 100000");
        }

        if (request.currency() == null
                || !SUPPORTED_CURRENCIES.contains(request.currency().toUpperCase())) {
            errors.add("currency must be one of INR, USD, EUR");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}