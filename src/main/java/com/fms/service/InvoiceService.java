package com.fms.service;

import com.fms.dto.invoice.PaymentRequest;
import com.fms.entity.Invoice;
import com.fms.entity.Payment;
import com.fms.entity.SystemSetting;
import com.fms.entity.User;
import com.fms.entity.WorkOrder;
import com.fms.enums.InvoiceStatus;
import com.fms.enums.PaymentMethod;
import com.fms.exception.BusinessException;
import com.fms.exception.ResourceNotFoundException;
import com.fms.repository.InvoiceRepository;
import com.fms.repository.PaymentRepository;
import com.fms.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final SystemSettingRepository settingRepository;
    private final WorkOrderService workOrderService;

    @Transactional(readOnly = true)
    public Page<Invoice> search(InvoiceStatus status, Long clientId, String search, Pageable pageable) {
        return invoiceRepository.search(status, clientId, search == null ? "" : search, pageable);
    }

    @Transactional(readOnly = true)
    public Invoice findById(Long id) {
        return invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }

    @Transactional(readOnly = true)
    public Optional<Invoice> findByWorkOrder(Long workOrderId) {
        return invoiceRepository.findByWorkOrderId(workOrderId);
    }

    public Invoice generateFromWorkOrder(Long workOrderId, User user) {
        Optional<Invoice> existing = invoiceRepository.findByWorkOrderId(workOrderId);
        if (existing.isPresent()) {
            return existing.get();
        }
        WorkOrder wo = workOrderService.findById(workOrderId);

        BigDecimal subtotal = wo.getTotalAmount() != null ? wo.getTotalAmount() :
                nullSafe(wo.getActualCost(), wo.getEstimatedCost());

        BigDecimal taxRate = getTaxRate();
        BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(taxAmount);

        int paymentTerm = wo.getClient().getPaymentTerm() == null ? 30 : wo.getClient().getPaymentTerm();
        LocalDate today = LocalDate.now();

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateNumber())
                .workOrder(wo)
                .client(wo.getClient())
                .invoiceDate(today)
                .dueDate(today.plusDays(paymentTerm))
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(total)
                .paidAmount(BigDecimal.ZERO)
                .balance(total)
                .status(InvoiceStatus.DRAFT)
                .createdBy(user)
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        workOrderService.markBilled(workOrderId);
        return saved;
    }

    public Invoice send(Long invoiceId) {
        Invoice invoice = findById(invoiceId);
        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            invoice.setStatus(InvoiceStatus.SENT);
        }
        return invoiceRepository.save(invoice);
    }

    public Payment recordPayment(Long invoiceId, PaymentRequest request, User user) {
        Invoice invoice = findById(invoiceId);
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException("Cannot record payment on a cancelled invoice");
        }
        BigDecimal amount = request.getAmount() == null ? BigDecimal.ZERO : request.getAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Payment amount must be greater than zero");
        }

        Payment payment = Payment.builder()
                .paymentNumber(generatePaymentNumber())
                .invoice(invoice)
                .client(invoice.getClient())
                .paymentDate(request.getPaymentDate() == null ? LocalDate.now() : request.getPaymentDate())
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .amount(amount)
                .referenceNumber(request.getReferenceNumber())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .notes(request.getNotes())
                .createdBy(user)
                .build();
        Payment saved = paymentRepository.save(payment);

        BigDecimal newPaid = invoice.getPaidAmount().add(amount);
        BigDecimal balance = invoice.getTotalAmount().subtract(newPaid);
        invoice.setPaidAmount(newPaid);
        invoice.setBalance(balance);
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaymentDate(saved.getPaymentDate());
            invoice.setPaymentMethod(saved.getPaymentMethod());
            invoice.setPaymentReference(saved.getReferenceNumber());
        } else if (newPaid.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(InvoiceStatus.PARTIAL_PAID);
        }
        invoiceRepository.save(invoice);
        return saved;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void checkOverdueInvoices() {
        List<Invoice> overdue = invoiceRepository.findOverdue(LocalDate.now());
        for (Invoice i : overdue) {
            i.setStatus(InvoiceStatus.OVERDUE);
        }
        invoiceRepository.saveAll(overdue);
        log.info("Marked {} invoices as OVERDUE", overdue.size());
    }

    @Transactional(readOnly = true)
    public BigDecimal getRevenue(LocalDate start, LocalDate end) {
        BigDecimal v = invoiceRepository.sumRevenueByDateRange(start, end);
        return v == null ? BigDecimal.ZERO : v;
    }

    @Transactional(readOnly = true)
    public long countByStatus(InvoiceStatus status) {
        Long c = invoiceRepository.countByStatus(status);
        return c == null ? 0L : c;
    }

    private BigDecimal getTaxRate() {
        return settingRepository.findBySettingKey("tax_percentage")
                .map(SystemSetting::getSettingValue)
                .map(v -> new BigDecimal(v).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                .orElse(new BigDecimal("0.11"));
    }

    private BigDecimal nullSafe(BigDecimal a, BigDecimal b) {
        if (a != null) return a;
        if (b != null) return b;
        return BigDecimal.ZERO;
    }

    private String generateNumber() {
        long count = invoiceRepository.count() + 1;
        return String.format("INV-%d-%04d", Year.now().getValue(), count);
    }

    private String generatePaymentNumber() {
        long count = paymentRepository.count() + 1;
        return String.format("PAY-%d-%04d", Year.now().getValue(), count);
    }
}
