package com.tamv.systema.backend.controllers;

import com.tamv.systema.backend.dto.RepairOrderCreateRequest;
import com.tamv.systema.backend.dto.RepairOrderNotesUpdateRequest;
import com.tamv.systema.backend.dto.RepairOrderStatusUpdateRequest;
import com.tamv.systema.backend.entities.RepairOrder;
import com.tamv.systema.backend.service.RepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class RepairOrderController {
    private final RepairOrderService repairOrderService;
    @Autowired
    public RepairOrderController(RepairOrderService repairOrderService) {
        this.repairOrderService = repairOrderService;
    }

    /**
     * Retrieves all the currently registered repair orders
     * @return A list containing the repair orders
     */
    @GetMapping
    public List<RepairOrder> getAllOrders() {
        return this.repairOrderService.getAllRepairOrders();
    }

    /**
     * Retrieves a repair order by its ID
     * @param id The id of the repair order to retrieve
     * @return The order or 404 not found status
     */
    @GetMapping("/{id}")
    public ResponseEntity<RepairOrder> getOrderById(@PathVariable("id") Long id) {
        try {
            RepairOrder order = this.repairOrderService.getRepairOrderById(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/v1/orders
     * Creates a new RepairOrder based on the provided data
     * @param request The DTO containing Customer ID, equipment name, serial number and reported issue
     * @return A response entity containing the newly created RepairOrder with an HTTP status of 201
     * HTTP Status 400 in case of a wrong DTO
     */
    @PostMapping
    public ResponseEntity<RepairOrder> createOrder(@RequestBody RepairOrderCreateRequest request) {
        try {
            RepairOrder order = this.repairOrderService.createRepairOrder(request);
            return new ResponseEntity<>(order, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * PUT /api/v1/orders/{id}/status
     * Updates the status of a given repair order
     * @param id The repair order's id whose status will be updated
     * @param request DTO containing the new status ID
     * @return Response entity containing the updated RepairOrder with an HTTP Status 200
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<RepairOrder> updateOrderStatus(@PathVariable("id") Long id, @RequestBody RepairOrderStatusUpdateRequest request) {
        try {
            RepairOrder repairOrder = this.repairOrderService.updateOrderStatus(id, request.getStatusId());
            return ResponseEntity.ok(repairOrder);
        }catch(RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/v1/orders/{id}/notes
     * Updates the technician notes of a given repair order
     * @param id The repair order's id whose notes will be updated
     * @param request DTO containing the new notes
     * @return Response entity containing the updated RepairOrder with an HTTP Status 200
     */
    @PutMapping("/{id}/notes")
    public ResponseEntity<RepairOrder> updateOrderNotes(@PathVariable("id") Long id, @RequestBody RepairOrderNotesUpdateRequest request) {
        try {
            RepairOrder repairOrder = this.repairOrderService.updateOrderNotes(id, request.getTechnicianNotes());
            return ResponseEntity.ok(repairOrder);
        }catch(RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deletes a Repair Order
     * @param id The repair order to be deleted
     * @return HTTP Status 204 No Content on successful deletion
     * 404 Not Found otherwise
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepairOrder(@PathVariable("id") Long id) {
        try {
            this.repairOrderService.deleteRepairOrder(id);
            return ResponseEntity.noContent().build();
        }catch(RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
