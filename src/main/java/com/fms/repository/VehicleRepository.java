package com.fms.repository;

import com.fms.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    boolean existsByLicensePlate(String licensePlate);

    List<Vehicle> findByClientId(Long clientId);

    Page<Vehicle> findByClientId(Long clientId, Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE " +
            "(:clientId IS NULL OR v.client.id = :clientId) AND " +
            "(:search = '' OR LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Vehicle> search(@Param("clientId") Long clientId, @Param("search") String search, Pageable pageable);
}
