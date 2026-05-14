package com.fms.dto.inventory;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockMovementRequest {
    private Long sparePartId;
    private Long locationId;
    private Long fromLocationId;
    private Long toLocationId;
    private String movementType;
    private Integer quantity;
    private BigDecimal unitCost;
    private String notes;
}
