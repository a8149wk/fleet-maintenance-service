package com.fms.repository;

import com.fms.entity.WorkOrderDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkOrderDocumentRepository extends JpaRepository<WorkOrderDocument, Long> {
    List<WorkOrderDocument> findByWorkOrderId(Long workOrderId);
}
