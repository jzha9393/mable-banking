package com.mable.banking.io;

import com.mable.banking.model.Account;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a company's opening balances ("number,balance" per line, no header).
 * Balances are our own trusted seed data, so this reader fails fast on a
 * malformed line rather than silently dropping money.
 */
public final class AccountCsvReader {

    public List<Account> read(Path file) throws IOException {
        List<Account> accounts = new ArrayList<>();
        List<String> lines = Files.readAllLines(file);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (!line.isEmpty()) {
                accounts.add(parse(line, i + 1));
            }
        }
        return accounts;
    }

    private Account parse(String line, int lineNumber) {
        String[] fields = line.split(",", -1);
        if (fields.length != 2) {
            throw new IllegalArgumentException("balances line " + lineNumber + " must be 'number,balance': " + line);
        }
        try {
            return new Account(fields[0].strip(), new BigDecimal(fields[1].strip()));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("balances line " + lineNumber + ": " + e.getMessage(), e);
        }
    }
}
