package com.example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Account {
    private BigDecimal balance;
    private final List<Operation> history;
    private final DateProvider date; // Injected dependency

    /**
     * Primary constructor for production use.
     * Initializes a new account with a zero balance.
     */
    public Account() {
        // Delegates to the testable constructor, providing the real date implementation.
        this(new SystemClock());
    }

    /**
     * Package-private constructor for testing, allowing injection of a DateProvider.
     * @param date The DateProvider implementation (e.g., a real or test clock).
     */
    Account(DateProvider date) {
        this.history = new ArrayList<>();
        this.balance = BigDecimal.ZERO;
        this.date = date;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public List<Operation> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        balance = balance.add(amount);
        history.add(new Operation(OperationType.DEPOSIT, date.now(), amount, balance));
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        balance = balance.subtract(amount);
        history.add(new Operation(OperationType.WITHDRAWAL, date.now(), amount, balance));
    }
}