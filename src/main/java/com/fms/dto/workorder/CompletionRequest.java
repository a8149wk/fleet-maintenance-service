package com.fms.dto.workorder;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class CompletionRequest {
    private BigDecimal actualCost;
    private BigDecimal laborCost;
    private BigDecimal partsCost;
    private String notes;
    private Integer odometer;
    private List<WorkOrderItemRequest> items = new ArrayList<>();
    private boolean generateInvoice = true;
}
