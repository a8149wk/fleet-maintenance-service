package com.fms.repository;

import com.fms.entity.Mechanic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MechanicRepository extends JpaRepository<Mechanic, Long> {

    Optional<Mechanic> findByCode(String code);

    boolean existsByCode(String code);

    List<Mechanic> findByWorkshopIdAndIsActiveTrue(Long workshopId);

    List<Mechanic> findByIsActiveTrue();

    @Query("SELECT m FROM Mechanic m WHERE " +
            "(:workshopId IS NULL OR m.workshop.id = :workshopId) AND " +
            "(:search = '' OR LOWER(m.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(m.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Mechanic> search(@Param("workshopId") Long workshopId, @Param("search") String search, Pageable pageable);
}
