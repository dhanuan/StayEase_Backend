package com.hotel.stayease.controller;

import com.hotel.stayease.dto.BookingRequest;
import com.hotel.stayease.dto.BookingDto;
import com.hotel.stayease.model.Booking;
import com.hotel.stayease.service.BookingService;
import com.hotel.stayease.service.mapper.BookingMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest req) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Booking b = bookingService.createBooking(email, req.getRoomId(), req.getCheckInDate(), req.getCheckOutDate());
        return ResponseEntity.status(201).body(BookingMapper.toDto(b));
    }

    @GetMapping("/mine")
    public ResponseEntity<?> myBookings() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Booking> list = bookingService.getBookingsForUser(email);
        List<BookingDto> dtos = list.stream().map(BookingMapper::toDto).toList();
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        bookingService.cancelBooking(id, email);
        return ResponseEntity.noContent().build();
    }
}

