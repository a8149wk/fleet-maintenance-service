package com.fms.repository;

import com.fms.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findBySparePartId(Long sparePartId);

    List<Inventory> findByLocationId(Long locationId);

    Optional<Inventory> findBySparePartIdAndLocationId(Long sparePartId, Long locationId);

    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.reorderPoint")
    List<Inventory> findLowStockItems();

    @Query("SELECT i FROM Inventory i WHERE " +
            "(:locationId IS NULL OR i.location.id = :locationId) AND " +
            "(:search IS NULL OR LOWER(i.sparePart.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(i.sparePart.partNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Inventory> search(@Param("locationId") Long locationId,
                           @Param("search") String search,
                           Pageable pageable);
}
