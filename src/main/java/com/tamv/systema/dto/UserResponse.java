package com.tamv.systema.dto;

import com.tamv.systema.entities.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @Getter @Setter
public class UserResponse {
    private Long id;
    private String username;
    private Role role;
}
