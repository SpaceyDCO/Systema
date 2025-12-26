package com.tamv.systema.backend.service;

import com.tamv.systema.backend.dto.RepairOrderCreateRequest;
import com.tamv.systema.backend.entities.Customer;
import com.tamv.systema.backend.entities.RepairOrder;
import com.tamv.systema.backend.entities.Status;
import com.tamv.systema.backend.entities.StatusType;
import com.tamv.systema.backend.repositories.CustomerRepository;
import com.tamv.systema.backend.repositories.RepairOrderRepository;
import com.tamv.systema.backend.repositories.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RepairOrderService {
    private final CustomerRepository customerRepository;
    private final RepairOrderRepository repairOrderRepository;
    private final StatusRepository statusRepository;
    @Autowired
    public RepairOrderService(CustomerRepository customerRepository, RepairOrderRepository repairOrderRepository, StatusRepository statusRepository) {
        this.customerRepository = customerRepository;
        this.repairOrderRepository = repairOrderRepository;
        this.statusRepository = statusRepository;
    }
    public List<RepairOrder> getAllRepairOrders() {
        return this.repairOrderRepository.findAll();
    }
    public RepairOrder getRepairOrderById(Long id) {
        return this.repairOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("RepairOrder not found with ID: " + id));
    }
    @Transactional
    public RepairOrder createRepairOrder(RepairOrderCreateRequest request) {
        Customer customer = this.customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + request.getCustomerId()));
        Status initialStatus = this.statusRepository.findByNameAndType("PENDING", StatusType.REPAIR_ORDER)
                .orElseThrow(() -> new RuntimeException("Initial status 'PENDING' not found. Please make sure default statuses are configured in the database."));
        RepairOrder repairOrder = new RepairOrder();
        repairOrder.setCustomer(customer);
        repairOrder.setEquipmentName(request.getEquipmentName());
        if(request.getSerialNumber() != null && !request.getSerialNumber().isEmpty()) repairOrder.setSerialNumber(request.getSerialNumber());
        repairOrder.setDateReceived(LocalDate.now());
        repairOrder.setStatus(initialStatus);
        repairOrder.setReportedIssue(request.getReportedIssue());
        return this.repairOrderRepository.save(repairOrder);
    }

    @Transactional
    public RepairOrder updateOrderStatus(Long orderId, Long statusId) {
        RepairOrder repairOrder = this.repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Repair order not found with ID: " + orderId));
        Status newStatus = this.statusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Status not found with ID: " + statusId));
        repairOrder.setStatus(newStatus);
        return this.repairOrderRepository.save(repairOrder);
    }
    @Transactional
    public RepairOrder updateOrderNotes(Long orderId, String notes) {
        RepairOrder repairOrder = this.repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Repair order not found with ID: " + orderId));
        if(notes != null && !notes.isEmpty()) repairOrder.setTechnicianNotes(notes);
        return this.repairOrderRepository.save(repairOrder);
    }
    @Transactional
    public void deleteRepairOrder(Long id) {
        RepairOrder repairOrder = this.repairOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RepairOrder not found with id: " + id));
        this.repairOrderRepository.delete(repairOrder);
    }
}
