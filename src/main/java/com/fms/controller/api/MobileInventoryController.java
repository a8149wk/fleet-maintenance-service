package com.fms.controller.api;

import com.fms.dto.inventory.StockCheckRequest;
import com.fms.entity.Inventory;
import com.fms.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile/inventory")
@RequiredArgsConstructor
public class MobileInventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<Page<Map<String, Object>>> list(@RequestParam(required = false) String search,
                                                          @RequestParam(required = false) Long locationId,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        Page<Inventory> p = inventoryService.search(locationId, search, PageRequest.of(page, size));
        return ResponseEntity.ok(p.map(this::toMap));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(toMap(inventoryService.findById(id)));
    }

    @PostMapping("/check-availability")
    public ResponseEntity<Map<String, Object>> check(@RequestBody StockCheckRequest request) {
        boolean available = inventoryService.checkAvailability(
                request.getSparePartId(), request.getLocationId(), request.getQuantity());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("available", available);
        response.put("requestedQuantity", request.getQuantity());
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toMap(Inventory inv) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", inv.getId());
        map.put("partNumber", inv.getSparePart().getPartNumber());
        map.put("partName", inv.getSparePart().getName());
        map.put("locationName", inv.getLocation().getName());
        map.put("quantity", inv.getQuantity());
        map.put("reservedQuantity", inv.getReservedQuantity());
        map.put("availableQuantity", inv.getEffectiveAvailableQuantity());
        map.put("reorderPoint", inv.getReorderPoint());
        return map;
    }
}
