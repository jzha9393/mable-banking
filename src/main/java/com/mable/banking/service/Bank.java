package com.mable.banking.service;

import com.mable.banking.model.Account;
import com.mable.banking.model.Transfer;
import com.mable.banking.model.TransferOutcome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Holds the accounts for a single company and applies transfers between them,
 * enforcing the rule that no account may be overdrawn. A transfer is applied in
 * full or not at all: the balance is checked before either side is touched, so a
 * rejected transfer never leaves money half-moved.
 */
public final class Bank {

    private final Map<String, Account> accounts = new LinkedHashMap<>();

    /** Creates a bank from a set of accounts; rejects duplicates. */
    public Bank(Collection<Account> accounts) {
        for (Account account : accounts) {
            if (this.accounts.putIfAbsent(account.number(), account) != null) {
                throw new IllegalArgumentException("duplicate account: " + account.number());
            }
        }
    }

    public Optional<Account> find(String number) {
        return Optional.ofNullable(accounts.get(number));
    }

    public Collection<Account> accounts() {
        return List.copyOf(accounts.values());
    }

    /** Applies one transfer, returning its outcome rather than throwing on a business-rule failure. */
    public TransferOutcome process(Transfer transfer) {
        Account from = accounts.get(transfer.from());
        Account to = accounts.get(transfer.to());

        if (from == null) {
            return TransferOutcome.rejected(transfer, "unknown source account " + transfer.from());
        }
        if (to == null) {
            return TransferOutcome.rejected(transfer, "unknown destination account " + transfer.to());
        }
        if (from == to) {
            return TransferOutcome.rejected(transfer, "source and destination are the same account");
        }
        if (!from.canWithdraw(transfer.amount())) {
            return TransferOutcome.rejected(transfer, "insufficient funds in " + from.number());
        }

        from.withdraw(transfer.amount());
        to.deposit(transfer.amount());
        return TransferOutcome.applied(transfer);
    }

    /** Applies transfers in order, so a later transfer sees the balances left by earlier ones. */
    public List<TransferOutcome> processAll(List<Transfer> transfers) {
        List<TransferOutcome> outcomes = new ArrayList<>(transfers.size());
        for (Transfer transfer : transfers) {
            outcomes.add(process(transfer));
        }
        return outcomes;
    }
}
