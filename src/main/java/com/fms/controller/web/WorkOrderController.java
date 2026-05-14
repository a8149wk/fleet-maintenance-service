package com.fms.controller.web;

import com.fms.dto.workorder.CompletionRequest;
import com.fms.dto.workorder.EstimationRequest;
import com.fms.dto.workorder.WorkOrderCreateRequest;
import com.fms.entity.User;
import com.fms.entity.WorkOrder;
import com.fms.entity.WorkOrderSupplierUser;
import com.fms.enums.ExecutionType;
import com.fms.enums.ServiceType;
import com.fms.enums.WorkOrderStatus;
import com.fms.security.SecurityUtils;
import com.fms.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/workorders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final VehicleService vehicleService;
    private final WorkshopService workshopService;
    private final MechanicService mechanicService;
    private final SparePartService sparePartService;
    private final InvoiceService invoiceService;
    private final SecurityUtils securityUtils;
    private final ApprovalSettingsService approvalSettingsService;

    @GetMapping
    public String list(@RequestParam(required = false) WorkOrderStatus status,
                       @RequestParam(required = false) String search,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<WorkOrder> workOrders = workOrderService.search(status, null, search,
                PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt")));
        model.addAttribute("workOrders", workOrders);
        model.addAttribute("statuses", WorkOrderStatus.values());
        return "workorder/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("workOrder", new WorkOrderCreateRequest());
        prepareForm(model);
        return "workorder/create";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("workOrder") WorkOrderCreateRequest request,
                         BindingResult br, Model model, RedirectAttributes attrs) {
        if (br.hasErrors()) {
            prepareForm(model);
            return "workorder/create";
        }
        WorkOrder wo = workOrderService.create(request, securityUtils.getCurrentUser());
        attrs.addFlashAttribute("message", "Work order " + wo.getWorkOrderNumber() + " created");
        return "redirect:/workorders/" + wo.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        WorkOrder wo = workOrderService.findById(id);
        model.addAttribute("wo", wo);
        model.addAttribute("mechanics", mechanicService.findActive());
        model.addAttribute("invoice", invoiceService.findByWorkOrder(id).orElse(null));
        User me = securityUtils.getCurrentUser();
        model.addAttribute("canApproveExternalEstimate",
                me != null && approvalSettingsService.userMayApproveExternalEstimates(me));
        List<WorkOrderSupplierUser> supplierAssignments = workOrderService.listSupplierAssignments(id);
        model.addAttribute("supplierAssignments", supplierAssignments);
        model.addAttribute("supplierUserOptions", workOrderService.findActiveSupplierUsers());
        model.addAttribute("supplierAssignedIds", supplierAssignments.stream()
                .map(su -> su.getUser().getId())
                .collect(Collectors.toSet()));
        return "workorder/detail";
    }

    @GetMapping("/{id}/estimation")
    public String estimationForm(@PathVariable Long id, Model model) {
        model.addAttribute("wo", workOrderService.findById(id));
        model.addAttribute("spareParts", sparePartService.findAll());
        model.addAttribute("estimation", new EstimationRequest());
        return "workorder/estimation";
    }

    @PostMapping("/{id}/estimation")
    public String createEstimation(@PathVariable Long id,
                                   @ModelAttribute("estimation") EstimationRequest request,
                                   RedirectAttributes attrs) {
        workOrderService.createEstimation(id, request, securityUtils.getCurrentUser());
        attrs.addFlashAttribute("message", "Estimation submitted");
        return "redirect:/workorders/" + id;
    }

    @PostMapping("/{id}/supplier-assign")
    public String assignSuppliers(@PathVariable Long id,
                                  @RequestParam(required = false) List<Long> supplierUserIds,
                                  RedirectAttributes attrs) {
        workOrderService.assignSupplierUsers(id,
                supplierUserIds == null ? Collections.emptyList() : supplierUserIds,
                securityUtils.getCurrentUser());
        attrs.addFlashAttribute("message", "Penyedia spare part diperbarui");
        return "redirect:/workorders/" + id;
    }

    @PostMapping("/{id}/approve-estimate")
    public String approveEstimate(@PathVariable Long id,
                                  @RequestParam(required = false) String notes,
                                  RedirectAttributes attrs) {
        workOrderService.approveEstimate(id, securityUtils.getCurrentUser(), notes);
        attrs.addFlashAttribute("message", "Estimasi eksternal disetujui");
        return "redirect:/workorders/" + id;
    }

    @PostMapping("/{id}/reject-estimate")
    public String rejectEstimate(@PathVariable Long id,
                                 @RequestParam String reason,
                                 RedirectAttributes attrs) {
        workOrderService.rejectEstimate(id, securityUtils.getCurrentUser(), reason);
        attrs.addFlashAttribute("message", "Estimasi eksternal ditolak");
        return "redirect:/workorders/" + id;
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, @RequestParam(required = false) String notes,
                          RedirectAttributes attrs) {
        workOrderService.approve(id, notes, securityUtils.getCurrentUser());
        attrs.addFlashAttribute("message", "Work order approved");
        return "redirect:/workorders/" + id;
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam String reason,
                         RedirectAttributes attrs) {
        workOrderService.reject(id, reason, securityUtils.getCurrentUser());
        attrs.addFlashAttribute("message", "Work order rejected");
        return "redirect:/workorders/" + id;
    }

    @PostMapping("/{id}/assign")
    public String assignMechanic(@PathVariable Long id, @RequestParam Long mechanicId,
                                 RedirectAttributes attrs) {
        workOrderService.assignMechanic(id, mechanicId);
        attrs.addFlashAttribute("message", "Mechanic assigned");
        return "redirect:/workorders/" + id;
    }

    @PostMapping("/{id}/start")
    public String start(@PathVariable Long id,
                        @RequestParam(required = false) Long mechanicId,
                        RedirectAttributes attrs) {
        workOrderService.start(id, mechanicId, securityUtils.getCurrentUser());
        attrs.addFlashAttribute("message", "Work order started");
        return "redirect:/workorders/" + id;
    }

    @GetMapping("/{id}/complete")
    public String completeForm(@PathVariable Long id, Model model) {
        model.addAttribute("wo", workOrderService.findById(id));
        model.addAttribute("completion", new CompletionRequest());
        model.addAttribute("spareParts", sparePartService.findAll());
        return "workorder/complete";
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id,
                           @ModelAttribute("completion") CompletionRequest request,
                           RedirectAttributes attrs) {
        workOrderService.complete(id, request, securityUtils.getCurrentUser());
        if (request.isGenerateInvoice()) {
            invoiceService.generateFromWorkOrder(id, securityUtils.getCurrentUser());
        }
        attrs.addFlashAttribute("message", "Work order completed");
        return "redirect:/workorders/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, @RequestParam(required = false) String reason,
                         RedirectAttributes attrs) {
        workOrderService.cancel(id, reason, securityUtils.getCurrentUser());
        attrs.addFlashAttribute("message", "Work order cancelled");
        return "redirect:/workorders/" + id;
    }

    private void prepareForm(Model model) {
        model.addAttribute("vehicles", vehicleService.search(null, null, PageRequest.of(0, 200)).getContent());
        model.addAttribute("workshops", workshopService.findActive());
        model.addAttribute("serviceTypes", ServiceType.values());
        model.addAttribute("executionTypes", ExecutionType.values());
    }
}
