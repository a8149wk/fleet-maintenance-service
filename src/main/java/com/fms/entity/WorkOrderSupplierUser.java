package com.fms.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Assigns a {@code ROLE_PARTS_SUPPLIER} user to a specific work order so
 * they can submit a parts/labour estimate even when no workshop is set.
 */
@Entity
@Table(name = "work_order_supplier_users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"work_order_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderSupplierUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
