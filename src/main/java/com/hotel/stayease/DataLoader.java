package com.hotel.stayease;

import com.hotel.stayease.model.Hotel;
import com.hotel.stayease.model.Room;
import com.hotel.stayease.model.User;
import com.hotel.stayease.repository.HotelRepository;
import com.hotel.stayease.repository.RoomRepository;
import com.hotel.stayease.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner init(UserRepository userRepository, HotelRepository hotelRepository, RoomRepository roomRepository, PasswordEncoder encoder) {
        return args -> {
            if (userRepository.findByEmail("admin@stayease.com").isEmpty()) {
                User admin = new User();
                admin.setName("Admin");
                admin.setEmail("admin@stayease.com");
                admin.setPassword(encoder.encode("adminpass"));
                admin.setRole("ADMIN");
                userRepository.save(admin);
            }

            if (userRepository.findByEmail("manager@stayease.com").isEmpty()) {
                User manager = new User();
                manager.setName("Manager");
                manager.setEmail("manager@stayease.com");
                manager.setPassword(encoder.encode("managerpass"));
                manager.setRole("MANAGER");
                manager = userRepository.save(manager);

                Hotel h = new Hotel();
                h.setName("Sample Hotel");
                h.setCity("Metropolis");
                h.setDescription("A sample hotel for demo");
                h.setStarRating(4);
                h.setCoverImageUrl("");
                h.setManagerId(manager.getId());
                h = hotelRepository.save(h);

                Room r1 = new Room();
                r1.setHotel(h);
                r1.setRoomNumber("101");
                r1.setRoomType("SINGLE");
                r1.setPricePerNight(BigDecimal.valueOf(50));
                r1.setMaxOccupancy(1);
                roomRepository.save(r1);

                Room r2 = new Room();
                r2.setHotel(h);
                r2.setRoomNumber("102");
                r2.setRoomType("DOUBLE");
                r2.setPricePerNight(BigDecimal.valueOf(80));
                r2.setMaxOccupancy(2);
                roomRepository.save(r2);
            }
        };
    }
}

