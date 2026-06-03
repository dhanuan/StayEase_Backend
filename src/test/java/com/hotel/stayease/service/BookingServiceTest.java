package com.hotel.stayease.service;

import com.hotel.stayease.model.Booking;
import com.hotel.stayease.model.Room;
import com.hotel.stayease.model.User;
import com.hotel.stayease.repository.BookingRepository;
import com.hotel.stayease.repository.RoomRepository;
import com.hotel.stayease.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    @Mock
    BookingRepository bookingRepository;

    @Mock
    RoomRepository roomRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    BookingService bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBooking_success() {
        User u = new User(); u.setId(1L); u.setEmail("guest@x.com");
        Room r = new Room(); r.setId(10L); r.setPricePerNight(BigDecimal.valueOf(100));
        LocalDate in = LocalDate.now().plusDays(1);
        LocalDate out = in.plusDays(2);

        when(userRepository.findByEmail("guest@x.com")).thenReturn(Optional.of(u));
        when(roomRepository.findById(10L)).thenReturn(Optional.of(r));
        when(bookingRepository.findOverlappingBookings(eq(r), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Booking b = bookingService.createBooking("guest@x.com", 10L, in, out);
        assertNotNull(b);
        assertEquals(b.getTotalPrice(), BigDecimal.valueOf(200));
    }

    @Test
    void createBooking_overlap_throws() {
        User u = new User(); u.setId(1L); u.setEmail("guest@x.com");
        Room r = new Room(); r.setId(10L); r.setPricePerNight(BigDecimal.valueOf(100));
        LocalDate in = LocalDate.now().plusDays(1);
        LocalDate out = in.plusDays(2);

        when(userRepository.findByEmail("guest@x.com")).thenReturn(Optional.of(u));
        when(roomRepository.findById(10L)).thenReturn(Optional.of(r));
        when(bookingRepository.findOverlappingBookings(eq(r), any(), any())).thenReturn(Collections.singletonList(new Booking()));

        assertThrows(RuntimeException.class, () -> bookingService.createBooking("guest@x.com", 10L, in, out));
    }
}

