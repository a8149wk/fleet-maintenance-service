package com.fms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_part_id", nullable = false)
    private SparePart sparePart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private InventoryLocation location;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "reserved_quantity")
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "available_quantity", insertable = false, updatable = false)
    private Integer availableQuantity;

    @Column(name = "min_stock_level")
    @Builder.Default
    private Integer minStockLevel = 0;

    @Column(name = "max_stock_level")
    @Builder.Default
    private Integer maxStockLevel = 0;

    @Column(name = "reorder_point")
    @Builder.Default
    private Integer reorderPoint = 0;

    @Column(name = "unit_cost", precision = 15, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "total_value", insertable = false, updatable = false, precision = 15, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "serial_number", length = 50)
    private String serialNumber;

    @Column(name = "last_restock_date")
    private LocalDateTime lastRestockDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    public Integer getEffectiveAvailableQuantity() {
        int q = quantity == null ? 0 : quantity;
        int r = reservedQuantity == null ? 0 : reservedQuantity;
        return q - r;
    }
}
