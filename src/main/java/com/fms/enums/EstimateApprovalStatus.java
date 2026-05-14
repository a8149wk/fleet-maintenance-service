package com.fms.enums;

/**
 * Tracks whether a cost estimate on a work order was produced by
 * internal staff (no extra gate) or by an external partner / parts
 * supplier (must be approved by configured internal roles before the
 * client can approve the overall work order).
 */
public enum EstimateApprovalStatus {
    /** Internal estimator (admin/manager/mechanic) — client may approve WO when ESTIMATED. */
    NOT_REQUIRED,
    /** External party submitted figures — waiting for internal review. */
    PENDING,
    /** Internal reviewer accepted the external estimate. */
    APPROVED,
    /** Internal reviewer rejected; external party may revise and resubmit. */
    REJECTED
}
