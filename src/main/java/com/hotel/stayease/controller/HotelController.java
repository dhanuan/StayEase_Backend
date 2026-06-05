package com.hotel.stayease.controller;

import com.hotel.stayease.dto.HotelDto;
import com.hotel.stayease.dto.HotelSummaryDto;
import com.hotel.stayease.dto.RoomDto;
import com.hotel.stayease.model.Hotel;
import com.hotel.stayease.model.Room;
import com.hotel.stayease.model.User;
import com.hotel.stayease.repository.UserRepository;
import com.hotel.stayease.service.HotelService;
import com.hotel.stayease.service.mapper.HotelMapper;
import com.hotel.stayease.service.mapper.RoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> search(@RequestParam String city,
                                    @RequestParam(required = false) Integer minStars,
                                    @RequestParam(required = false, defaultValue = "0") Integer page,
                                    @RequestParam(required = false, defaultValue = "20") Integer size) {
        // Basic filter and pagination (in-memory for simplicity)
        List<Hotel> hotels = hotelService.findByCity(city);
        if (minStars != null) {
            hotels = hotels.stream().filter(h -> h.getStarRating() >= minStars).toList();
        }
        int from = Math.min(page * size, hotels.size());
        int to = Math.min(from + size, hotels.size());
        List<HotelDto> dtos = hotels.subList(from, to).stream().map(HotelMapper::toDto).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/all")
    public ResponseEntity<?> listAll(@RequestParam(required = false) String name,
                                     @RequestParam(required = false) String city) {
        List<Hotel> hotels = (city == null || city.isBlank())
                ? hotelService.findAll()
                : hotelService.findByCity(city);
        if (name != null && !name.isBlank()) {
            String q = name.toLowerCase();
            hotels = hotels.stream()
                    .filter(h -> h.getName() != null && h.getName().toLowerCase().contains(q))
                    .toList();
        }
        List<HotelSummaryDto> dtos = hotels.stream()
                .map(h -> new HotelSummaryDto(h.getName(), h.getCity(), h.getStarRating(), h.getManagerId()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/rooms")
    public ResponseEntity<?> availableRooms(@PathVariable Long id,
                                            @RequestParam LocalDate checkIn,
                                            @RequestParam LocalDate checkOut) {
        List<Room> rooms = hotelService.findAvailableRooms(id, checkIn, checkOut);
        List<RoomDto> dtos = rooms.stream().map(RoomMapper::toDto).toList();
        return ResponseEntity.ok(dtos);
    }

    // Admin endpoints
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createHotel(@RequestBody Hotel hotel) {
        Hotel saved = hotelService.save(hotel);
        return ResponseEntity.status(201).body(HotelMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateHotel(@PathVariable Long id, @RequestBody Hotel hotel) {
        hotel.setId(id);
        return ResponseEntity.ok(HotelMapper.toDto(hotelService.save(hotel)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteHotel(@PathVariable Long id) {
        hotelService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Hotel deleted", "id", id));
    }
}

