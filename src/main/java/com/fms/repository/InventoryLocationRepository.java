package com.fms.repository;

import com.fms.entity.InventoryLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryLocationRepository extends JpaRepository<InventoryLocation, Long> {
    Optional<InventoryLocation> findByCode(String code);

    List<InventoryLocation> findByIsActiveTrue();
}
