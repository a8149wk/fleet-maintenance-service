package com.fms.repository;

import com.fms.entity.WorkOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkOrderItemRepository extends JpaRepository<WorkOrderItem, Long> {
    List<WorkOrderItem> findByWorkOrderId(Long workOrderId);

    void deleteByWorkOrderId(Long workOrderId);
}
