package com.tamv.systema.frontend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal defaultPrice;
    private String category;
}
