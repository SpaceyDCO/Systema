package com.tamv.systema.controllers;

import com.tamv.systema.dto.UserRegisterRequest;
import com.tamv.systema.dto.UserResponse;
import com.tamv.systema.entities.User;
import com.tamv.systema.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserRegisterRequest request) {
        try {
            User user = this.userService.registerNewUser(request);
            UserResponse response = this.userService.convertToResponseDTO(user);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }catch(RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
