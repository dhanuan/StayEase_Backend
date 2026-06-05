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
import java.util.List;

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
                h.setCity("Mumbai");
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

            // Seed 2 hotels per city (Bengaluru, Pune, Mumbai), each with a dedicated manager and rooms.
            seedCityHotel(userRepository, hotelRepository, roomRepository, encoder,
                    "Bengaluru", "Bengaluru Skyline Inn", 4,
                    "Modern hotel near MG Road with rooftop dining.",
                    "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa",
                    "Ravi Kumar", "ravi.kumar@stayease.com");
            seedCityHotel(userRepository, hotelRepository, roomRepository, encoder,
                    "Bengaluru", "Garden City Suites", 5,
                    "Luxury suites surrounded by lush gardens in Whitefield.",
                    "https://images.unsplash.com/photo-1566073771259-6a8506099945",
                    "Priya Nair", "priya.nair@stayease.com");

            seedCityHotel(userRepository, hotelRepository, roomRepository, encoder,
                    "Pune", "Pune Heritage Hotel", 4,
                    "Heritage-style hotel close to Shaniwar Wada.",
                    "https://images.unsplash.com/photo-1455587734955-081b22074882",
                    "Amit Deshpande", "amit.deshpande@stayease.com");
            seedCityHotel(userRepository, hotelRepository, roomRepository, encoder,
                    "Pune", "Hinjewadi Tech Stay", 3,
                    "Business hotel in the heart of Hinjewadi IT Park.",
                    "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb",
                    "Sneha Patil", "sneha.patil@stayease.com");

            seedCityHotel(userRepository, hotelRepository, roomRepository, encoder,
                    "Mumbai", "Marine Drive Plaza", 5,
                    "Sea-facing rooms overlooking the Queen's Necklace.",
                    "https://images.unsplash.com/photo-1564501049412-61c2a3083791",
                    "Rohan Mehta", "rohan.mehta@stayease.com");
            seedCityHotel(userRepository, hotelRepository, roomRepository, encoder,
                    "Mumbai", "Bandra Boutique Hotel", 4,
                    "Trendy boutique hotel in the heart of Bandra West.",
                    "https://images.unsplash.com/photo-1571896349842-33c89424de2d",
                    "Anjali Shah", "anjali.shah@stayease.com");
        };
    }

    private void seedCityHotel(UserRepository userRepository,
                               HotelRepository hotelRepository,
                               RoomRepository roomRepository,
                               PasswordEncoder encoder,
                               String city,
                               String hotelName,
                               int starRating,
                               String description,
                               String coverImageUrl,
                               String managerName,
                               String managerEmail) {
        // Skip if a hotel with this name already exists in the city (idempotent restart).
        boolean exists = hotelRepository.findByCityIgnoreCase(city).stream()
                .anyMatch(h -> hotelName.equalsIgnoreCase(h.getName()));
        if (exists) {
            return;
        }

        User manager = userRepository.findByEmail(managerEmail).orElseGet(() -> {
            User u = new User();
            u.setName(managerName);
            u.setEmail(managerEmail);
            u.setPassword(encoder.encode("managerpass"));
            u.setRole("MANAGER");
            return userRepository.save(u);
        });

        Hotel hotel = new Hotel();
        hotel.setName(hotelName);
        hotel.setCity(city);
        hotel.setStarRating(starRating);
        hotel.setDescription(description);
        hotel.setCoverImageUrl(coverImageUrl);
        hotel.setManagerId(manager.getId());
        hotel = hotelRepository.save(hotel);

        // Three rooms per hotel: SINGLE, DOUBLE, SUITE
        roomRepository.saveAll(List.of(
                buildRoom(hotel, "101", "SINGLE", BigDecimal.valueOf(2499), 1,
                        "Cozy single room with city view.",
                        "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85"),
                buildRoom(hotel, "102", "DOUBLE", BigDecimal.valueOf(3999), 2,
                        "Comfortable double room with king-size bed.",
                        "https://images.unsplash.com/photo-1591088398332-8a7791972843"),
                buildRoom(hotel, "201", "SUITE", BigDecimal.valueOf(7999), 4,
                        "Spacious suite with living area and premium amenities.",
                        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b")
        ));
    }

    private Room buildRoom(Hotel hotel, String number, String type, BigDecimal price,
                           int maxOccupancy, String description, String imageUrl) {
        Room r = new Room();
        r.setHotel(hotel);
        r.setRoomNumber(number);
        r.setRoomType(type);
        r.setPricePerNight(price);
        r.setMaxOccupancy(maxOccupancy);
        r.setDescription(description);
        r.setImageUrl(imageUrl);
        r.setActive(true);
        return r;
    }
}

