package com.tamv.systema.backend.controllers;

import com.tamv.systema.backend.dto.PaymentCreateRequest;
import com.tamv.systema.backend.dto.PaymentResponse;
import com.tamv.systema.backend.entities.Payment;
import com.tamv.systema.backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<Payment>> getPaymentsByInvoice(@PathVariable("id") Long invoiceId) {
        try {
            List<Payment> payments = this.paymentService.getPaymentsByInvoice(invoiceId);
            return ResponseEntity.ok(payments);
        }catch(RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Creates a new payment
     * @param request The DTO containing the invoice for which this payment is being made, the amount paid and the payment method used
     * @return A ResponseEntity containing the newly created payment, bad request error otherwise
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentCreateRequest request) {
        try {
            Payment payment = this.paymentService.createPayment(request);
            PaymentResponse response = this.paymentService.createPaymentResponseDTO(payment);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }catch(RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
