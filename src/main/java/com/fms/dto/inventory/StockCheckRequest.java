package com.fms.dto.inventory;

import lombok.Data;

@Data
public class StockCheckRequest {
    private Long sparePartId;
    private Long locationId;
    private Integer quantity;
}
