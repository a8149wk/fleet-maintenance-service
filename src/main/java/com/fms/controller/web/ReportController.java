package com.fms.controller.web;

import com.fms.entity.Invoice;
import com.fms.entity.WorkOrder;
import com.fms.repository.InvoiceRepository;
import com.fms.repository.WorkOrderRepository;
import com.fms.service.InventoryService;
import com.fms.service.InvoiceService;
import com.fms.util.DateUtils;
import com.fms.util.ExcelExporter;
import com.fms.util.NumberUtils;
import com.fms.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final WorkOrderRepository workOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final InventoryService inventoryService;

    @GetMapping
    public String index() {
        return "report/index";
    }

    @GetMapping("/workorders")
    public String workOrderReport(@RequestParam(required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                  @RequestParam(required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                  Model model) {
        LocalDate s = startDate == null ? LocalDate.now().withDayOfMonth(1) : startDate;
        LocalDate e = endDate == null ? LocalDate.now() : endDate;
        List<WorkOrder> data = workOrderRepository.findByDateRange(s.atStartOfDay(), e.atTime(23, 59, 59));
        model.addAttribute("workOrders", data);
        model.addAttribute("startDate", s);
        model.addAttribute("endDate", e);
        return "report/workorder-report";
    }

    @GetMapping("/inventory")
    public String inventoryReport(Model model) {
        model.addAttribute("alerts", inventoryService.getLowStockAlerts());
        return "report/inventory-report";
    }

    @GetMapping("/financial")
    public String financialReport(@RequestParam(required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                  @RequestParam(required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                  Model model) {
        LocalDate s = startDate == null ? LocalDate.now().withDayOfMonth(1) : startDate;
        LocalDate e = endDate == null ? LocalDate.now() : endDate;
        model.addAttribute("startDate", s);
        model.addAttribute("endDate", e);
        model.addAttribute("revenue", invoiceService.getRevenue(s, e));
        model.addAttribute("invoices", invoiceRepository.findAll());
        return "report/financial-report";
    }

    @GetMapping("/{type}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable String type) {
        byte[] bytes;
        switch (type) {
            case "workorders" -> {
                List<WorkOrder> rows = workOrderRepository.findByDateRange(
                        LocalDateTime.now().minusMonths(1), LocalDateTime.now());
                bytes = PdfGenerator.simpleTable("Work Order Report",
                        List.of("WO#", "Vehicle", "Client", "Status", "Date", "Amount"),
                        rows.stream().map(w -> List.of(
                                w.getWorkOrderNumber(),
                                w.getVehicle().getLicensePlate(),
                                w.getClient().getName(),
                                w.getStatus().name(),
                                DateUtils.format(w.getRequestedDate()),
                                NumberUtils.formatRupiah(
                                        w.getTotalAmount() == null ? w.getEstimatedCost() : w.getTotalAmount())
                        )).toList());
            }
            case "invoices" -> {
                List<Invoice> rows = invoiceRepository.findAll();
                bytes = PdfGenerator.simpleTable("Invoice Report",
                        List.of("Invoice#", "Client", "Date", "Due", "Total", "Status"),
                        rows.stream().map(i -> List.of(
                                i.getInvoiceNumber(),
                                i.getClient().getName(),
                                DateUtils.format(i.getInvoiceDate()),
                                DateUtils.format(i.getDueDate()),
                                NumberUtils.formatRupiah(i.getTotalAmount()),
                                i.getStatus().name()
                        )).toList());
            }
            default -> throw new IllegalArgumentException("Unknown report type: " + type);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", type + "-report.pdf");
        return new ResponseEntity<>(bytes, headers, 200);
    }

    @GetMapping("/{type}/export/excel")
    public ResponseEntity<byte[]> exportExcel(@PathVariable String type) throws Exception {
        byte[] bytes;
        switch (type) {
            case "workorders" -> {
                List<WorkOrder> rows = workOrderRepository.findByDateRange(
                        LocalDateTime.now().minusMonths(1), LocalDateTime.now());
                bytes = ExcelExporter.simple("WorkOrders",
                        List.of("WO#", "Vehicle", "Client", "Status", "Date", "Amount"),
                        rows.stream().map(w -> List.of(
                                w.getWorkOrderNumber(),
                                w.getVehicle().getLicensePlate(),
                                w.getClient().getName(),
                                w.getStatus().name(),
                                DateUtils.format(w.getRequestedDate()),
                                NumberUtils.formatRupiah(
                                        w.getTotalAmount() == null ? w.getEstimatedCost() : w.getTotalAmount())
                        )).toList());
            }
            case "invoices" -> {
                List<Invoice> rows = invoiceRepository.findAll();
                bytes = ExcelExporter.simple("Invoices",
                        List.of("Invoice#", "Client", "Date", "Due", "Total", "Status"),
                        rows.stream().map(i -> List.of(
                                i.getInvoiceNumber(),
                                i.getClient().getName(),
                                DateUtils.format(i.getInvoiceDate()),
                                DateUtils.format(i.getDueDate()),
                                NumberUtils.formatRupiah(i.getTotalAmount()),
                                i.getStatus().name()
                        )).toList());
            }
            default -> throw new IllegalArgumentException("Unknown report type: " + type);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", type + "-report.xlsx");
        return new ResponseEntity<>(bytes, headers, 200);
    }
}
