package com.tamv.systema.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class InvoiceItemRequest {
    private Long productId;
    private int quantity;
}
