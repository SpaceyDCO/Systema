package com.tamv.systema.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class InvoiceCreateRequest {
    private Long customerId;
    private List<InvoiceItemRequest> items;
    private LocalDate dueDate;
}
