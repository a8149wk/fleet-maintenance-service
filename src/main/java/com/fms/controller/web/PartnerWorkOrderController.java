package com.fms.controller.web;

import com.fms.dto.workorder.EstimationRequest;
import com.fms.entity.User;
import com.fms.security.SecurityUtils;
import com.fms.service.SparePartService;
import com.fms.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/partner/workorders")
@RequiredArgsConstructor
public class PartnerWorkOrderController {

    private final WorkOrderService workOrderService;
    private final SparePartService sparePartService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        User user = securityUtils.getCurrentUser();
        model.addAttribute("workOrders", workOrderService.findPartnerWorkOrders(user,
                PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"))));
        model.addAttribute("portalPrefix", "partner");
        model.addAttribute("portalTitle", "Bengkel rekanan");
        return "external/workorders";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        User user = securityUtils.getCurrentUser();
        model.addAttribute("wo", workOrderService.findByIdForPartner(user, id));
        model.addAttribute("portalPrefix", "partner");
        model.addAttribute("portalTitle", "Bengkel rekanan");
        return "external/workorder-detail";
    }

    @GetMapping("/{id}/estimation")
    public String estimationForm(@PathVariable Long id, Model model) {
        User user = securityUtils.getCurrentUser();
        model.addAttribute("wo", workOrderService.findByIdForPartner(user, id));
        model.addAttribute("spareParts", sparePartService.findAll());
        model.addAttribute("estimation", new EstimationRequest());
        model.addAttribute("portalPrefix", "partner");
        return "external/estimation";
    }

    @PostMapping("/{id}/estimation")
    public String createEstimation(@PathVariable Long id,
                                   @ModelAttribute("estimation") EstimationRequest request,
                                   RedirectAttributes attrs) {
        User user = securityUtils.getCurrentUser();
        workOrderService.createEstimation(id, request, user);
        attrs.addFlashAttribute("message", "Estimasi biaya telah dikirim");
        return "redirect:/partner/workorders/" + id;
    }
}
