package com.tamv.systema.backend.controllers;

import com.tamv.systema.backend.dto.InvoiceCreateRequest;
import com.tamv.systema.backend.dto.InvoiceStatusUpdateRequest;
import com.tamv.systema.backend.entities.Invoice;
import com.tamv.systema.backend.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;
    @Autowired
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * GET /api/v1/invoices
     * Retrieves all invoices
     * @return A list containing all registered invoices
     */
    @GetMapping
    public List<Invoice> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    /**
     * Retrieves an Invoice by its ID
     * @param id The id of the Invoice to retrieve
     * @return The invoice or 404 Not Found status
     */
    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable("id") Long id) {
        try {
            Invoice invoice = invoiceService.getInvoiceById(id);
            return ResponseEntity.ok(invoice);
        }catch(RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/v1/invoices
     * Creates a new invoice based on the provided data
     * @param request The DTO containing the customer ID and a list of items to be invoiced
     * @return A ResponseEntity containing the newly created invoice with an HTTP status of 201
     * If no customer or product were found, returns an HTTP Status 400 (bad request)
     */
    @PostMapping
    public ResponseEntity<Invoice> createInvoice(@RequestBody InvoiceCreateRequest request) {
        try {
            Invoice invoice = invoiceService.createInvoice(request);
            return new ResponseEntity<>(invoice, HttpStatus.CREATED);
        }catch(RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Updates the status of an existing invoice
     * @param id The ID of the invoice to update
     * @param request The DTO containing the ID of the new status
     * @return A ResponseEntity containing the updated Invoice with an HTTP Status 200
     * Or HTTP Bad Request if the Invoice was not found
     *
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Invoice> updateInvoiceStatus(@PathVariable("id") Long id, @RequestBody InvoiceStatusUpdateRequest request) {
        try {
            Invoice updatedInvoice = invoiceService.updateInvoiceStatus(id, request.getStatusId());
            return ResponseEntity.ok(updatedInvoice);
        }catch(RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deletes an invoice and all of its associated line items
     * @param id The ID of the invoice to delete
     * @return An HTTP Status 204 (No content) on successful deletion.
     * 404 Not Found status otherwise
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable("id") Long id) {
        try {
            invoiceService.deleteInvoice(id);
            return ResponseEntity.noContent().build();
        }catch(RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
