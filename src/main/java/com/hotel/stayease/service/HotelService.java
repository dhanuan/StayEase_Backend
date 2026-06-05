package com.hotel.stayease.service;

import com.hotel.stayease.model.Hotel;
import com.hotel.stayease.model.Room;
import com.hotel.stayease.repository.HotelRepository;
import com.hotel.stayease.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    public List<Hotel> findByCity(String city) {
        return hotelRepository.findByCityIgnoreCase(city);
    }

    public List<Hotel> findAll() {
        return hotelRepository.findAll();
    }

    public Optional<Hotel> findById(Long id) { return hotelRepository.findById(id); }

    public Hotel save(Hotel hotel) { return hotelRepository.save(hotel); }

    public void delete(Long id) { hotelRepository.deleteById(id); }

    public List<Room> findAvailableRooms(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAvailableRooms(hotelId, checkIn, checkOut);
    }
}

