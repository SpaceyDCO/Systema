package com.tamv.systema.backend.controllers;

import com.tamv.systema.backend.entities.Customer;
import com.tamv.systema.backend.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerRepository customerRepository;
    @Autowired
    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * GET /api/v1/customers
     * @return A list containing all customers
     */
    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    /**
     * GET /api/v1/customers/{id}
     * Retrieves a customer by ID
     * @param id The id of the customer to retrieve
     * @return A customer by their ID or 404 Not Found status
     */
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        return customer.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/customers
     * Creates a customer from a json
     * @param customer The customer to be created (json)
     * @return 201 Created status
     */
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        Customer savedCustomer = customerRepository.save(customer);
        return new ResponseEntity<>(savedCustomer, HttpStatus.CREATED);
    }

    /**
     * PUT /api/v1/customers/{id}
     * Updates an existing customer
     * @param id The id of the customer to update
     * @param customerDetails The details for the new customer (json)
     * @return The updated customer or 404 Not Found status
     */
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customerDetails) {
        return customerRepository.findById(id).map(existingCustomer -> {
            existingCustomer.setFullName(customerDetails.getFullName());
            existingCustomer.setEmail(customerDetails.getEmail());
            existingCustomer.setAddress(customerDetails.getAddress());
            existingCustomer.setPhoneNumber(customerDetails.getPhoneNumber());
            Customer updatedCustomer = customerRepository.save(existingCustomer);
            return ResponseEntity.ok(updatedCustomer);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes an existing customer by their ID
     * @param id The id of the customer to delete
     * @return 204 No Content on success, or 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        if(!customerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        customerRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
