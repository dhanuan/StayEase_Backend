package com.hotel.stayease.controller;

import com.hotel.stayease.dto.AuthRequest;
import com.hotel.stayease.dto.AuthResponse;
import com.hotel.stayease.dto.RegisterRequest;
import com.hotel.stayease.model.User;
import com.hotel.stayease.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        User u = authService.register(req.getName(), req.getEmail(), req.getPassword());
        return ResponseEntity.status(201).body(u.getEmail());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}

