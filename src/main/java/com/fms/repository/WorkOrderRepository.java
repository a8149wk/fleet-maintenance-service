package com.fms.repository;

import com.fms.entity.WorkOrder;
import com.fms.enums.EstimateApprovalStatus;
import com.fms.enums.WorkOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    Optional<WorkOrder> findByWorkOrderNumber(String workOrderNumber);

    Page<WorkOrder> findByStatus(WorkOrderStatus status, Pageable pageable);

    @Query(value = "SELECT DISTINCT w FROM WorkOrder w " +
            "LEFT JOIN FETCH w.vehicle " +
            "LEFT JOIN FETCH w.client " +
            "WHERE (:status IS NULL OR w.status = :status) AND " +
            "(:clientId IS NULL OR w.client.id = :clientId) AND " +
            "(:search = '' OR LOWER(w.workOrderNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(w.vehicle.licensePlate) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(w.client.name) LIKE LOWER(CONCAT('%', :search, '%')))",
            countQuery = "SELECT COUNT(w) FROM WorkOrder w WHERE " +
                    "(:status IS NULL OR w.status = :status) AND " +
                    "(:clientId IS NULL OR w.client.id = :clientId) AND " +
                    "(:search = '' OR LOWER(w.workOrderNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "OR LOWER(w.vehicle.licensePlate) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "OR LOWER(w.client.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<WorkOrder> search(@Param("status") WorkOrderStatus status,
                           @Param("clientId") Long clientId,
                           @Param("search") String search,
                           Pageable pageable);

    List<WorkOrder> findByClientId(Long clientId);

    List<WorkOrder> findByClientIdAndStatusIn(Long clientId, List<WorkOrderStatus> statuses);

    List<WorkOrder> findByAssignedMechanicIdAndStatus(Long mechanicId, WorkOrderStatus status);

    List<WorkOrder> findByVehicleId(Long vehicleId);

    @Query("SELECT COUNT(w) FROM WorkOrder w WHERE w.status = :status")
    Long countByStatus(@Param("status") WorkOrderStatus status);

    @Query("SELECT w FROM WorkOrder w WHERE w.requestedDate BETWEEN :startDate AND :endDate")
    List<WorkOrder> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT w FROM WorkOrder w " +
            "LEFT JOIN FETCH w.vehicle " +
            "LEFT JOIN FETCH w.client " +
            "ORDER BY w.createdAt DESC",
            countQuery = "SELECT COUNT(w) FROM WorkOrder w")
    Page<WorkOrder> findRecent(Pageable pageable);

    long countByStatusAndEstimateApprovalStatus(WorkOrderStatus status, EstimateApprovalStatus estimateApproval);

    @Query("SELECT DISTINCT w FROM WorkOrder w " +
            "LEFT JOIN FETCH w.vehicle v " +
            "LEFT JOIN FETCH w.client c " +
            "LEFT JOIN FETCH w.workshop ws " +
            "WHERE w.workshop IS NOT NULL AND EXISTS (" +
            "  SELECT 1 FROM UserWorkshop uw WHERE uw.user.id = :userId AND uw.workshop.id = w.workshop.id" +
            ") ORDER BY w.createdAt DESC")
    Page<WorkOrder> findVisibleForPartner(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT DISTINCT w FROM WorkOrder w " +
            "LEFT JOIN FETCH w.vehicle v " +
            "LEFT JOIN FETCH w.client c " +
            "LEFT JOIN FETCH w.workshop ws " +
            "WHERE EXISTS (" +
            "  SELECT 1 FROM WorkOrderSupplierUser su WHERE su.workOrder.id = w.id AND su.user.id = :userId" +
            ") ORDER BY w.createdAt DESC")
    Page<WorkOrder> findVisibleForSupplier(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT w FROM WorkOrder w WHERE w.status = :st AND w.estimateApprovalStatus = :eas ORDER BY w.createdAt ASC")
    Page<WorkOrder> findPendingEstimateApprovals(@Param("st") WorkOrderStatus st,
                                                @Param("eas") EstimateApprovalStatus eas,
                                                Pageable pageable);
}
