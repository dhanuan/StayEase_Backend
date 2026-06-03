package com.hotel.stayease.service.mapper;

import com.hotel.stayease.dto.HotelDto;
import com.hotel.stayease.model.Hotel;

public class HotelMapper {
    public static HotelDto toDto(Hotel h) {
        HotelDto d = new HotelDto();
        d.setId(h.getId());
        d.setName(h.getName());
        d.setCity(h.getCity());
        d.setStarRating(h.getStarRating());
        d.setDescription(h.getDescription());
        d.setCoverImageUrl(h.getCoverImageUrl());
        return d;
    }
}

