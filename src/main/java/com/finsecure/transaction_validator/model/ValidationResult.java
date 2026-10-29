package com.finsecure.transaction_validator.model;

import java.util.List;

// The response we send back: whether the transaction is valid, plus a list of error messages
public record ValidationResult(boolean valid, List<String> errors) {
}