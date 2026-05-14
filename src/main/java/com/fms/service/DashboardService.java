package com.fms.service;

import com.fms.enums.InvoiceStatus;
import com.fms.enums.WorkOrderStatus;
import com.fms.repository.ClientRepository;
import com.fms.repository.InvoiceRepository;
import com.fms.repository.SparePartRepository;
import com.fms.repository.VehicleRepository;
import com.fms.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ClientRepository clientRepository;
    private final VehicleRepository vehicleRepository;
    private final WorkOrderRepository workOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final SparePartRepository sparePartRepository;

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalClients", clientRepository.count());
        stats.put("totalVehicles", vehicleRepository.count());
        stats.put("totalSpareParts", sparePartRepository.count());

        long requested = countByStatus(WorkOrderStatus.REQUESTED);
        long estimated = countByStatus(WorkOrderStatus.ESTIMATED);
        long approved = countByStatus(WorkOrderStatus.APPROVED);
        long inProgress = countByStatus(WorkOrderStatus.IN_PROGRESS);
        long completed = countByStatus(WorkOrderStatus.COMPLETED);
        long billed = countByStatus(WorkOrderStatus.BILLED);

        stats.put("woRequested", requested);
        stats.put("woEstimated", estimated);
        stats.put("woApproved", approved);
        stats.put("woInProgress", inProgress);
        stats.put("woCompleted", completed);
        stats.put("woBilled", billed);
        stats.put("activeWorkOrders", requested + estimated + approved + inProgress);

        long pendingInvoices = countInvoiceByStatus(InvoiceStatus.SENT) +
                countInvoiceByStatus(InvoiceStatus.PARTIAL_PAID) +
                countInvoiceByStatus(InvoiceStatus.OVERDUE);
        stats.put("pendingInvoices", pendingInvoices);
        stats.put("paidInvoices", countInvoiceByStatus(InvoiceStatus.PAID));

        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        BigDecimal revenue = invoiceRepository.sumRevenueByDateRange(start, now);
        stats.put("revenueThisMonth", revenue == null ? BigDecimal.ZERO : revenue);
        return stats;
    }

    private long countByStatus(WorkOrderStatus status) {
        Long c = workOrderRepository.countByStatus(status);
        return c == null ? 0L : c;
    }

    private long countInvoiceByStatus(InvoiceStatus status) {
        Long c = invoiceRepository.countByStatus(status);
        return c == null ? 0L : c;
    }
}
