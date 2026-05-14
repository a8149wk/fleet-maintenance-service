package com.fms.repository;

import com.fms.entity.Invoice;
import com.fms.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByWorkOrderId(Long workOrderId);

    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    List<Invoice> findByClientId(Long clientId);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :today AND i.status IN ('SENT', 'PARTIAL_PAID')")
    List<Invoice> findOverdue(@Param("today") LocalDate today);

    @Query("SELECT i FROM Invoice i WHERE " +
            "(:status IS NULL OR i.status = :status) AND " +
            "(:clientId IS NULL OR i.client.id = :clientId) AND " +
            "(:search IS NULL OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(i.client.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Invoice> search(@Param("status") InvoiceStatus status,
                         @Param("clientId") Long clientId,
                         @Param("search") String search,
                         Pageable pageable);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    Long countByStatus(@Param("status") InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.status = 'PAID' AND i.invoiceDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumRevenueByDateRange(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
}
