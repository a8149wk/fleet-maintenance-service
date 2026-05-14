package com.fms.controller.web;

import com.fms.dto.invoice.PaymentRequest;
import com.fms.entity.Invoice;
import com.fms.enums.InvoiceStatus;
import com.fms.enums.PaymentMethod;
import com.fms.repository.PaymentRepository;
import com.fms.security.SecurityUtils;
import com.fms.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PaymentRepository paymentRepository;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String list(@RequestParam(required = false) InvoiceStatus status,
                       @RequestParam(required = false) Long clientId,
                       @RequestParam(required = false) String search,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<Invoice> invoices = invoiceService.search(status, clientId, search,
                PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "invoiceDate")));
        model.addAttribute("invoices", invoices);
        model.addAttribute("statuses", InvoiceStatus.values());
        return "invoice/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceService.findById(id);
        model.addAttribute("invoice", invoice);
        model.addAttribute("payments", paymentRepository.findByInvoiceId(id));
        return "invoice/detail";
    }

    @PostMapping("/{id}/send")
    public String send(@PathVariable Long id, RedirectAttributes attrs) {
        invoiceService.send(id);
        attrs.addFlashAttribute("message", "Invoice sent");
        return "redirect:/invoices/" + id;
    }

    @GetMapping("/{id}/payment")
    public String paymentForm(@PathVariable Long id, Model model) {
        model.addAttribute("invoice", invoiceService.findById(id));
        model.addAttribute("payment", new PaymentRequest());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "invoice/payment";
    }

    @PostMapping("/{id}/payment")
    public String recordPayment(@PathVariable Long id,
                                @ModelAttribute("payment") PaymentRequest request,
                                RedirectAttributes attrs) {
        invoiceService.recordPayment(id, request, securityUtils.getCurrentUser());
        attrs.addFlashAttribute("message", "Payment recorded");
        return "redirect:/invoices/" + id;
    }
}
