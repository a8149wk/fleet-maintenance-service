package com.fms.repository;

import com.fms.entity.WorkOrderSupplierUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkOrderSupplierUserRepository extends JpaRepository<WorkOrderSupplierUser, Long> {

    List<WorkOrderSupplierUser> findByWorkOrderId(Long workOrderId);

    boolean existsByWorkOrderIdAndUserId(Long workOrderId, Long userId);

    void deleteByWorkOrderId(Long workOrderId);
}
