package com.tamv.systema.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Invoice {
    private Long id;
    private Customer customer;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private Status status;
    public String getCustomerName() {
        return customer != null ? customer.getFullName() : "N/A";
    }
    public String getStatusName() {
        return status != null ? status.getName() : "N/A";
    }
}
