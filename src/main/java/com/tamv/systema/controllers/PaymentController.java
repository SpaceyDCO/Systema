package com.tamv.systema.controllers;

import com.tamv.systema.entities.Payment;
import com.tamv.systema.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;
    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Retrieves all the payments made to an invoice, if any
     * @param invoiceId The Invoice id whose payments will be retrieved
     * @return A list containing all payments made, empty list if no payments were made
     */
    @GetMapping("/invoice/{id}")
    public ResponseEntity<List<Payment>> getPaymentsToInvoice(@PathVariable Long invoiceId) {
        try {
            List<Payment> payments = this.paymentService.getPaymentsByInvoice(invoiceId);
            return ResponseEntity.ok(payments);
        }catch(RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
