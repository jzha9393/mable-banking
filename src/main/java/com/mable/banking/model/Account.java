package com.mable.banking.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A customer account identified by a 16-digit number, holding a balance that is
 * never allowed to fall below zero. Money is held as {@link BigDecimal} so that
 * cents are exact.
 */
public final class Account {

    private static final Pattern SIXTEEN_DIGITS = Pattern.compile("\\d{16}");

    private final String number;
    private BigDecimal balance;

    public Account(String number, BigDecimal openingBalance) {
        this.number = requireValidNumber(number);
        this.balance = requireNonNegative(openingBalance);
    }

    public String number() {
        return number;
    }

    public BigDecimal balance() {
        return balance;
    }

    /** True when the account can pay {@code amount} without dropping below zero. */
    public boolean canWithdraw(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }

    /** Subtracts the amount from balance, or throws if it would go negative. */
    public void withdraw(BigDecimal amount) {
        requirePositive(amount);
        if (!canWithdraw(amount)) {
            throw new IllegalStateException(
                    "account %s holds %s, cannot withdraw %s".formatted(number, balance, amount));
        }
        balance = balance.subtract(amount);
    }

    /** Adds the amount to balance. */
    public void deposit(BigDecimal amount) {
        requirePositive(amount);
        balance = balance.add(amount);
    }

    private static String requireValidNumber(String number) {
        Objects.requireNonNull(number, "account number is required");
        if (!SIXTEEN_DIGITS.matcher(number).matches()) {
            throw new IllegalArgumentException("account number must be 16 digits: " + number);
        }
        return number;
    }

    private static BigDecimal requireNonNegative(BigDecimal balance) {
        Objects.requireNonNull(balance, "balance is required");
        if (balance.signum() < 0) {
            throw new IllegalArgumentException("opening balance cannot be negative: " + balance);
        }
        return balance;
    }

    private static void requirePositive(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount is required");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive: " + amount);
        }
    }
}
