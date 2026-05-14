package com.fms.controller.web;

import com.fms.entity.User;
import com.fms.security.SecurityUtils;
import com.fms.service.ApprovalSettingsService;
import com.fms.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/approvals/estimates")
@RequiredArgsConstructor
public class EstimateApprovalController {

    private final WorkOrderService workOrderService;
    private final SecurityUtils securityUtils;
    private final ApprovalSettingsService approvalSettingsService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        User user = securityUtils.getCurrentUser();
        if (!approvalSettingsService.userMayApproveExternalEstimates(user)) {
            model.addAttribute("error", "Akun Anda tidak memiliki izin untuk menyetujui estimasi eksternal.");
            model.addAttribute("workOrders", Page.empty());
            return "approvals/estimate-list";
        }
        model.addAttribute("workOrders", workOrderService.findPendingEstimateApprovals(
                PageRequest.of(page, 25, Sort.by(Sort.Direction.ASC, "createdAt"))));
        return "approvals/estimate-list";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id,
                          @RequestParam(required = false) String notes,
                          RedirectAttributes attrs) {
        User user = securityUtils.getCurrentUser();
        workOrderService.approveEstimate(id, user, notes);
        attrs.addFlashAttribute("message", "Estimasi disetujui");
        return "redirect:/approvals/estimates";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam String reason,
                         RedirectAttributes attrs) {
        User user = securityUtils.getCurrentUser();
        workOrderService.rejectEstimate(id, user, reason);
        attrs.addFlashAttribute("message", "Estimasi ditolak");
        return "redirect:/approvals/estimates";
    }
}
