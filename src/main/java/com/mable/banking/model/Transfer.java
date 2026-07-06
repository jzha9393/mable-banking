package com.mable.banking.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A well-formed request to move {@code amount} from one account to another.
 * Structural validity (16-digit accounts, positive amount) is enforced here;
 * business rules (accounts exist, sufficient funds) live in {@link Bank}.
 */
public record Transfer(String from, String to, BigDecimal amount) {

    private static final Pattern SIXTEEN_DIGITS = Pattern.compile("\\d{16}");

    public Transfer {
        from = requireValidNumber(from, "from");
        to = requireValidNumber(to, "to");
        Objects.requireNonNull(amount, "amount is required");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("transfer amount must be positive: " + amount);
        }
    }

    private static String requireValidNumber(String number, String field) {
        Objects.requireNonNull(number, field + " account is required");
        if (!SIXTEEN_DIGITS.matcher(number).matches()) {
            throw new IllegalArgumentException(field + " account must be 16 digits: " + number);
        }
        return number;
    }
}
