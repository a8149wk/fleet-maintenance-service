package com.fms.controller.web;

import com.fms.service.DashboardService;
import com.fms.service.InventoryService;
import com.fms.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final WorkOrderService workOrderService;
    private final InventoryService inventoryService;

    @GetMapping("/")
    public String index(Model model, Authentication authentication) {
        if (authentication != null) {
            if (hasAuthority(authentication, "ROLE_PARTNER_WORKSHOP")) {
                return "redirect:/partner/workorders";
            }
            if (hasAuthority(authentication, "ROLE_PARTS_SUPPLIER")) {
                return "redirect:/supplier/workorders";
            }
        }
        model.addAttribute("stats", dashboardService.getStatistics());
        model.addAttribute("recentWorkOrders", workOrderService.findRecent(10).getContent());
        model.addAttribute("lowStockAlerts", inventoryService.getLowStockAlerts());
        return "dashboard/index";
    }

    private static boolean hasAuthority(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> role.equals(a.getAuthority()));
    }
}
