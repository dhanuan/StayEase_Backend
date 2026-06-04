package com.hotel.stayease.service;

import com.hotel.stayease.dto.AuthResponse;
import com.hotel.stayease.model.User;
import com.hotel.stayease.repository.UserRepository;
import com.hotel.stayease.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    public AuthResponse login(String email, String password) {
        Authentication a = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        // if no exception, authenticated
        String token = jwtUtil.generateToken(email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));
        return new AuthResponse(token, user.getRole(), user.getEmail());
    }

    public User register(String name, String email, String password) {
        return userService.register(name, email, password);
    }

    public User registerWithRole(String name, String email, String password, String role) {
        return userService.register(name, email, password, role);
    }
}

