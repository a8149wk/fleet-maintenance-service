package com.fms.repository;

import com.fms.entity.Workshop;
import com.fms.enums.WorkshopType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkshopRepository extends JpaRepository<Workshop, Long> {

    Optional<Workshop> findByCode(String code);

    boolean existsByCode(String code);

    List<Workshop> findByTypeAndIsActiveTrue(WorkshopType type);

    List<Workshop> findByIsActiveTrue();

    @Query("SELECT w FROM Workshop w WHERE " +
            "(:type IS NULL OR w.type = :type) AND " +
            "(:search = '' OR LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(w.city) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Workshop> search(@Param("type") WorkshopType type, @Param("search") String search, Pageable pageable);
}
