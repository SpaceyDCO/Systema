package com.tamv.systema.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class InvoiceItemRequest {
    private Long productId;
    private int quantity;
}
