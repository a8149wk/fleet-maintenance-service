package com.fms.controller.api;

import com.fms.dto.workorder.CompletionRequest;
import com.fms.entity.WorkOrder;
import com.fms.entity.WorkOrderDocument;
import com.fms.enums.WorkOrderStatus;
import com.fms.repository.WorkOrderDocumentRepository;
import com.fms.security.SecurityUtils;
import com.fms.service.InvoiceService;
import com.fms.service.WorkOrderService;
import com.fms.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile/workorders")
@RequiredArgsConstructor
public class MobileWorkOrderController {

    private final WorkOrderService workOrderService;
    private final InvoiceService invoiceService;
    private final WorkOrderDocumentRepository documentRepository;
    private final SecurityUtils securityUtils;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<Page<Map<String, Object>>> list(@RequestParam(required = false) WorkOrderStatus status,
                                                          @RequestParam(required = false) String search,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        Page<WorkOrder> p = workOrderService.search(status, null, search,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(p.map(this::summarize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(summarize(workOrderService.findById(id)));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> start(@PathVariable Long id,
                                                     @RequestBody(required = false) Map<String, Object> body) {
        Long mechanicId = body != null && body.get("mechanicId") != null
                ? Long.valueOf(body.get("mechanicId").toString()) : null;
        return ResponseEntity.ok(summarize(workOrderService.start(id, mechanicId, securityUtils.getCurrentUser())));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> complete(@PathVariable Long id,
                                                        @RequestBody CompletionRequest request) {
        WorkOrder wo = workOrderService.complete(id, request, securityUtils.getCurrentUser());
        if (request.isGenerateInvoice()) {
            invoiceService.generateFromWorkOrder(id, securityUtils.getCurrentUser());
        }
        return ResponseEntity.ok(summarize(wo));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable Long id,
                                                      @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        return ResponseEntity.ok(summarize(workOrderService.cancel(id, reason, securityUtils.getCurrentUser())));
    }

    @PostMapping(value = "/{id}/upload-photo", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> uploadPhoto(@PathVariable Long id,
                                                           @RequestParam("photo") MultipartFile file,
                                                           @RequestParam(value = "documentType", defaultValue = "OTHER") String type)
            throws Exception {
        WorkOrder wo = workOrderService.findById(id);
        String path = FileUploadUtil.save(uploadDir, "workorders/" + id, file);
        WorkOrderDocument doc = WorkOrderDocument.builder()
                .workOrder(wo)
                .documentType(type)
                .fileName(file.getOriginalFilename())
                .filePath(path)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .uploadedBy(securityUtils.getCurrentUser())
                .build();
        documentRepository.save(doc);
        return ResponseEntity.ok(Map.of("filePath", path, "id", doc.getId()));
    }

    private Map<String, Object> summarize(WorkOrder wo) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", wo.getId());
        map.put("workOrderNumber", wo.getWorkOrderNumber());
        map.put("status", wo.getStatus());
        map.put("serviceType", wo.getServiceType());
        map.put("executionType", wo.getExecutionType());
        map.put("requestDescription", wo.getRequestDescription());
        map.put("vehicle", wo.getVehicle() == null ? null : Map.of(
                "id", wo.getVehicle().getId(),
                "licensePlate", wo.getVehicle().getLicensePlate(),
                "brand", wo.getVehicle().getBrand(),
                "model", wo.getVehicle().getModel()));
        map.put("client", wo.getClient() == null ? null : Map.of(
                "id", wo.getClient().getId(),
                "name", wo.getClient().getName()));
        map.put("workshop", wo.getWorkshop() == null ? null : Map.of(
                "id", wo.getWorkshop().getId(),
                "name", wo.getWorkshop().getName()));
        map.put("assignedMechanic", wo.getAssignedMechanic() == null ? null : Map.of(
                "id", wo.getAssignedMechanic().getId(),
                "fullName", wo.getAssignedMechanic().getFullName()));
        map.put("estimatedCost", wo.getEstimatedCost());
        map.put("actualCost", wo.getActualCost());
        map.put("totalAmount", wo.getTotalAmount());
        map.put("requestedDate", wo.getRequestedDate());
        map.put("startedDate", wo.getStartedDate());
        map.put("completedDate", wo.getCompletedDate());
        map.put("isUrgent", wo.getIsUrgent());
        return map;
    }
}
