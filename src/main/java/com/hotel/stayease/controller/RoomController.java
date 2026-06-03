package com.hotel.stayease.controller;

import com.hotel.stayease.dto.RoomDto;
import com.hotel.stayease.model.Hotel;
import com.hotel.stayease.model.Room;
import com.hotel.stayease.model.User;
import com.hotel.stayease.repository.HotelRepository;
import com.hotel.stayease.repository.RoomRepository;
import com.hotel.stayease.repository.UserRepository;
import com.hotel.stayease.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/api/hotels/{hotelId}/rooms")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> createRoom(@PathVariable Long hotelId, @RequestBody Room room) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User u = userRepository.findByEmail(email).orElseThrow();
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow();
        if (!u.getId().equals(hotel.getManagerId())) return ResponseEntity.status(403).build();
        Room saved = roomService.createRoom(hotelId, room);
        return ResponseEntity.status(201).body(com.hotel.stayease.service.mapper.RoomMapper.toDto(saved));
    }

    @PutMapping("/api/rooms/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody Room r) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User u = userRepository.findByEmail(email).orElseThrow();
        Room existing = roomService.findById(id).orElseThrow();
        if (!u.getId().equals(existing.getHotel().getManagerId())) return ResponseEntity.status(403).build();
        r.setId(id);
        r.setHotel(existing.getHotel());
        return ResponseEntity.ok(com.hotel.stayease.service.mapper.RoomMapper.toDto(roomService.update(r)));
    }

    @DeleteMapping("/api/rooms/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User u = userRepository.findByEmail(email).orElseThrow();
        Room existing = roomService.findById(id).orElseThrow();
        if (!u.getId().equals(existing.getHotel().getManagerId())) return ResponseEntity.status(403).build();
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/rooms/{id}/status")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User u = userRepository.findByEmail(email).orElseThrow();
        Room existing = roomService.findById(id).orElseThrow();
        if (!u.getId().equals(existing.getHotel().getManagerId())) return ResponseEntity.status(403).build();
        existing.setActive(!existing.isActive());
        roomService.update(existing);
        return ResponseEntity.ok(com.hotel.stayease.service.mapper.RoomMapper.toDto(existing));
    }
}

