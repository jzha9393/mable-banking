package com.mable.banking.model;

/** The result of attempting a transfer, with enough detail to report back to the user. */
public record TransferOutcome(Transfer transfer, Status status, String detail) {

    public enum Status { APPLIED, REJECTED }

    public static TransferOutcome applied(Transfer transfer) {
        return new TransferOutcome(transfer, Status.APPLIED, "applied");
    }

    public static TransferOutcome rejected(Transfer transfer, String reason) {
        return new TransferOutcome(transfer, Status.REJECTED, reason);
    }

    public boolean isApplied() {
        return status == Status.APPLIED;
    }
}
