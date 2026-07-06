package com.mable.banking;

import com.mable.banking.io.AccountCsvReader;
import com.mable.banking.io.TransferCsvReader;
import com.mable.banking.model.Account;
import com.mable.banking.model.TransferOutcome;
import com.mable.banking.service.Bank;

import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/** Command-line entry point: load balances, apply a day's transfers, print feedback. */
public final class App {

    public static void main(String[] args) throws IOException {
        Path balancesFile = Path.of(args.length > 0 ? args[0] : "data/mable_account_balances.csv");
        Path transfersFile = Path.of(args.length > 1 ? args[1] : "data/mable_transactions.csv");

        Bank bank = new Bank(new AccountCsvReader().read(balancesFile));
        TransferCsvReader.Result input = new TransferCsvReader().read(transfersFile);
        List<TransferOutcome> outcomes = bank.processAll(input.transfers());

        report(outcomes, input.malformed(), bank);
    }

    /** Prints a summary of applied/rejected transfers, any malformed lines, and final balances. */
    private static void report(List<TransferOutcome> outcomes, List<String> malformed, Bank bank) {
        System.out.println("Transfers");
        System.out.println("---------");
        for (TransferOutcome outcome : outcomes) {
            System.out.printf("  [%s] %s -> %s %s%s%n",
                    outcome.isApplied() ? "APPLIED " : "REJECTED",
                    outcome.transfer().from(),
                    outcome.transfer().to(),
                    outcome.transfer().amount().toPlainString(),
                    outcome.isApplied() ? "" : "  (" + outcome.detail() + ")");
        }

        if (!malformed.isEmpty()) {
            System.out.println("\nMalformed lines (skipped)");
            System.out.println("-------------------------");
            malformed.forEach(m -> System.out.println("  " + m));
        }

        System.out.println("\nFinal balances");
        System.out.println("--------------");
        bank.accounts().stream()
                .sorted(Comparator.comparing(Account::number))
                .forEach(a -> System.out.printf("  %s  %s%n",
                        a.number(), a.balance().setScale(2, RoundingMode.UNNECESSARY).toPlainString()));

        long applied = outcomes.stream().filter(TransferOutcome::isApplied).count();
        System.out.printf("%n%d of %d transfers applied.%n", applied, outcomes.size());
    }
}
