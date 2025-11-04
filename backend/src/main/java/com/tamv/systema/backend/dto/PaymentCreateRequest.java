package com.tamv.systema.backend.dto;

import com.tamv.systema.backend.entities.PaymentMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter @Getter @NoArgsConstructor
public class PaymentCreateRequest {
    private Long invoiceId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
}
