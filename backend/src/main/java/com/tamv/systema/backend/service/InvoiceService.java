package com.tamv.systema.backend.service;

import com.tamv.systema.backend.dto.InvoiceCreateRequest;
import com.tamv.systema.backend.dto.InvoiceItemRequest;
import com.tamv.systema.backend.entities.*;
import com.tamv.systema.backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final StatusRepository statusRepository;
    @Autowired
    public InvoiceService(InvoiceRepository invoiceRepository, CustomerRepository customerRepository, ProductRepository productRepository, InvoiceItemRepository invoiceItemRepository, StatusRepository statusRepository) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.statusRepository = statusRepository;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }
    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id).orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
    }
    @Transactional
    public Invoice updateInvoiceStatus(Long invoiceId, Long newStatusId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + invoiceId));
        Status status = statusRepository.findById(newStatusId)
                .orElseThrow(() -> new RuntimeException("Status not found with id: " + newStatusId));
        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }
    @Transactional
    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id " + id));
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(id);
        invoiceItemRepository.deleteAll(items);
        invoiceRepository.delete(invoice);
    }
    @Transactional
    public Invoice createInvoice(InvoiceCreateRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + request.getCustomerId()));
        Status initialStatus = statusRepository.findByNameAndType("DRAFT", StatusType.INVOICE)
                .orElseThrow(() -> new RuntimeException("Initial status 'DRAFT' not found. Please make sure default statuses are configured in the database."));
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setStatus(initialStatus);
        invoice.setInvoiceDate(LocalDate.now());
        if(request.getDueDate() != null) {
            invoice.setDueDate(request.getDueDate());
        }else invoice.setDueDate(LocalDate.now().plusDays(30));
        Invoice savedInvoice = invoiceRepository.save(invoice);
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<InvoiceItem> itemsToSave = new ArrayList<>();
        for(InvoiceItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemRequest.getProductId()));
            InvoiceItem newItem = new InvoiceItem();
            newItem.setInvoice(savedInvoice);
            newItem.setProduct(product);
            newItem.setQuantity(itemRequest.getQuantity());
            newItem.setPriceAtSale(product.getDefaultPrice());
            BigDecimal itemSubTotal = product.getDefaultPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemSubTotal);
            itemsToSave.add(newItem);
        }
        invoiceItemRepository.saveAll(itemsToSave);
        savedInvoice.setTotalAmount(totalAmount);
        return invoiceRepository.save(savedInvoice);
    }
}
