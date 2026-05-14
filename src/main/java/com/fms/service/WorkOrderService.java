package com.fms.service;

import com.fms.dto.workorder.CompletionRequest;
import com.fms.dto.workorder.EstimationRequest;
import com.fms.dto.workorder.WorkOrderCreateRequest;
import com.fms.dto.workorder.WorkOrderItemRequest;
import com.fms.entity.*;
import com.fms.enums.ExecutionType;
import com.fms.enums.ItemType;
import com.fms.enums.ServiceType;
import com.fms.enums.WorkOrderStatus;
import com.fms.exception.BusinessException;
import com.fms.exception.ResourceNotFoundException;
import com.fms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderItemRepository itemRepository;
    private final VehicleRepository vehicleRepository;
    private final WorkshopRepository workshopRepository;
    private final MechanicRepository mechanicRepository;
    private final SparePartRepository sparePartRepository;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<WorkOrder> search(WorkOrderStatus status, Long clientId, String search, Pageable pageable) {
        return workOrderRepository.search(status, clientId, search == null ? "" : search, pageable);
    }

    @Transactional(readOnly = true)
    public Page<WorkOrder> findRecent(int size) {
        return workOrderRepository.findRecent(PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @Transactional(readOnly = true)
    public WorkOrder findById(Long id) {
        return workOrderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("WorkOrder", id));
    }

    @Transactional(readOnly = true)
    public long countByStatus(WorkOrderStatus status) {
        return workOrderRepository.countByStatus(status);
    }

    public WorkOrder create(WorkOrderCreateRequest request, User currentUser) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.getVehicleId()));

        Workshop workshop = null;
        if (request.getWorkshopId() != null) {
            workshop = workshopRepository.findById(request.getWorkshopId())
                    .orElseThrow(() -> new ResourceNotFoundException("Workshop", request.getWorkshopId()));
        }

        WorkOrder wo = WorkOrder.builder()
                .workOrderNumber(generateNumber())
                .vehicle(vehicle)
                .client(vehicle.getClient())
                .workshop(workshop)
                .status(WorkOrderStatus.REQUESTED)
                .executionType(ExecutionType.valueOf(request.getExecutionType()))
                .serviceType(ServiceType.valueOf(request.getServiceType()))
                .requestDescription(request.getRequestDescription())
                .currentOdometer(request.getCurrentOdometer())
                .isUrgent(Boolean.TRUE.equals(request.getIsUrgent()))
                .isWarranty(Boolean.TRUE.equals(request.getIsWarranty()))
                .warrantyReference(request.getWarrantyReference())
                .requestedDate(LocalDateTime.now())
                .createdBy(currentUser)
                .build();
        WorkOrder saved = workOrderRepository.save(wo);
        notificationService.create(currentUser, "WORK_ORDER",
                "New Work Order " + saved.getWorkOrderNumber(),
                "A new work order has been created for vehicle " + vehicle.getLicensePlate(),
                "WORK_ORDER", saved.getId());
        return saved;
    }

    public WorkOrder createEstimation(Long id, EstimationRequest request) {
        WorkOrder wo = findById(id);
        if (wo.getStatus() != WorkOrderStatus.REQUESTED && wo.getStatus() != WorkOrderStatus.ESTIMATED) {
            throw new BusinessException("Estimation only allowed when status is REQUESTED or ESTIMATED");
        }
        wo.setDiagnosis(request.getDiagnosis());
        wo.setEstimatedCost(request.getEstimatedCost());
        wo.setLaborCost(request.getLaborCost());
        wo.setPartsCost(request.getPartsCost());
        wo.setEstimatedDate(LocalDateTime.now());

        wo.getItems().clear();
        if (request.getItems() != null) {
            for (WorkOrderItemRequest itemReq : request.getItems()) {
                wo.getItems().add(toItem(wo, itemReq));
            }
        }
        wo.setStatus(WorkOrderStatus.ESTIMATED);
        return workOrderRepository.save(wo);
    }

    public WorkOrder approve(Long id, String notes, User user) {
        WorkOrder wo = findById(id);
        if (wo.getStatus() != WorkOrderStatus.ESTIMATED) {
            throw new BusinessException("Only ESTIMATED work orders can be approved");
        }
        wo.setStatus(WorkOrderStatus.APPROVED);
        wo.setApprovalNotes(notes);
        wo.setApprovedBy(user);
        wo.setApprovedDate(LocalDateTime.now());
        return workOrderRepository.save(wo);
    }

    public WorkOrder reject(Long id, String reason, User user) {
        WorkOrder wo = findById(id);
        if (wo.getStatus() != WorkOrderStatus.ESTIMATED) {
            throw new BusinessException("Only ESTIMATED work orders can be rejected");
        }
        wo.setStatus(WorkOrderStatus.REJECTED);
        wo.setRejectionReason(reason);
        wo.setApprovedBy(user);
        return workOrderRepository.save(wo);
    }

    public WorkOrder assignMechanic(Long id, Long mechanicId) {
        WorkOrder wo = findById(id);
        Mechanic mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic", mechanicId));
        wo.setAssignedMechanic(mechanic);
        return workOrderRepository.save(wo);
    }

    public WorkOrder start(Long id, Long mechanicId, User user) {
        WorkOrder wo = findById(id);
        if (wo.getStatus() != WorkOrderStatus.APPROVED) {
            throw new BusinessException("Only APPROVED work orders can be started");
        }
        if (mechanicId != null) {
            Mechanic mechanic = mechanicRepository.findById(mechanicId)
                    .orElseThrow(() -> new ResourceNotFoundException("Mechanic", mechanicId));
            wo.setAssignedMechanic(mechanic);
        }
        wo.setStatus(WorkOrderStatus.IN_PROGRESS);
        wo.setStartedDate(LocalDateTime.now());
        return workOrderRepository.save(wo);
    }

    public WorkOrder complete(Long id, CompletionRequest request, User user) {
        WorkOrder wo = findById(id);
        if (wo.getStatus() != WorkOrderStatus.IN_PROGRESS) {
            throw new BusinessException("Only IN_PROGRESS work orders can be completed");
        }
        wo.setActualCost(request.getActualCost());
        wo.setLaborCost(request.getLaborCost());
        wo.setPartsCost(request.getPartsCost());
        BigDecimal total = nullSafe(request.getActualCost());
        wo.setTotalAmount(total);
        wo.setStatus(WorkOrderStatus.COMPLETED);
        wo.setCompletedDate(LocalDateTime.now());

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            wo.getItems().clear();
            for (WorkOrderItemRequest itemReq : request.getItems()) {
                WorkOrderItem item = toItem(wo, itemReq);
                wo.getItems().add(item);
                if (Objects.equals(item.getItemType(), ItemType.PART) && item.getSparePart() != null) {
                    inventoryService.consumeBySparePart(item.getSparePart().getId(), item.getQuantity(), wo.getId(), user);
                }
            }
        }

        if (request.getOdometer() != null && wo.getVehicle() != null) {
            wo.getVehicle().setCurrentOdometer(request.getOdometer());
            vehicleRepository.save(wo.getVehicle());
        }

        return workOrderRepository.save(wo);
    }

    public WorkOrder cancel(Long id, String reason, User user) {
        WorkOrder wo = findById(id);
        if (wo.getStatus() == WorkOrderStatus.COMPLETED || wo.getStatus() == WorkOrderStatus.BILLED) {
            throw new BusinessException("Cannot cancel a completed/billed work order");
        }
        inventoryService.releaseReservedStock(id, user);
        wo.setStatus(WorkOrderStatus.CANCELLED);
        wo.setRejectionReason(reason);
        return workOrderRepository.save(wo);
    }

    public void markBilled(Long id) {
        WorkOrder wo = findById(id);
        wo.setStatus(WorkOrderStatus.BILLED);
        workOrderRepository.save(wo);
    }

    public void rate(Long id, Integer rating, String feedback) {
        WorkOrder wo = findById(id);
        wo.setCustomerRating(rating);
        wo.setCustomerFeedback(feedback);
        workOrderRepository.save(wo);
    }

    private String generateNumber() {
        long count = workOrderRepository.count() + 1;
        return String.format("WO-%d-%04d", Year.now().getValue(), count);
    }

    private WorkOrderItem toItem(WorkOrder wo, WorkOrderItemRequest req) {
        SparePart part = null;
        if (req.getSparePartId() != null) {
            part = sparePartRepository.findById(req.getSparePartId()).orElse(null);
        }
        BigDecimal qty = BigDecimal.valueOf(req.getQuantity() == null ? 0 : req.getQuantity());
        BigDecimal unit = req.getUnitPrice() == null ? BigDecimal.ZERO : req.getUnitPrice();
        BigDecimal discount = req.getDiscountAmount() == null ? BigDecimal.ZERO : req.getDiscountAmount();
        BigDecimal subtotal = unit.multiply(qty).subtract(discount);

        return WorkOrderItem.builder()
                .workOrder(wo)
                .itemType(req.getItemType() == null ? ItemType.PART : ItemType.valueOf(req.getItemType()))
                .sparePart(part)
                .serviceName(req.getServiceName())
                .description(req.getDescription())
                .quantity(req.getQuantity() == null ? 1 : req.getQuantity())
                .unitPrice(unit)
                .discountPercent(req.getDiscountPercent() == null ? BigDecimal.ZERO : req.getDiscountPercent())
                .discountAmount(discount)
                .subtotal(subtotal)
                .notes(req.getNotes())
                .build();
    }

    private BigDecimal nullSafe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
