package com.tamv.systema.dto;

import com.tamv.systema.entities.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class UserRegisterRequest {
    private String username;
    private String password;
    private Role role;
}
