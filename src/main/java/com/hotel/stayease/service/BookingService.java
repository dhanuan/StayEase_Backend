package com.hotel.stayease.service;

import com.hotel.stayease.model.Booking;
import com.hotel.stayease.model.Room;
import com.hotel.stayease.model.User;
import com.hotel.stayease.repository.BookingRepository;
import com.hotel.stayease.repository.RoomRepository;
import com.hotel.stayease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    public Booking createBooking(String userEmail, Long roomId, java.time.LocalDate checkIn, java.time.LocalDate checkOut) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        Room room = roomRepository.findById(roomId).orElseThrow();

        // check overlapping bookings
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(room, checkIn, checkOut);
        if (!overlaps.isEmpty()) {
            throw new RuntimeException("Room not available for selected dates");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal total = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Booking b = new Booking();
        b.setUser(user);
        b.setRoom(room);
        b.setCheckInDate(checkIn);
        b.setCheckOutDate(checkOut);
        b.setTotalPrice(total);
        b.setStatus("CONFIRMED");
        return bookingRepository.save(b);
    }

    public List<Booking> getBookingsForUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return bookingRepository.findByUser(user);
    }

    public void cancelBooking(Long bookingId, String requesterEmail) {
        Booking b = bookingRepository.findById(bookingId).orElseThrow();
        // allow only owner or manager/admin in real app; here allow owner
        if (!b.getUser().getEmail().equals(requesterEmail)) {
            throw new RuntimeException("Not authorized to cancel");
        }
        b.setStatus("CANCELLED");
        bookingRepository.save(b);
    }
}

