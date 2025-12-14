package com.tamv.systema.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RepairOrder {
    private Long id;
    private Customer customer;
    private String equipmentName;
    private String serialNumber;
    private LocalDate dateReceived;
    private Status status;
    private String reportedIssue;
    private String technicianNotes;
    public String getCustomerName() {
        return customer != null ? customer.getFullName() : "N/A";
    }
    public String getStatusName() {
        return status != null ? status.getName() : "N/A";
    }
}
