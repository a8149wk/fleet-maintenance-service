package com.fms.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockAlert {
    private Long inventoryId;
    private String partNumber;
    private String partName;
    private String locationName;
    private Integer currentQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer reorderPoint;
    private Integer minStockLevel;
}
