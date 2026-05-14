package com.fms.service;

import com.fms.dto.inventory.LowStockAlert;
import com.fms.entity.Inventory;
import com.fms.entity.InventoryLocation;
import com.fms.entity.InventoryMovement;
import com.fms.entity.SparePart;
import com.fms.entity.User;
import com.fms.enums.MovementType;
import com.fms.exception.BusinessException;
import com.fms.exception.ResourceNotFoundException;
import com.fms.repository.InventoryLocationRepository;
import com.fms.repository.InventoryMovementRepository;
import com.fms.repository.InventoryRepository;
import com.fms.repository.SparePartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;
    private final InventoryLocationRepository locationRepository;
    private final SparePartRepository sparePartRepository;

    @Transactional(readOnly = true)
    public Page<Inventory> search(Long locationId, String search, Pageable pageable) {
        return inventoryRepository.search(locationId, search == null ? "" : search, pageable);
    }

    @Transactional(readOnly = true)
    public Inventory findById(Long id) {
        return inventoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inventory", id));
    }

    @Transactional(readOnly = true)
    public List<InventoryLocation> findAllLocations() {
        return locationRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public boolean checkAvailability(Long sparePartId, Long locationId, Integer quantity) {
        return inventoryRepository.findBySparePartIdAndLocationId(sparePartId, locationId)
                .map(inv -> inv.getEffectiveAvailableQuantity() >= quantity)
                .orElse(false);
    }

    public void reserveStock(Long inventoryId, Integer quantity, Long workOrderId, User user) {
        Inventory inv = findById(inventoryId);
        if (inv.getEffectiveAvailableQuantity() < quantity) {
            throw new BusinessException("Insufficient stock for " + inv.getSparePart().getName());
        }
        inv.setReservedQuantity((inv.getReservedQuantity() == null ? 0 : inv.getReservedQuantity()) + quantity);
        inventoryRepository.save(inv);
        recordMovement(inv, MovementType.ADJUSTMENT, quantity, "WORK_ORDER_RESERVE", workOrderId, user);
    }

    public void releaseReservedStock(Long workOrderId, User user) {
        List<InventoryMovement> reservations = movementRepository
                .findByReferenceTypeAndReferenceId("WORK_ORDER_RESERVE", workOrderId);
        for (InventoryMovement m : reservations) {
            Inventory inv = m.getInventory();
            inv.setReservedQuantity(Math.max(0, (inv.getReservedQuantity() == null ? 0 : inv.getReservedQuantity()) - m.getQuantity()));
            inventoryRepository.save(inv);
            recordMovement(inv, MovementType.ADJUSTMENT, m.getQuantity(), "WORK_ORDER_RELEASE", workOrderId, user);
        }
    }

    public void consumeStock(Long inventoryId, Integer quantity, Long workOrderId, User user) {
        Inventory inv = findById(inventoryId);
        if (inv.getQuantity() < quantity) {
            throw new BusinessException("Not enough physical stock: " + inv.getSparePart().getName());
        }
        inv.setQuantity(inv.getQuantity() - quantity);
        if (inv.getReservedQuantity() != null && inv.getReservedQuantity() > 0) {
            inv.setReservedQuantity(Math.max(0, inv.getReservedQuantity() - quantity));
        }
        inventoryRepository.save(inv);
        recordMovement(inv, MovementType.OUT, quantity, "WORK_ORDER", workOrderId, user);
    }

    public void consumeBySparePart(Long sparePartId, Integer quantity, Long workOrderId, User user) {
        List<Inventory> stocks = inventoryRepository.findBySparePartId(sparePartId);
        int remaining = quantity;
        for (Inventory inv : stocks) {
            if (remaining <= 0) break;
            int take = Math.min(remaining, inv.getQuantity() == null ? 0 : inv.getQuantity());
            if (take <= 0) continue;
            consumeStock(inv.getId(), take, workOrderId, user);
            remaining -= take;
        }
        if (remaining > 0) {
            throw new BusinessException("Insufficient total stock for spare part id " + sparePartId);
        }
    }

    public Inventory restock(Long inventoryId, Integer quantity, BigDecimal unitCost, String notes, User user) {
        Inventory inv = findById(inventoryId);
        inv.setQuantity(inv.getQuantity() + quantity);
        if (unitCost != null) inv.setUnitCost(unitCost);
        inv.setLastRestockDate(LocalDateTime.now());
        Inventory saved = inventoryRepository.save(inv);
        InventoryMovement movement = recordMovement(saved, MovementType.IN, quantity, "RESTOCK", null, user);
        movement.setNotes(notes);
        movementRepository.save(movement);
        return saved;
    }

    public Inventory adjustStock(Long inventoryId, Integer newQuantity, String notes, User user) {
        Inventory inv = findById(inventoryId);
        int delta = newQuantity - inv.getQuantity();
        inv.setQuantity(newQuantity);
        Inventory saved = inventoryRepository.save(inv);
        InventoryMovement movement = recordMovement(saved, MovementType.ADJUSTMENT, Math.abs(delta), "MANUAL", null, user);
        movement.setNotes(notes);
        movementRepository.save(movement);
        return saved;
    }

    public void transferStock(Long sparePartId, Long fromLocationId, Long toLocationId, Integer quantity, User user) {
        Inventory source = inventoryRepository.findBySparePartIdAndLocationId(sparePartId, fromLocationId)
                .orElseThrow(() -> new BusinessException("Source stock not found"));
        if (source.getQuantity() < quantity) {
            throw new BusinessException("Insufficient stock at source location");
        }
        source.setQuantity(source.getQuantity() - quantity);
        inventoryRepository.save(source);

        Inventory target = inventoryRepository.findBySparePartIdAndLocationId(sparePartId, toLocationId)
                .orElseGet(() -> {
                    SparePart part = sparePartRepository.findById(sparePartId).orElseThrow();
                    InventoryLocation loc = locationRepository.findById(toLocationId).orElseThrow();
                    Inventory created = Inventory.builder()
                            .sparePart(part)
                            .location(loc)
                            .quantity(0)
                            .unitCost(source.getUnitCost())
                            .build();
                    return inventoryRepository.save(created);
                });
        target.setQuantity(target.getQuantity() + quantity);
        target.setLastRestockDate(LocalDateTime.now());
        inventoryRepository.save(target);

        InventoryMovement out = recordMovement(source, MovementType.TRANSFER, quantity, "TRANSFER_OUT", null, user);
        out.setToLocation(target.getLocation());
        out.setFromLocation(source.getLocation());
        movementRepository.save(out);

        InventoryMovement in = recordMovement(target, MovementType.TRANSFER, quantity, "TRANSFER_IN", null, user);
        in.setFromLocation(source.getLocation());
        in.setToLocation(target.getLocation());
        movementRepository.save(in);
    }

    @Transactional(readOnly = true)
    public List<LowStockAlert> getLowStockAlerts() {
        return inventoryRepository.findLowStockItems().stream().map(inv -> LowStockAlert.builder()
                .inventoryId(inv.getId())
                .partNumber(inv.getSparePart().getPartNumber())
                .partName(inv.getSparePart().getName())
                .locationName(inv.getLocation().getName())
                .currentQuantity(inv.getQuantity())
                .reservedQuantity(inv.getReservedQuantity())
                .availableQuantity(inv.getEffectiveAvailableQuantity())
                .reorderPoint(inv.getReorderPoint())
                .minStockLevel(inv.getMinStockLevel())
                .build()).toList();
    }

    private InventoryMovement recordMovement(Inventory inv, MovementType type, Integer quantity,
                                             String referenceType, Long referenceId, User user) {
        InventoryMovement movement = InventoryMovement.builder()
                .inventory(inv)
                .movementType(type)
                .quantity(quantity)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .unitCost(inv.getUnitCost())
                .createdBy(user)
                .build();
        return movementRepository.save(movement);
    }
}
