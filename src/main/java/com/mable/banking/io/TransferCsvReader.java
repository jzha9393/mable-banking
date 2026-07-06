package com.mable.banking.io;

import com.mable.banking.model.Transfer;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a day's transfers ("from,to,amount" per line, no header). The transfer
 * file is external daily input, so this reader is lenient: it keeps the
 * well-formed transfers and reports the malformed lines instead of aborting.
 */
public final class TransferCsvReader {

    /** Well-formed transfers plus a human-readable note for every line that could not be parsed. */
    public record Result(List<Transfer> transfers, List<String> malformed) {}

    public Result read(Path file) throws IOException {
        List<Transfer> transfers = new ArrayList<>();
        List<String> malformed = new ArrayList<>();
        List<String> lines = Files.readAllLines(file);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (line.isEmpty()) {
                continue;
            }
            try {
                transfers.add(parse(line));
            } catch (RuntimeException e) {
                malformed.add("line " + (i + 1) + ": " + e.getMessage());
            }
        }
        return new Result(transfers, malformed);
    }

    private Transfer parse(String line) {
        String[] fields = line.split(",", -1);
        if (fields.length != 3) {
            throw new IllegalArgumentException("expected 'from,to,amount': " + line);
        }
        return new Transfer(fields[0].strip(), fields[1].strip(), new BigDecimal(fields[2].strip()));
    }
}
