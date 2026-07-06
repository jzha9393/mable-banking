package com.mable.banking.io;

import com.mable.banking.model.Transfer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TransferCsvReaderTest {

    @TempDir
    Path dir;

    private Path fileWith(String contents) throws IOException {
        Path file = dir.resolve("transfers.csv");
        Files.writeString(file, contents);
        return file;
    }

    @Test
    @DisplayName("parses well-formed transfers")
    void parsesTransfers() throws IOException {
        Path file = fileWith("1111234522226789,1212343433335665,500.00\n");

        TransferCsvReader.Result result = new TransferCsvReader().read(file);

        assertEquals(1, result.transfers().size());
        assertTrue(result.malformed().isEmpty());
        Transfer transfer = result.transfers().get(0);
        assertEquals("1111234522226789", transfer.from());
        assertEquals("1212343433335665", transfer.to());
    }

    @Test
    @DisplayName("reports a malformed line but keeps the good transfers")
    void reportsMalformedAndKeepsGood() throws IOException {
        Path file = fileWith("""
                1111234522226789,1212343433335665,500.00
                garbage-line
                3212343433335755,2222123433331212,1000.00
                """);

        TransferCsvReader.Result result = new TransferCsvReader().read(file);

        assertEquals(2, result.transfers().size());
        assertEquals(1, result.malformed().size());
        assertTrue(result.malformed().get(0).contains("line 2"));
    }
}
