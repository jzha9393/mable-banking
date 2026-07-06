package com.mable.banking.io;

import com.mable.banking.model.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountCsvReaderTest {

    @TempDir
    Path dir;

    private Path fileWith(String contents) throws IOException {
        Path file = dir.resolve("balances.csv");
        Files.writeString(file, contents);
        return file;
    }

    @Test
    @DisplayName("parses each line into an account with an exact balance")
    void parsesBalances() throws IOException {
        Path file = fileWith("""
                1111234522226789,5000.00
                2222123433331212,550.00
                """);

        List<Account> accounts = new AccountCsvReader().read(file);

        assertEquals(2, accounts.size());
        assertEquals("1111234522226789", accounts.get(0).number());
        assertEquals(0, accounts.get(0).balance().compareTo(new BigDecimal("5000.00")));
        assertEquals(0, accounts.get(1).balance().compareTo(new BigDecimal("550.00")));
    }

    @Test
    @DisplayName("fails fast on a malformed balances line rather than dropping it")
    void failsFastOnMalformedLine() throws IOException {
        Path file = fileWith("1111234522226789,not-a-number\n");
        assertThrows(IllegalArgumentException.class, () -> new AccountCsvReader().read(file));
    }
}
