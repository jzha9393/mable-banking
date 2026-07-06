package com.mable.banking.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransferTest {

    private static final String A = "1111234522226789";
    private static final String B = "1212343433335665";

    @Test
    @DisplayName("holds its from, to and amount")
    void holdsValues() {
        Transfer transfer = new Transfer(A, B, new BigDecimal("10.00"));
        assertEquals(A, transfer.from());
        assertEquals(B, transfer.to());
        assertEquals(0, transfer.amount().compareTo(new BigDecimal("10.00")));
    }

    @Test
    @DisplayName("amount must be positive")
    void rejectsNonPositiveAmount() {
        assertThrows(IllegalArgumentException.class, () -> new Transfer(A, B, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new Transfer(A, B, new BigDecimal("-1.00")));
    }

    @Test
    @DisplayName("account numbers must be 16 digits")
    void rejectsBadAccountNumbers() {
        assertThrows(IllegalArgumentException.class, () -> new Transfer("123", B, new BigDecimal("1.00")));
        assertThrows(IllegalArgumentException.class, () -> new Transfer(A, "nope", new BigDecimal("1.00")));
    }
}
