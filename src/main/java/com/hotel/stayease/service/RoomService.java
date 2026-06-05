package com.hotel.stayease.service;

import com.hotel.stayease.model.Hotel;
import com.hotel.stayease.model.Room;
import com.hotel.stayease.repository.HotelRepository;
import com.hotel.stayease.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    public Room createRoom(Long hotelId, Room room) {
        Hotel h = hotelRepository.findById(hotelId).orElseThrow();
        room.setHotel(h);
        return roomRepository.save(room);
    }

    public Optional<Room> findById(Long id) { return roomRepository.findById(id); }

    public Room update(Room r) { return roomRepository.save(r); }

    public void delete(Long id) { roomRepository.deleteById(id); }

    public List<Room> findByHotelId(Long hotelId) { return roomRepository.findByHotelId(hotelId); }
}

