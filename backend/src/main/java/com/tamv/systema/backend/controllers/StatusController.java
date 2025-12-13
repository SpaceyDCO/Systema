package com.tamv.systema.backend.controllers;

import com.tamv.systema.backend.entities.Status;
import com.tamv.systema.backend.repositories.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/statuses")
public class StatusController {
    private final StatusRepository statusRepository;
    @Autowired
    public StatusController(StatusRepository repository) {
        this.statusRepository = repository;
    }
    @GetMapping
    public List<Status> getAllStatuses() {
        return this.statusRepository.findAll();
    }
}
