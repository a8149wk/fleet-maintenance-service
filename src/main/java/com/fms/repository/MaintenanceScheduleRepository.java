package com.fms.repository;

import com.fms.entity.MaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, Long> {

    List<MaintenanceSchedule> findByVehicleId(Long vehicleId);

    @Query("SELECT m FROM MaintenanceSchedule m WHERE m.isActive = true AND m.nextServiceDate <= :date")
    List<MaintenanceSchedule> findDue(@Param("date") LocalDate date);
}
