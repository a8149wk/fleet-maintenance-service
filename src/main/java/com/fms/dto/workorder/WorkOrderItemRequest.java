package com.fms.dto.workorder;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WorkOrderItemRequest {

    private String itemType;
    private Long sparePartId;
    private String serviceName;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPercent = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private String notes;
}
