package com.fms.repository;

import com.fms.entity.SparePart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SparePartRepository extends JpaRepository<SparePart, Long> {

    Optional<SparePart> findByPartNumber(String partNumber);

    boolean existsByPartNumber(String partNumber);

    List<SparePart> findByCategory(String category);

    Page<SparePart> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT p FROM SparePart p WHERE " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.partNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<SparePart> search(@Param("category") String category, @Param("search") String search, Pageable pageable);
}
