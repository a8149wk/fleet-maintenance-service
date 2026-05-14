package com.fms.controller.web;

import com.fms.service.ApprovalSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/approval-settings")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminApprovalSettingsController {

    private final ApprovalSettingsService approvalSettingsService;

    @GetMapping
    public String show(Model model) {
        model.addAttribute("approverRolesCsv", approvalSettingsService.getEstimateApproverRolesRaw());
        return "admin/approval-settings";
    }

    @PostMapping
    public String save(@RequestParam("approverRolesCsv") String csv, RedirectAttributes ra) {
        approvalSettingsService.saveEstimateApproverRolesRaw(csv);
        ra.addFlashAttribute("message", "Pengaturan peran persetujuan estimasi telah disimpan.");
        return "redirect:/admin/approval-settings";
    }
}
