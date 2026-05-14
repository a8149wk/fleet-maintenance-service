package com.fms.service;

import com.fms.dto.workorder.CompletionRequest;
import com.fms.dto.workorder.EstimationRequest;
import com.fms.dto.workorder.WorkOrderCreateRequest;
import com.fms.dto.workorder.WorkOrderItemRequest;
import com.fms.entity.*;
import com.fms.enums.EstimateApprovalStatus;
import com.fms.enums.ExecutionType;
import com.fms.enums.ItemType;
import com.fms.enums.ServiceType;
import com.fms.enums.WorkOrderStatus;
import com.fms.enums.WorkshopType;
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
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderService {

    private static final String ROLE_PARTNER = "ROLE_PARTNER_WORKSHOP";
    private static final String ROLE_SUPPLIER = "ROLE_PARTS_SUPPLIER";

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderItemRepository itemRepository;
    private final VehicleRepository vehicleRepository;
    private final WorkshopRepository workshopRepository;
    private final MechanicRepository mechanicRepository;
    private final SparePartRepository sparePartRepository;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final UserWorkshopRepository userWorkshopRepository;
    private final WorkOrderSupplierUserRepository workOrderSupplierUserRepository;
    private final ApprovalSettingsService approvalSettingsService;

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

    @Transactional(readOnly = true)
    public Page<WorkOrder> findPartnerWorkOrders(User user, Pageable pageable) {
        return workOrderRepository.findVisibleForPartner(user.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<WorkOrder> findSupplierWorkOrders(User user, Pageable pageable) {
        return workOrderRepository.findVisibleForSupplier(user.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public WorkOrder findByIdForPartner(User user, Long id) {
        WorkOrder wo = findById(id);
        if (!canPartnerAccessWorkOrder(user, wo)) {
            throw new BusinessException("Access denied");
        }
        return wo;
    }

    @Transactional(readOnly = true)
    public WorkOrder findByIdForSupplier(User user, Long id) {
        WorkOrder wo = findById(id);
        if (!canSupplierAccessWorkOrder(user, wo)) {
            throw new BusinessException("Access denied");
        }
        return wo;
    }

    @Transactional(readOnly = true)
    public boolean canPartnerAccessWorkOrder(User user, WorkOrder wo) {
        return user != null && wo.getWorkshop() != null
                && userWorkshopRepository.existsByUserIdAndWorkshopId(user.getId(), wo.getWorkshop().getId())
                && hasRole(user, ROLE_PARTNER);
    }

    @Transactional(readOnly = true)
    public boolean canSupplierAccessWorkOrder(User user, WorkOrder wo) {
        return user != null && hasRole(user, ROLE_SUPPLIER)
                && workOrderSupplierUserRepository.existsByWorkOrderIdAndUserId(wo.getId(), user.getId());
    }

    @Transactional(readOnly = true)
    public long countPendingEstimateApprovals() {
        return workOrderRepository.countByStatusAndEstimateApprovalStatus(
                WorkOrderStatus.ESTIMATED, EstimateApprovalStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public Page<WorkOrder> findPendingEstimateApprovals(Pageable pageable) {
        return workOrderRepository.findPendingEstimateApprovals(
                WorkOrderStatus.ESTIMATED, EstimateApprovalStatus.PENDING, pageable);
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
                .estimateApprovalStatus(EstimateApprovalStatus.NOT_REQUIRED)
                .build();
        WorkOrder saved = workOrderRepository.save(wo);
        notificationService.create(currentUser, "WORK_ORDER",
                "New Work Order " + saved.getWorkOrderNumber(),
                "A new work order has been created for vehicle " + vehicle.getLicensePlate(),
                "WORK_ORDER", saved.getId());
        notifyPartnerUsersForNewWorkOrder(saved);
        return saved;
    }

    private void notifyPartnerUsersForNewWorkOrder(WorkOrder wo) {
        if (wo.getWorkshop() == null) {
            return;
        }
        Workshop ws = workshopRepository.findById(wo.getWorkshop().getId()).orElse(null);
        if (ws == null || ws.getType() != WorkshopType.PARTNER) {
            return;
        }
        for (UserWorkshop uw : userWorkshopRepository.findByWorkshopId(ws.getId())) {
            notificationService.create(uw.getUser(), "WORK_ORDER_ASSIGNED",
                    "Work order " + wo.getWorkOrderNumber(),
                    "A new work order has been assigned to workshop " + ws.getName() + ".",
                    "WORK_ORDER", wo.getId());
        }
    }

    public WorkOrder createEstimation(Long id, EstimationRequest request, User user) {
        if (user == null) {
            throw new BusinessException("You must be signed in to submit an estimate");
        }
        WorkOrder wo = findById(id);
        if (wo.getStatus() != WorkOrderStatus.REQUESTED && wo.getStatus() != WorkOrderStatus.ESTIMATED) {
            throw new BusinessException("Estimation only allowed when status is REQUESTED or ESTIMATED");
        }

        boolean external = isExternalPartyForWorkOrder(user, wo);
        if (external) {
            if (wo.getStatus() == WorkOrderStatus.ESTIMATED
                    && wo.getEstimateApprovalStatus() == EstimateApprovalStatus.APPROVED) {
                throw new BusinessException("This estimate has already been approved. Contact the fleet team to request changes.");
            }
            if (wo.getStatus() == WorkOrderStatus.ESTIMATED
                    && wo.getEstimateApprovalStatus() == EstimateApprovalStatus.PENDING) {
                throw new BusinessException("Your estimate is still awaiting internal approval");
            }
            applyEstimationPayload(wo, request);
            wo.setStatus(WorkOrderStatus.ESTIMATED);
            wo.setEstimateApprovalStatus(EstimateApprovalStatus.PENDING);
            wo.setEstimateSubmittedBy(user);
            wo.setEstimateReviewedBy(null);
            wo.setEstimateReviewedAt(null);
            wo.setEstimateApprovalRemark(null);
            WorkOrder saved = workOrderRepository.save(wo);
            notifyInternalApproversEstimatePending(saved);
            return saved;
        }

        if (!isInternalEstimator(user)) {
            throw new BusinessException("You are not allowed to submit an internal estimate for this work order");
        }
        applyEstimationPayload(wo, request);
        wo.setStatus(WorkOrderStatus.ESTIMATED);
        wo.setEstimateApprovalStatus(EstimateApprovalStatus.NOT_REQUIRED);
        wo.setEstimateSubmittedBy(null);
        wo.setEstimateReviewedBy(null);
        wo.setEstimateReviewedAt(null);
        wo.setEstimateApprovalRemark(null);
        return workOrderRepository.save(wo);
    }

    public WorkOrder approveEstimate(Long id, User reviewer, String notes) {
        if (!approvalSettingsService.userMayApproveExternalEstimates(reviewer)) {
            throw new BusinessException("You are not allowed to approve external estimates");
        }
        WorkOrder wo = findById(id);
        if (wo.getStatus() != WorkOrderStatus.ESTIMATED
                || wo.getEstimateApprovalStatus() != EstimateApprovalStatus.PENDING) {
            throw new BusinessException("No pending external estimate to approve");
        }
        wo.setEstimateApprovalStatus(EstimateApprovalStatus.APPROVED);
        wo.setEstimateReviewedBy(reviewer);
        wo.setEstimateReviewedAt(LocalDateTime.now());
        wo.setEstimateApprovalRemark(notes);
        WorkOrder saved = workOrderRepository.save(wo);
        if (saved.getEstimateSubmittedBy() != null) {
            notificationService.create(saved.getEstimateSubmittedBy(), "ESTIMATE_APPROVED",
                    "Estimate approved: " + saved.getWorkOrderNumber(),
                    "Your cost estimate for work order " + saved.getWorkOrderNumber() + " has been approved.",
                    "WORK_ORDER", saved.getId());
        }
        return saved;
    }

    public WorkOrder rejectEstimate(Long id, User reviewer, String reason) {
        if (!approvalSettingsService.userMayApproveExternalEstimates(reviewer)) {
            throw new BusinessException("You are not allowed to reject external estimates");
        }
        WorkOrder wo = findById(id);
        if (wo.getStatus() != WorkOrderStatus.ESTIMATED
                || wo.getEstimateApprovalStatus() != EstimateApprovalStatus.PENDING) {
            throw new BusinessException("No pending external estimate to reject");
        }
        wo.setEstimateApprovalStatus(EstimateApprovalStatus.REJECTED);
        wo.setEstimateReviewedBy(reviewer);
        wo.setEstimateReviewedAt(LocalDateTime.now());
        wo.setEstimateApprovalRemark(reason);
        WorkOrder saved = workOrderRepository.save(wo);
        if (saved.getEstimateSubmittedBy() != null) {
            notificationService.create(saved.getEstimateSubmittedBy(), "ESTIMATE_REJECTED",
                    "Estimate needs revision: " + saved.getWorkOrderNumber(),
                    "Your estimate was rejected. Reason: " + (reason == null ? "(none)" : reason),
                    "WORK_ORDER", saved.getId());
        }
        return saved;
    }

    public void assignSupplierUsers(Long workOrderId, List<Long> supplierUserIds, User actor) {
        if (!canAssignSuppliers(actor)) {
            throw new BusinessException("Only administrators or managers can assign parts suppliers");
        }
        WorkOrder wo = findById(workOrderId);
        workOrderSupplierUserRepository.deleteByWorkOrderId(workOrderId);
        if (CollectionUtils.isEmpty(supplierUserIds)) {
            return;
        }
        for (Long uid : supplierUserIds) {
            User u = userRepository.findById(uid)
                    .orElseThrow(() -> new ResourceNotFoundException("User", uid));
            if (!hasRole(u, ROLE_SUPPLIER)) {
                throw new BusinessException("User " + u.getUsername() + " is not a parts supplier account");
            }
            WorkOrderSupplierUser link = WorkOrderSupplierUser.builder()
                    .workOrder(wo)
                    .user(u)
                    .build();
            workOrderSupplierUserRepository.save(link);
            notificationService.create(u, "WORK_ORDER_ASSIGNED",
                    "Work order " + wo.getWorkOrderNumber(),
                    "You have been assigned as parts supplier for this work order. Please review and submit an estimate.",
                    "WORK_ORDER", wo.getId());
        }
    }

    @Transactional(readOnly = true)
    public List<User> findActiveSupplierUsers() {
        return userRepository.findActiveByRoleName(ROLE_SUPPLIER);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderSupplierUser> listSupplierAssignments(Long workOrderId) {
        return workOrderSupplierUserRepository.findByWorkOrderId(workOrderId);
    }

    public WorkOrder approve(Long id, String notes, User user) {
        WorkOrder wo = findById(id);
        if (wo.getStatus() != WorkOrderStatus.ESTIMATED) {
            throw new BusinessException("Only ESTIMATED work orders can be approved");
        }
        if (wo.getEstimateApprovalStatus() == EstimateApprovalStatus.PENDING) {
            throw new BusinessException("This work order is waiting for internal approval of the external estimate. Use the Estimate approvals page.");
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
        if (wo.getEstimateApprovalStatus() == EstimateApprovalStatus.PENDING) {
            throw new BusinessException("An external estimate is awaiting internal approval. Reject the estimate first from the Estimate approvals page.");
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

    private void applyEstimationPayload(WorkOrder wo, EstimationRequest request) {
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
    }

    private void notifyInternalApproversEstimatePending(WorkOrder wo) {
        List<String> roleNames = approvalSettingsService.getEstimateApproverRoleNames();
        List<User> recipients = userRepository.findAllActiveHavingAnyRole(roleNames);
        for (User recipient : recipients) {
            notificationService.create(recipient, "ESTIMATE_APPROVAL_REQUIRED",
                    "Approve estimate: " + wo.getWorkOrderNumber(),
                    "Partner/supplier submitted an estimate for work order " + wo.getWorkOrderNumber() + ".",
                    "WORK_ORDER", wo.getId());
        }
    }

    private boolean isExternalPartyForWorkOrder(User user, WorkOrder wo) {
        return canPartnerAccessWorkOrder(user, wo) || canSupplierAccessWorkOrder(user, wo);
    }

    private boolean isInternalEstimator(User user) {
        return hasRole(user, "ROLE_ADMIN") || hasRole(user, "ROLE_MANAGER") || hasRole(user, "ROLE_MECHANIC");
    }

    private boolean canAssignSuppliers(User user) {
        return hasRole(user, "ROLE_ADMIN") || hasRole(user, "ROLE_MANAGER");
    }

    private boolean hasRole(User user, String roleName) {
        if (user == null || user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream().anyMatch(r -> roleName.equals(r.getName()));
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
