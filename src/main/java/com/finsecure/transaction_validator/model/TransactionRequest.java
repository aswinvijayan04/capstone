package com.finsecure.transaction_validator.model;

import java.math.BigDecimal;

// A "record" is a short way to declare an immutable data holder (like a plain JS object shape).
// Spring converts the incoming JSON body into this object automatically.
// BigDecimal is used for money because double/float give rounding errors (e.g. 0.1 + 0.2).
public record TransactionRequest(
        String transactionId,
        String accountId,
        BigDecimal amount,
        String currency) {
}