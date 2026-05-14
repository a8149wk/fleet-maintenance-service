package com.fms.dto.workorder;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class EstimationRequest {
    private String diagnosis;
    private BigDecimal estimatedCost;
    private BigDecimal laborCost;
    private BigDecimal partsCost;
    private List<WorkOrderItemRequest> items = new ArrayList<>();
}
