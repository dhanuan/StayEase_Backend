package com.hotel.stayease.service.mapper;

import com.hotel.stayease.dto.RoomDto;
import com.hotel.stayease.model.Room;

public class RoomMapper {
    public static RoomDto toDto(Room r) {
        RoomDto d = new RoomDto();
        d.setId(r.getId());
        d.setRoomNumber(r.getRoomNumber());
        d.setRoomType(r.getRoomType());
        d.setPricePerNight(r.getPricePerNight());
        d.setMaxOccupancy(r.getMaxOccupancy());
        d.setDescription(r.getDescription());
        d.setImageUrl(r.getImageUrl());
        d.setActive(r.isActive());
        // optionally include hotel info in DTO in future
        return d;
    }
}

