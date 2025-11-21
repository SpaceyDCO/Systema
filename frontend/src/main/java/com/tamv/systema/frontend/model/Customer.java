package com.tamv.systema.frontend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter @NoArgsConstructor
public class Customer {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
}
