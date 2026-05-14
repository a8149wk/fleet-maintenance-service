package com.fms.entity;

import com.fms.enums.EstimateApprovalStatus;
import com.fms.enums.ExecutionType;
import com.fms.enums.ServiceType;
import com.fms.enums.WorkOrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "work_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_order_number", nullable = false, unique = true, length = 30)
    private String workOrderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_id")
    private Workshop workshop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_mechanic_id")
    private Mechanic assignedMechanic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkOrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_type", nullable = false, length = 30)
    private ExecutionType executionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    private ServiceType serviceType;

    @Column(name = "request_description", nullable = false, columnDefinition = "TEXT")
    private String requestDescription;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "current_odometer")
    private Integer currentOdometer;

    @Column(name = "estimated_cost", precision = 15, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "actual_cost", precision = 15, scale = 2)
    private BigDecimal actualCost;

    @Column(name = "labor_cost", precision = 15, scale = 2)
    private BigDecimal laborCost;

    @Column(name = "parts_cost", precision = 15, scale = 2)
    private BigDecimal partsCost;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "requested_date")
    private LocalDateTime requestedDate;

    @Column(name = "estimated_date")
    private LocalDateTime estimatedDate;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "started_date")
    private LocalDateTime startedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "is_urgent")
    @Builder.Default
    private Boolean isUrgent = false;

    @Column(name = "is_warranty")
    @Builder.Default
    private Boolean isWarranty = false;

    @Column(name = "warranty_reference", length = 100)
    private String warrantyReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approval_notes", columnDefinition = "TEXT")
    private String approvalNotes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "estimate_approval_status", nullable = false, length = 30)
    @Builder.Default
    private EstimateApprovalStatus estimateApprovalStatus = EstimateApprovalStatus.NOT_REQUIRED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimate_submitted_by")
    private User estimateSubmittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimate_reviewed_by")
    private User estimateReviewedBy;

    @Column(name = "estimate_reviewed_at")
    private LocalDateTime estimateReviewedAt;

    /** Internal reviewer comment when approving or rejecting an external estimate. */
    @Column(name = "estimate_approval_remark", columnDefinition = "TEXT")
    private String estimateApprovalRemark;

    @Column(name = "customer_rating")
    private Integer customerRating;

    @Column(name = "customer_feedback", columnDefinition = "TEXT")
    private String customerFeedback;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkOrderSupplierUser> supplierUsers = new ArrayList<>();

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkOrderItem> items = new ArrayList<>();
}
