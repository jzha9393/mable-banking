package com.mable.banking.service;

import com.mable.banking.model.Account;
import com.mable.banking.model.Transfer;
import com.mable.banking.model.TransferOutcome;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BankTest {

    private static final String A = "1111234522226789";
    private static final String B = "1212343433335665";
    private static final String UNKNOWN = "9999999999999999";

    private Account account(String number, String balance) {
        return new Account(number, new BigDecimal(balance));
    }

    private BigDecimal balanceOf(Bank bank, String number) {
        return bank.find(number).orElseThrow().balance();
    }

    @Test
    @DisplayName("a valid transfer debits the source and credits the destination")
    void appliesValidTransfer() {
        Bank bank = new Bank(List.of(account(A, "100.00"), account(B, "10.00")));

        TransferOutcome outcome = bank.process(new Transfer(A, B, new BigDecimal("40.00")));

        assertTrue(outcome.isApplied());
        assertEquals(0, balanceOf(bank, A).compareTo(new BigDecimal("60.00")));
        assertEquals(0, balanceOf(bank, B).compareTo(new BigDecimal("50.00")));
    }

    @Test
    @DisplayName("a transfer from an unknown account is rejected and nothing moves")
    void rejectsUnknownSource() {
        Bank bank = new Bank(List.of(account(B, "10.00")));

        TransferOutcome outcome = bank.process(new Transfer(UNKNOWN, B, new BigDecimal("5.00")));

        assertFalse(outcome.isApplied());
        assertEquals(0, balanceOf(bank, B).compareTo(new BigDecimal("10.00")));
    }

    @Test
    @DisplayName("a transfer to an unknown account is rejected and nothing moves")
    void rejectsUnknownDestination() {
        Bank bank = new Bank(List.of(account(A, "100.00")));

        TransferOutcome outcome = bank.process(new Transfer(A, UNKNOWN, new BigDecimal("5.00")));

        assertFalse(outcome.isApplied());
        assertEquals(0, balanceOf(bank, A).compareTo(new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("an overdrawing transfer is rejected and both balances are untouched")
    void rejectsInsufficientFunds() {
        Bank bank = new Bank(List.of(account(A, "30.00"), account(B, "10.00")));

        TransferOutcome outcome = bank.process(new Transfer(A, B, new BigDecimal("30.01")));

        assertFalse(outcome.isApplied());
        assertEquals(0, balanceOf(bank, A).compareTo(new BigDecimal("30.00")));
        assertEquals(0, balanceOf(bank, B).compareTo(new BigDecimal("10.00")));
    }

    @Test
    @DisplayName("a transfer to the same account is rejected")
    void rejectsSelfTransfer() {
        Bank bank = new Bank(List.of(account(A, "100.00")));

        TransferOutcome outcome = bank.process(new Transfer(A, A, new BigDecimal("5.00")));

        assertFalse(outcome.isApplied());
        assertEquals(0, balanceOf(bank, A).compareTo(new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("transfers apply in order, so a later one can spend money an earlier one delivered")
    void appliesInOrder() {
        Bank bank = new Bank(List.of(account(A, "50.00"), account(B, "0.00")));

        List<TransferOutcome> outcomes = bank.processAll(List.of(
                new Transfer(A, B, new BigDecimal("50.00")),   // B: 0 -> 50
                new Transfer(B, A, new BigDecimal("50.00"))));  // only possible because of the first

        assertTrue(outcomes.get(0).isApplied());
        assertTrue(outcomes.get(1).isApplied());
        assertEquals(0, balanceOf(bank, A).compareTo(new BigDecimal("50.00")));
        assertEquals(0, balanceOf(bank, B).compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("duplicate accounts are rejected when the bank is built")
    void rejectsDuplicateAccounts() {
        assertThrows(IllegalArgumentException.class,
                () -> new Bank(List.of(account(A, "1.00"), account(A, "2.00"))));
    }

    @Test
    @DisplayName("the sample day produces the expected final balances")
    void sampleDayEndToEnd() {
        Bank bank = new Bank(List.of(
                account("1111234522226789", "5000.00"),
                account("1111234522221234", "10000.00"),
                account("2222123433331212", "550.00"),
                account("1212343433335665", "1200.00"),
                account("3212343433335755", "50000.00")));

        bank.processAll(List.of(
                new Transfer("1111234522226789", "1212343433335665", new BigDecimal("500.00")),
                new Transfer("3212343433335755", "2222123433331212", new BigDecimal("1000.00")),
                new Transfer("3212343433335755", "1111234522226789", new BigDecimal("320.50")),
                new Transfer("1111234522221234", "1212343433335665", new BigDecimal("25.60"))));

        assertEquals(0, balanceOf(bank, "1111234522226789").compareTo(new BigDecimal("4820.50")));
        assertEquals(0, balanceOf(bank, "1111234522221234").compareTo(new BigDecimal("9974.40")));
        assertEquals(0, balanceOf(bank, "2222123433331212").compareTo(new BigDecimal("1550.00")));
        assertEquals(0, balanceOf(bank, "1212343433335665").compareTo(new BigDecimal("1725.60")));
        assertEquals(0, balanceOf(bank, "3212343433335755").compareTo(new BigDecimal("48679.50")));
    }
}
