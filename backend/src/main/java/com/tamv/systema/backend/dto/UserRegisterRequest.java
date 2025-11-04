package com.tamv.systema.backend.dto;

import com.tamv.systema.backend.entities.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class UserRegisterRequest {
    private String username;
    private String password;
    private Role role;
}
