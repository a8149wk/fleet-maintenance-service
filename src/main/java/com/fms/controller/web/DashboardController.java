package com.fms.controller.web;

import com.fms.service.DashboardService;
import com.fms.service.InventoryService;
import com.fms.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
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
    public String index(Model model) {
        model.addAttribute("stats", dashboardService.getStatistics());
        model.addAttribute("recentWorkOrders", workOrderService.findRecent(10).getContent());
        model.addAttribute("lowStockAlerts", inventoryService.getLowStockAlerts());
        return "dashboard/index";
    }
}
