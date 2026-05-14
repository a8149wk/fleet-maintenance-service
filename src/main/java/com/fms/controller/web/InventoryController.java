package com.fms.controller.web;

import com.fms.dto.inventory.StockMovementRequest;
import com.fms.entity.Inventory;
import com.fms.security.SecurityUtils;
import com.fms.service.InventoryService;
import com.fms.service.SparePartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final SparePartService sparePartService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String list(@RequestParam(required = false) Long locationId,
                       @RequestParam(required = false) String search,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<Inventory> items = inventoryService.search(locationId, search,
                PageRequest.of(page, 20, Sort.by("id").ascending()));
        model.addAttribute("items", items);
        model.addAttribute("locations", inventoryService.findAllLocations());
        return "inventory/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("inventory", inventoryService.findById(id));
        return "inventory/detail";
    }

    @GetMapping("/movement")
    public String movementForm(Model model) {
        model.addAttribute("movement", new StockMovementRequest());
        model.addAttribute("spareParts", sparePartService.findAll());
        model.addAttribute("locations", inventoryService.findAllLocations());
        return "inventory/stock-movement";
    }

    @PostMapping("/movement")
    public String recordMovement(@ModelAttribute("movement") StockMovementRequest request,
                                 RedirectAttributes attrs) {
        switch (request.getMovementType()) {
            case "IN" -> {
                var inv = inventoryService.search(request.getLocationId(), null,
                                PageRequest.of(0, 1000)).getContent().stream()
                        .filter(i -> i.getSparePart().getId().equals(request.getSparePartId())).findFirst()
                        .orElseThrow();
                inventoryService.restock(inv.getId(), request.getQuantity(), request.getUnitCost(),
                        request.getNotes(), securityUtils.getCurrentUser());
            }
            case "TRANSFER" -> inventoryService.transferStock(request.getSparePartId(),
                    request.getFromLocationId(), request.getToLocationId(),
                    request.getQuantity(), securityUtils.getCurrentUser());
            case "ADJUSTMENT" -> {
                var inv = inventoryService.search(request.getLocationId(), null,
                                PageRequest.of(0, 1000)).getContent().stream()
                        .filter(i -> i.getSparePart().getId().equals(request.getSparePartId())).findFirst()
                        .orElseThrow();
                inventoryService.adjustStock(inv.getId(), request.getQuantity(), request.getNotes(),
                        securityUtils.getCurrentUser());
            }
            default -> throw new IllegalArgumentException("Unknown movement type: " + request.getMovementType());
        }
        attrs.addFlashAttribute("message", "Stock movement recorded");
        return "redirect:/inventory";
    }

    @GetMapping("/low-stock")
    public String lowStock(Model model) {
        model.addAttribute("alerts", inventoryService.getLowStockAlerts());
        return "inventory/reorder-alert";
    }
}
