package com.hotel.stayease.service.mapper;

import com.hotel.stayease.dto.BookingDto;
import com.hotel.stayease.model.Booking;

public class BookingMapper {
    public static BookingDto toDto(Booking b) {
        BookingDto d = new BookingDto();
        d.setId(b.getId());
        d.setBookingRef(b.getBookingRef());
        if (b.getRoom() != null) {
            d.setRoomId(b.getRoom().getId());
            if (b.getRoom().getHotel() != null) {
                d.setHotelId(b.getRoom().getHotel().getId());
                d.setHotelName(b.getRoom().getHotel().getName());
            }
        }
        d.setCheckInDate(b.getCheckInDate());
        d.setCheckOutDate(b.getCheckOutDate());
        d.setTotalPrice(b.getTotalPrice());
        d.setStatus(b.getStatus());
        return d;
    }
}

