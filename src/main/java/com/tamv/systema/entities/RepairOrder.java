package com.tamv.systema.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity @Table(name = "RepairOrders")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
public class RepairOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "repair_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @Column(name = "equipment_name")
    private String equipmentName;
    @Column(name = "serial_number")
    private String serialNumber;
    @Column(name = "date_received")
    private LocalDate dateReceived;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;
    @Column(name = "reported_issue", columnDefinition = "TEXT")
    private String reportedIssue;
    @Column(name = "technician_notes", columnDefinition = "TEXT")
    private String technicianNotes;
}
