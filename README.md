# Mable Back-End Code Challenge — Simple Banking Service

Loads a company's account balances and applies a day's transfers from CSV files,
never allowing an account to be overdrawn, and prints a per-transfer result plus
the final balances.

## Running

Requires JDK 17+.

```bash
# run the tests
mvn test

# run against the provided sample files (data/)
mvn -q exec:java

# or against your own files
mvn -q exec:java -Dexec.args="path/to/balances.csv path/to/transfers.csv"
```

No Maven? Compile and run with the JDK directly:

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out com.mable.banking.App
```

## Design

The code separates three concerns so each is small and independently testable:

- **Model** (`model/Account`, `model/Transfer`, `model/TransferOutcome`) — domain
  objects and value types, with no knowledge of files or I/O.
- **Service** (`service/Bank`) — orchestrates transfers between accounts, enforcing
  business rules (overdraft protection, unknown accounts, self-transfers).
- **I/O** (`io/AccountCsvReader`, `io/TransferCsvReader`) — turns CSV lines into
  domain objects, with no knowledge of banking rules.
- **Application** (`App`) — wires them together and reports.

### Key decisions

- **Money is `BigDecimal`, never `double`.** Currency in floating point silently
  loses cents (`0.1 + 0.2 != 0.3`); `BigDecimal` keeps every amount exact.
- **The overdraft rule lives in the domain.** `Account.withdraw` refuses to drop
  below zero, so the invariant can't be bypassed. Drawing an account down to
  exactly `0` is allowed (only *below* `$0` is forbidden).
- **A transfer is all-or-nothing.** `Bank.process` checks funds *before* touching
  either side, so a rejected transfer never leaves money half-moved.
- **Transfers apply in file order.** A later transfer sees the balances left by
  earlier ones — so a transfer can legitimately spend money an earlier transfer
  just delivered.
- **Trusted vs untrusted input are handled differently.** Balances are our own
  seed data, so `AccountCsvReader` *fails fast* on a bad line rather than silently
  dropping money. The daily transfer file is external, so `TransferCsvReader` is
  *lenient*: it applies the well-formed transfers and reports the malformed lines
  instead of aborting the whole run.
- **Every transfer produces feedback.** Applied or rejected (with a reason:
  unknown account, insufficient funds, self-transfer), so the output is auditable.

### Scope / assumptions

- Single company, single day, in-memory — matching the brief.
- Single-threaded batch processing. If transfers were applied concurrently, the
  check-then-apply step would need per-account locking or a database transaction
  to stay atomic; that's the natural next step for a real system.
- Possible extensions: emit a machine-readable results file, and reconcile the
  applied transfers against an external source of truth.

## Rubric mapping

| Rubric item | Where |
|---|---|
| domain models | `model/Account`, `model/Transfer`, `service/Bank`, `model/TransferOutcome` |
| native data structures, readably | `LinkedHashMap` of accounts, `List` of outcomes |
| tests (JUnit 5), good coverage | `src/test/java` — model + service + I/O + end-to-end |
| tests are orthogonal | one behaviour per test, isolated fixtures |
| tests explain functionality | `@DisplayName` reads as a spec |
| encapsulation | balance is private; only `deposit`/`withdraw` mutate it |
| separation of concerns | model / service / io / application split |
| short, readable methods | each method does one thing |
| runs and provides feedback | `App` prints per-transfer results + final balances |
