package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Account Class Tests")
class AccountTest {

    private MutableClock mutableClock;
    private final LocalDateTime FAKE_TIMESTAMP = LocalDateTime.of(2024, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        mutableClock = new MutableClock();
        mutableClock.setTime(FAKE_TIMESTAMP); // Set a default time for convenience
    }

    @Nested
    @DisplayName("Initialization")
    class InitializationTests {

        @Test
        @DisplayName("should start with zero balance and empty history")
        void shouldInitializeWithZeroBalance() {
            // Act
            Account account = new Account(mutableClock);

            // Assert
            assertEquals(BigDecimal.ZERO, account.getBalance());
            assertTrue(account.getHistory().isEmpty(), "History should be empty on creation.");
        }
    }

    @Nested
    @DisplayName("Deposits")
    class DepositTests {

        private Account account;

        @BeforeEach
        void createAccount() {
            account = new Account(mutableClock);
        }

        @Test
        @DisplayName("should increase balance and record a complete operation on valid deposit")
        void shouldIncreaseBalanceAndRecordOperationOnDeposit() {
            // Arrange
            BigDecimal depositAmount = new BigDecimal("250.75");

            // Act
            account.deposit(depositAmount);

            // Assert
            assertEquals(new BigDecimal("250.75"), account.getBalance());
            assertEquals(1, account.getHistory().size());

            // --- Detailed History Check ---
            Operation recordedOperation = account.getHistory().getFirst();
            assertNotNull(recordedOperation);
            assertEquals(OperationType.DEPOSIT, recordedOperation.type());
            assertEquals(depositAmount, recordedOperation.amount());
            assertEquals(FAKE_TIMESTAMP, recordedOperation.date());
            assertEquals(new BigDecimal("250.75"), recordedOperation.balance(), "The balance in the operation record should match the final account balance.");
        }

        @Test
        @DisplayName("should correctly track balance and history over a sequence of mixed operations")
        void shouldCorrectlyTrackStateOverMixedOperations() {
            // Arrange
            Account account = new Account(mutableClock); // Starts with 0

            // Act
            // 1. First deposit
            mutableClock.setTime(LocalDateTime.of(2024, 8, 1, 9, 0));
            account.deposit(new BigDecimal("500.00")); // Balance: 500.00

            // 2. Second deposit
            mutableClock.setTime(LocalDateTime.of(2024, 8, 2, 14, 15));
            account.deposit(new BigDecimal("150.00")); // Balance: 650.00

            // 3. A withdrawal
            mutableClock.setTime(LocalDateTime.of(2024, 8, 3, 11, 30));
            account.withdraw(new BigDecimal("75.50")); // Balance: 574.50

            // 4. Final deposit
            mutableClock.setTime(LocalDateTime.of(2024, 8, 5, 16, 0));
            account.deposit(new BigDecimal("25.00")); // Balance: 599.50

            // Assert
            // First, check the final state of the account
            assertEquals(0, account.getBalance().compareTo(new BigDecimal("599.50")), "Final balance should be correctly calculated.");

            // Verify the total number of operations
            List<Operation> history = account.getHistory();
            assertEquals(4, history.size(), "Should have recorded exactly 4 operations.");

            // Spot-check the last operation
            Operation lastOperation = history.get(3);
            assertEquals(OperationType.DEPOSIT, lastOperation.type());
            assertEquals(0, lastOperation.amount().compareTo(new BigDecimal("25.00")));
            assertEquals(0, lastOperation.balance().compareTo(new BigDecimal("599.50")));

            // Get the second-to-last operation
            Operation preLastOperation = history.get(2);
            // Assert that their balances are not numerically equal
            assertTrue(preLastOperation.balance().compareTo(lastOperation.balance()) != 0,
                    "The balance must change after each successful operation.");
        }

        @ParameterizedTest
        @ValueSource(strings = {"-100.00", "-0.01", "0.00"})
        @DisplayName("should throw exception for non-positive deposit amounts")
        void shouldThrowExceptionForNonPositiveDeposit(String amountValue) {
            BigDecimal nonPositiveAmount = new BigDecimal(amountValue);

            // Act & Assert
            Exception exception = assertThrows(IllegalArgumentException.class, () -> account.deposit(nonPositiveAmount));
            assertEquals("Deposit amount must be positive", exception.getMessage());
            assertTrue(account.getHistory().isEmpty(), "History should remain empty after a failed deposit.");
        }
    }

    @Nested
    @DisplayName("Withdrawals")
    class WithdrawalTests {

        private Account account;

        @BeforeEach
        void createAccountWithFunds() {
            // Arrange: Since there's no initial balance constructor,
            // we create an account and then perform a deposit to set it up.
            account = new Account(mutableClock);
            account.deposit(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("should decrease balance and record a complete operation on valid withdrawal")
        void shouldDecreaseBalanceAndRecordOperationOnWithdrawal() {
            // Arrange
            BigDecimal withdrawalAmount = new BigDecimal("300.00");
            LocalDateTime withdrawalTime = LocalDateTime.of(2024, 1, 2, 15, 0);
            mutableClock.setTime(withdrawalTime); // Set a new time for this specific operation

            // Act
            account.withdraw(withdrawalAmount);

            // Assert
            assertEquals(new BigDecimal("700.00"), account.getBalance());
            assertEquals(2, account.getHistory().size(), "History should contain the initial deposit and the withdrawal.");

            // --- Detailed History Check for the withdrawal operation ---
            Operation recordedOperation = account.getHistory().get(1); // The withdrawal is the second operation
            assertNotNull(recordedOperation);
            assertEquals(OperationType.WITHDRAWAL, recordedOperation.type());
            assertEquals(withdrawalAmount, recordedOperation.amount());
            assertEquals(withdrawalTime, recordedOperation.date());
            assertEquals(new BigDecimal("700.00"), recordedOperation.balance(), "The balance in the operation record should match the final account balance.");
        }

        @Test
        @DisplayName("should allow withdrawing the entire balance, resulting in zero")
        void shouldAllowWithdrawingFullBalance() {
            // Act
            assertDoesNotThrow(() -> account.withdraw(new BigDecimal("1000.00")));

            // Assert
            // CORRECT: Use compareTo to check for numerical equality, ignoring scale.
            // This correctly handles the case where 0 is compared to 0.00.
            assertEquals(0, account.getBalance().compareTo(BigDecimal.ZERO), "Balance should be exactly zero after withdrawing all funds.");
        }

        @Test
        @DisplayName("should throw exception for withdrawal exceeding balance")
        void shouldThrowExceptionForInsufficientFunds() {
            BigDecimal excessiveAmount = new BigDecimal("1000.01");

            // Act & Assert
            Exception exception = assertThrows(IllegalArgumentException.class, () -> account.withdraw(excessiveAmount));
            assertEquals("Insufficient funds", exception.getMessage());
            assertEquals(1, account.getHistory().size(), "History should not change after a failed withdrawal.");
        }

        @ParameterizedTest
        @ValueSource(strings = {"-200.00", "-0.01", "0.00"})
        @DisplayName("should throw exception for non-positive withdrawal amounts")
        void shouldThrowExceptionForNonPositiveWithdrawal(String amountValue) {
            BigDecimal nonPositiveAmount = new BigDecimal(amountValue);

            // Act & Assert
            Exception exception = assertThrows(IllegalArgumentException.class, () -> account.withdraw(nonPositiveAmount));
            assertEquals("Withdrawal amount must be positive", exception.getMessage());
        }
    }
}