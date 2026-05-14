package com.fms.dto.invoice;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentRequest {

    @NotNull
    private BigDecimal amount;

    @NotNull
    private String paymentMethod;

    private LocalDate paymentDate;

    private String referenceNumber;

    private String bankName;

    private String accountNumber;

    private String notes;
}
