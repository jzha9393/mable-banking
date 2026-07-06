package com.mable.banking.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private static final String NUMBER = "1111234522226789";

    private Account accountWith(String balance) {
        return new Account(NUMBER, new BigDecimal(balance));
    }

    @Test
    @DisplayName("deposit increases the balance")
    void depositIncreasesBalance() {
        Account account = accountWith("100.00");
        account.deposit(new BigDecimal("25.50"));
        assertEquals(0, account.balance().compareTo(new BigDecimal("125.50")));
    }

    @Test
    @DisplayName("withdraw decreases the balance")
    void withdrawDecreasesBalance() {
        Account account = accountWith("100.00");
        account.withdraw(new BigDecimal("30.00"));
        assertEquals(0, account.balance().compareTo(new BigDecimal("70.00")));
    }

    @Test
    @DisplayName("an account may be drawn down to exactly zero")
    void withdrawWholeBalanceLeavesZero() {
        Account account = accountWith("100.00");
        account.withdraw(new BigDecimal("100.00"));
        assertEquals(0, account.balance().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("withdrawing more than the balance is refused and leaves the balance untouched")
    void overdraftIsRefused() {
        Account account = accountWith("100.00");
        assertThrows(IllegalStateException.class,
                () -> account.withdraw(new BigDecimal("100.01")));
        assertEquals(0, account.balance().compareTo(new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("canWithdraw is true up to the balance and false beyond it")
    void canWithdrawBoundary() {
        Account account = accountWith("100.00");
        assertTrue(account.canWithdraw(new BigDecimal("100.00")));
        assertFalse(account.canWithdraw(new BigDecimal("100.01")));
    }

    @Test
    @DisplayName("account numbers must be 16 digits")
    void rejectsBadAccountNumber() {
        assertThrows(IllegalArgumentException.class, () -> new Account("123", new BigDecimal("1.00")));
    }

    @Test
    @DisplayName("opening balance cannot be negative")
    void rejectsNegativeOpeningBalance() {
        assertThrows(IllegalArgumentException.class, () -> new Account(NUMBER, new BigDecimal("-1.00")));
    }

    @Test
    @DisplayName("deposit and withdraw reject non-positive amounts")
    void rejectsNonPositiveAmounts() {
        Account account = accountWith("100.00");
        assertThrows(IllegalArgumentException.class, () -> account.deposit(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(new BigDecimal("-5.00")));
    }
}
