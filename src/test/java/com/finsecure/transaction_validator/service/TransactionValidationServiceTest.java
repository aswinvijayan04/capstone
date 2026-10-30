package com.finsecure.transaction_validator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.finsecure.transaction_validator.model.TransactionRequest;
import com.finsecure.transaction_validator.model.ValidationResult;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TransactionValidationServiceTest {

    // Plain object, no Spring needed: this is why we kept the logic out of the controller
    private final TransactionValidationService service = new TransactionValidationService();

    // Helper to build a request quickly so each test stays short
    private TransactionRequest request(String txnId, String accountId, String amount, String currency) {
        BigDecimal amt = (amount == null) ? null : new BigDecimal(amount);
        return new TransactionRequest(txnId, accountId, amt, currency);
    }

    // ---------- Success cases ----------

    @Test
    void validTransactionPasses() {
        ValidationResult result = service.validate(request("TXN-1", "ACC-1", "2500.50", "INR"));

        assertFalse(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void amountExactlyAtLimitPasses() {
        // Boundary test: 100000 is allowed, only values ABOVE it fail
        ValidationResult result = service.validate(request("TXN-1", "ACC-1", "100000", "USD"));

        assertTrue(result.valid());
    }

    @Test
    void lowercaseCurrencyIsAccepted() {
        ValidationResult result = service.validate(request("TXN-1", "ACC-1", "10", "eur"));

        assertTrue(result.valid());
    }

    // ---------- Failure cases ----------

    @Test
    void blankTransactionIdFails() {
        ValidationResult result = service.validate(request("  ", "ACC-1", "10", "INR"));

        assertFalse(result.valid());
        assertTrue(result.errors().contains("transactionId is required"));
    }

    @Test
    void missingAccountIdFails() {
        ValidationResult result = service.validate(request("TXN-1", null, "10", "INR"));

        assertFalse(result.valid());
        assertTrue(result.errors().contains("accountId is required"));
    }

    @Test
    void missingAmountFails() {
        ValidationResult result = service.validate(request("TXN-1", "ACC-1", null, "INR"));

        assertFalse(result.valid());
        assertTrue(result.errors().contains("amount is required"));
    }

    @Test
    void zeroAmountFails() {
        ValidationResult result = service.validate(request("TXN-1", "ACC-1", "0", "INR"));

        assertFalse(result.valid());
        assertTrue(result.errors().contains("amount must be greater than 0"));
    }

    @Test
    void negativeAmountFails() {
        ValidationResult result = service.validate(request("TXN-1", "ACC-1", "-5", "INR"));

        assertFalse(result.valid());
        assertTrue(result.errors().contains("amount must be greater than 0"));
    }

    @Test
    void amountAboveLimitFails() {
        ValidationResult result = service.validate(request("TXN-1", "ACC-1", "100000.01", "INR"));

        assertFalse(result.valid());
        assertTrue(result.errors().contains("amount must not exceed 100000"));
    }

    @Test
    void unsupportedCurrencyFails() {
        ValidationResult result = service.validate(request("TXN-1", "ACC-1", "10", "XYZ"));

        assertFalse(result.valid());
        assertTrue(result.errors().contains("currency must be one of INR, USD, EUR"));
    }

    @Test
    void multipleProblemsAreAllReported() {
        // Negative amount AND bad currency: we expect both errors, not just the first
        ValidationResult result = service.validate(request("TXN-1", "ACC-1", "-10", "XYZ"));

        assertFalse(result.valid());
        assertEquals(2, result.errors().size());
    }

    @Test
    void nullRequestFails() {
        ValidationResult result = service.validate(null);

        assertFalse(result.valid());
        assertEquals(1, result.errors().size());
    }
}