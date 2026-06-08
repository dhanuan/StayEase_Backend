package com.hotel.stayease.controller;

import com.hotel.stayease.model.User;
import com.hotel.stayease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Fetches the ID of a user matching the given email and role = MANAGER.
     * Example: GET /api/users/manager-id?email=bob@stayease.com
     */
    @GetMapping("/manager-id")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getManagerIdByEmail(@RequestParam String email) {
        Optional<User> user = userRepository.findByEmailIgnoreCaseAndRole(email, "MANAGER");
        if (user.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "message", "No manager found with the given email",
                    "email", email
            ));
        }
        User u = user.get();
        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "email", u.getEmail(),
                "name", u.getName(),
                "role", u.getRole()
        ));
    }

    /**
     * Returns all users with role = MANAGER (id, name, email). Admin only.
     * Example: GET /api/users/managers
     */
    @GetMapping("/managers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllManagers() {
        List<Map<String, Object>> managers = userRepository.findByRole("MANAGER").stream()
                .map(u -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("managerId", u.getId());
                    m.put("managerName", u.getName());
                    m.put("email", u.getEmail());
                    return m;
                })
                .toList();
        return ResponseEntity.ok(managers);
    }
}

