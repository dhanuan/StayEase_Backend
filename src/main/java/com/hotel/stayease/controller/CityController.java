package com.hotel.stayease.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    private static final List<String> CITIES = List.of("Bengaluru", "Mumbai", "Pune");

    @GetMapping
    public ResponseEntity<List<String>> getCities() {
        return ResponseEntity.ok(CITIES);
    }
}

