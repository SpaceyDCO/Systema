package com.tamv.systema.backend.config;

import com.tamv.systema.backend.entities.Status;
import com.tamv.systema.backend.entities.StatusType;
import com.tamv.systema.backend.repositories.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final StatusRepository statusRepository;
    @Autowired
    public DataInitializer(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }
    @Override
    public void run(String... args) throws Exception {
        if(statusRepository.count() == 0) {
            statusRepository.save(new Status(null, "PENDING", StatusType.REPAIR_ORDER));
            statusRepository.save(new Status(null, "IN PROGRESS", StatusType.REPAIR_ORDER));
            statusRepository.save(new Status(null, "COMPLETED", StatusType.REPAIR_ORDER));
            statusRepository.save(new Status(null, "CANCELLED", StatusType.REPAIR_ORDER));
            statusRepository.save(new Status(null, "UNPAID", StatusType.INVOICE));
            statusRepository.save(new Status(null, "PAID", StatusType.INVOICE));
            statusRepository.save(new Status(null, "OVERDUE", StatusType.INVOICE));
            statusRepository.save(new Status(null, "CANCELLED", StatusType.INVOICE));
            System.out.println("Status data initialized successfully!");
        }
    }
}
