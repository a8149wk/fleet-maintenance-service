package com.fms.repository;

import com.fms.entity.InventoryMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByInventoryId(Long inventoryId);

    Page<InventoryMovement> findByInventoryId(Long inventoryId, Pageable pageable);

    List<InventoryMovement> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
}
