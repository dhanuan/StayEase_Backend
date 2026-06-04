package com.hotel.stayease.security;

import com.hotel.stayease.model.User;
import com.hotel.stayease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenBlacklist tokenBlacklist;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (!tokenBlacklist.isBlacklisted(token) && jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractSubject(token);
                if (email != null) {
                    User user = userRepository.findByEmail(email).orElse(null);
                    if (user != null) {
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null, Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}

