package com.example;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a single bank account operation.
 * This is an immutable data carrier for an operation's details.
 *
 * @param type    the type of operation (e.g., DEPOSIT, WITHDRAWAL)
 * @param date    the date and time the operation occurred
 * @param amount  the amount of the transaction
 * @param balance the account balance after the operation was completed
 */
public record Operation(OperationType type, LocalDateTime date, BigDecimal amount, BigDecimal balance) {
}