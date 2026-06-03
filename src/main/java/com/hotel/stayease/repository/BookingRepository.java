package com.hotel.stayease.repository;

import com.hotel.stayease.model.Booking;
import com.hotel.stayease.model.Room;
import com.hotel.stayease.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);

    @Query("SELECT b FROM Booking b WHERE b.room = :room AND b.status <> 'CANCELLED' AND b.checkInDate < :checkOut AND b.checkOutDate > :checkIn")
    List<Booking> findOverlappingBookings(@Param("room") Room room, @Param("checkIn") LocalDate checkIn, @Param("checkOut") LocalDate checkOut);
}

