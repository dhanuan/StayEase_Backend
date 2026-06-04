package com.hotel.stayease.controller;

import com.hotel.stayease.dto.AuthRequest;
import com.hotel.stayease.dto.AuthResponse;
import com.hotel.stayease.dto.RegisterAdminRequest;
import com.hotel.stayease.dto.RegisterRequest;
import com.hotel.stayease.model.User;
import com.hotel.stayease.security.TokenBlacklist;
import com.hotel.stayease.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenBlacklist tokenBlacklist;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        User u = authService.register(req.getName(), req.getEmail(), req.getPassword());
        return ResponseEntity.status(201).body(u.getEmail());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    /**
     * Admin-only: create a user with a specified role (GUEST | MANAGER | ADMIN).
     * Requires Authorization: Bearer <token of an existing ADMIN>.
     */
    @PostMapping("/register-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerWithRole(@Valid @RequestBody RegisterAdminRequest req) {
        User u = authService.registerWithRole(req.getName(), req.getEmail(), req.getPassword(), req.getRole());
        return ResponseEntity.status(201).body(u.getEmail() + " (" + u.getRole() + ")");
    }

    /**
     * Logs out the current user by blacklisting their bearer token.
     * Subsequent requests using the same token will be rejected as unauthenticated.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokenBlacklist.blacklist(authHeader.substring(7));
        }
        return ResponseEntity.ok(java.util.Map.of("message", "Logout successful"));
    }
}

