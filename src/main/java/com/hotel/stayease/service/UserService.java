package com.hotel.stayease.service;

import com.hotel.stayease.model.User;
import com.hotel.stayease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(String name, String email, String rawPassword) {
        return register(name, email, rawPassword, "GUEST");
    }

    public User register(String name, String email, String rawPassword, String role) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRole(role == null ? "GUEST" : role);
        return userRepository.save(u);
    }

    public Optional<User> findByEmail(String email) { return userRepository.findByEmail(email); }
}

