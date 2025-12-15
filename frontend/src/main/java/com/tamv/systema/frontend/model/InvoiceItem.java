package com.tamv.systema.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class InvoiceItem {
    private Long id;
    private Invoice invoice;
    private Product product;
    private int quantity;
    private BigDecimal priceAtSale;
    public String getProductName() {
        return product != null ? product.getName() : "N/A";
    }
    public BigDecimal getLineTotal() {
        if(priceAtSale != null && quantity > 0) {
            return priceAtSale.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
}
