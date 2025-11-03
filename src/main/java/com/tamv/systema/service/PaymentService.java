package com.tamv.systema.service;

import com.tamv.systema.dto.PaymentCreateRequest;
import com.tamv.systema.entities.Invoice;
import com.tamv.systema.entities.Payment;
import com.tamv.systema.entities.Status;
import com.tamv.systema.entities.StatusType;
import com.tamv.systema.repositories.InvoiceRepository;
import com.tamv.systema.repositories.PaymentRepository;
import com.tamv.systema.repositories.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentService {
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final StatusRepository statusRepository;
    @Autowired
    public PaymentService(InvoiceRepository invoiceRepository, PaymentRepository paymentRepository, StatusRepository statusRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.statusRepository = statusRepository;
    }

    public List<Payment> getPaymentsByInvoice(Long invoiceId) {
        this.invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));
        return this.paymentRepository.findByInvoiceId(invoiceId);
    }
    public BigDecimal calculateTotalPaidForInvoice(Long invoiceId) {
        List<Payment> payments = getPaymentsByInvoice(invoiceId);
        return payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public Payment createPayment(PaymentCreateRequest request) {
        Invoice invoice = this.invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + request.getInvoiceId()));
        Status paidStatus = this.statusRepository.findByNameAndType("PAID", StatusType.INVOICE)
                .orElseThrow(() -> new RuntimeException("PAID status not found... Please make sure default statuses are configured in the database."));
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAmount(request.getAmount());
        Payment savedPayment = this.paymentRepository.save(payment);
        BigDecimal totalPaid = calculateTotalPaidForInvoice(request.getInvoiceId());
        if(totalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus(paidStatus);
            this.invoiceRepository.save(invoice);
        }
        return savedPayment;
    }
}
