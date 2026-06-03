# StayEase Implementation Notes

## Overview

This document provides a comprehensive guide to the StayEase Foundation Hotel Booking System implementation, covering architecture, JWT authentication, business logic, and key design decisions.

---

## 🏗️ Architecture Overview

### Layered Architecture
```
┌─────────────────────────────────────────┐
│        REST Controllers                  │  (HTTP Entry Points)
├─────────────────────────────────────────┤
│        Service Layer                     │  (Business Logic)
├─────────────────────────────────────────┤
│        Repository Layer (JPA)            │  (Data Persistence)
├─────────────────────────────────────────┤
│        H2/PostgreSQL Database            │  (Data Store)
└─────────────────────────────────────────┘
```

### Key Components

#### Controllers (`controller/`)
- **AuthController**: Registration and login endpoints
- **HotelController**: Hotel search, availability, and admin CRUD
- **RoomController**: Manager CRUD operations for rooms
- **BookingController**: Guest booking operations
- **GlobalExceptionHandler**: Centralized error handling

#### Services (`service/`)
- **AuthService**: Authentication orchestration (login/register)
- **UserService**: User management and lookup
- **CustomUserDetailsService**: Spring Security integration
- **HotelService**: Hotel-related business logic
- **RoomService**: Room management
- **BookingService**: **Core booking logic** (availability check, creation, cancellation)

#### DTOs (`dto/`)
- Request DTOs: `AuthRequest`, `RegisterRequest`, `BookingRequest`
- Response DTOs: `AuthResponse`, `HotelDto`, `RoomDto`, `BookingDto`
- Validation annotations on all request DTOs

#### Entities (`model/`)
- `User`: User account with role (GUEST/MANAGER/ADMIN)
- `Hotel`: Hotel information and manager reference
- `Room`: Hotel room details and pricing
- `Booking`: Guest booking records with date range

#### Repositories (`repository/`)
- Standard CRUD via Spring Data JPA
- **Custom JPQL queries**:
  - `RoomRepository.findAvailableRooms()`: Availability check logic
  - `BookingRepository.findOverlappingBookings()`: Overlap detection

---

## 🔐 JWT Authentication Implementation

### Flow Diagram
```
┌──────────────────────────────────────────────────────────────┐
│ Client Request with Authorization: Bearer <token>             │
└───���────────────────────────┬─────────────────────────────────┘
                             │
                      ┌──────▼──────┐
                      │  JwtFilter  │
                      └──────┬──────┘
                             │
                   ┌─────────┴─────────┐
                   │                   │
            ┌──────▼──────┐   ┌─────────▼────┐
            │ JwtUtil     │   │ Extract Token │
            │ validateToken() │
            └──────┬──────┘   └─────────┬────┘
                   │                   │
                   └─────────┬─────────┘
                             │
                    ┌────────▼────────┐
                    │ Valid? Extract  │
                    │ subject (email) │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │ UserRepository  │
                    │ findByEmail()   │
                    └────────┬────────┘
                             │
                    ┌────────▼────────────────────┐
                    │ Set Authentication in       │
                    │ SecurityContextHolder with  │
                    │ user's role as authority    │
                    └────────┬───────────────────┘
                             │
                    ┌────────▼────────┐
                    │ Continue to     │
                    │ Controller      │
                    └─────────────────┘
```

### JWT Components

#### 1. **JwtUtil** (`security/JwtUtil.java`)
- Reads JWT secret from `application.properties` (`jwt.secret` property)
- Generates tokens with subject (email), issued-at, and expiration
- Validates tokens and extracts subject (email)
- Uses HMAC SHA256 signing

```properties
# application.properties
jwt.secret=changeit-changeit-changeit-changeit-123456  # Change for production!
jwt.expiration-ms=86400000                              # 24 hours
```

**Key Methods:**
- `generateToken(String email)`: Creates JWT
- `extractSubject(String token)`: Extracts email from token
- `validateToken(String token)`: Checks token validity

#### 2. **JwtFilter** (`security/JwtFilter.java`)
- Extends `OncePerRequestFilter`
- Runs on every HTTP request
- Extracts `Authorization: Bearer <token>` header
- Validates token and sets `Authentication` in `SecurityContext`
- Automatically associates user role with the request

#### 3. **SecurityConfig** (`security/SecurityConfig.java`)
- **Stateless session policy** (no cookies, JWT only)
- **CSRF disabled** (stateless API)
- Public endpoints: `/api/auth/**`, GET `/api/hotels/**`
- Protected endpoints: Everything else (requires valid JWT)
- Configures `PasswordEncoder` (BCrypt)

#### 4. **CustomUserDetailsService** (`service/CustomUserDetailsService.java`)
- Implements Spring Security's `UserDetailsService`
- Loads user from database by email
- Converts StayEase `User` role to Spring `GrantedAuthority`
- Returns Spring `UserDetails` for authentication

### Token Lifecycle
1. **Login** → `POST /api/auth/login` with email/password
2. **Validation** → AuthenticationManager validates credentials
3. **Token Generation** → JwtUtil creates JWT token
4. **Token Returned** → Response includes token in JSON
5. **Client Stores** → Client (UI/app) stores token (localStorage/secure storage)
6. **Token Usage** → Client sends in `Authorization: Bearer <token>` header
7. **Token Validation** → JwtFilter validates on every request
8. **Role Assignment** → Authority extracted from User.role field
9. **Token Expiry** → After 24 hours, token becomes invalid

---

## 💼 Business Logic: Room Availability

### The Core Problem
How do we know if a room is available for a given date range?

### The Solution
**A room is available if:**
1. It belongs to the requested hotel
2. It is active (not deactivated by manager)
3. No other non-cancelled booking overlaps the requested dates

### Implementation

#### JPQL Query (RoomRepository)
```java
@Query("""
SELECT r FROM Room r 
WHERE r.hotel.id = :hotelId 
  AND r.isActive = true 
  AND r.id NOT IN (
    SELECT b.room.id FROM Booking b 
    WHERE b.status <> 'CANCELLED' 
      AND b.checkInDate < :checkOut 
      AND b.checkOutDate > :checkIn
  )
""")
List<Room> findAvailableRooms(Long hotelId, LocalDate checkIn, LocalDate checkOut);
```

#### Date Overlap Logic
Two date ranges overlap if:
- `b.checkInDate < requestedCheckOut` **AND**
- `b.checkOutDate > requestedCheckIn`

**Example:**
```
Existing Booking: Jun 10 - Jun 15
Requested:        Jun 12 - Jun 18  → OVERLAP (NOT available)
Requested:        Jun 15 - Jun 18  → OK (no overlap, checkout = next checkin)
Requested:        Jun 08 - Jun 10  → OK (no overlap, checkout = next checkin)
```

#### Booking Creation
```java
// BookingService.createBooking()
1. Load User from repository
2. Load Room from repository
3. Check for overlapping bookings
   - If any found → throw RuntimeException("Room not available...")
4. Calculate nights: ChronoUnit.DAYS.between(checkIn, checkOut)
5. Calculate totalPrice: pricePerNight × nights
6. Create Booking with status = "CONFIRMED"
7. Save and return
```

### Booking Cancellation
```java
// BookingService.cancelBooking()
1. Load Booking
2. Verify requester is the owner
3. Set status = "CANCELLED"
4. Save (doesn't delete, just marks as cancelled)
5. Room becomes available again for those dates
```

---

## 📊 Database Design

### Entity Relationship Diagram
```
app_user (1) ────────────────────(M) booking
  │ id                              │ id
  │ email (unique)                  │ user_id (FK)
  │ password                        │ room_id (FK)
  │ role                            │ checkInDate
  │ name                            │ checkOutDate
  │ created_at                      │ totalPrice
                                    │ status
                                    │ created_at

hotel (1) ─────────────────────(M) room
  │ id                            │ id
  │ name                          │ hotel_id (FK)
  │ city                          │ roomNumber
  │ starRating                    │ roomType
  │ description                   │ pricePerNight
  │ managerId                     │ maxOccupancy
  │ created_at                    │ isActive
                                  │ created_at

                                room (1) ─────────────────────(M) booking
                                                                  └─ (FK reference above)
```

### Key Constraints
- `app_user.email`: UNIQUE
- `booking.booking_ref`: UNIQUE (UUID)
- Foreign keys cascade on delete (room/booking)
- Lazy loading on relationships (no N+1 queries)

---

## 🔒 Role-Based Access Control

### Roles & Permissions
| Role | Can | Cannot |
|------|-----|--------|
| **GUEST** | Search hotels, view rooms, create/cancel own bookings | Manage anything, create hotels/rooms |
| **MANAGER** | Manage rooms for their hotel, view bookings for their hotel | Manage other hotels, manage admin functions |
| **ADMIN** | Create/update/delete hotels, manage all aspects | Restricted to admin endpoints only |

### Authorization Enforcement

#### Method-Level Security (`@PreAuthorize`)
```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> createHotel(@RequestBody Hotel hotel) { ... }

@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<?> createRoom(@PathVariable Long hotelId, ...) { ... }
```

#### Ownership Checks (In Method Body)
```java
// RoomController.createRoom()
String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
User u = userRepository.findByEmail(email).orElseThrow();
Hotel hotel = hotelRepository.findById(hotelId).orElseThrow();

if (!u.getId().equals(hotel.getManagerId())) 
    return ResponseEntity.status(403).build();  // Forbidden
```

This ensures even if a MANAGER token is valid, they can only modify rooms for their own hotel.

---

## ✅ Input Validation

### DTOs with Annotations
```java
// RegisterRequest.java
public class RegisterRequest {
    @NotBlank
    private String name;
    
    @NotBlank @Email
    private String email;
    
    @NotBlank @Size(min = 6)
    private String password;
}

// BookingRequest.java
public class BookingRequest {
    @NotNull
    private Long roomId;
    
    @NotNull @FutureOrPresent
    private LocalDate checkInDate;
    
    @NotNull
    private LocalDate checkOutDate;
}
```

### Validation Error Handling
```java
// GlobalExceptionHandler.java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("error", "validation_failed");
    Map<String, String> details = new HashMap<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(fe -> details.put(fe.getField(), fe.getDefaultMessage()));
    body.put("details", details);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
}
```

**Example Error Response:**
```json
{
  "error": "validation_failed",
  "details": {
    "email": "must be a valid email address",
    "password": "size must be between 6 and 2147483647"
  }
}
```

---

## 🧪 Unit Tests

### BookingService Tests
Located in `src/test/java/com/hotel/stayease/service/BookingServiceTest.java`

#### Test Cases

**1. Successful Booking Creation**
- Setup: Valid user, valid room, no overlapping bookings
- Action: Create booking for Jun 10-12
- Assert: Booking created with correct total price (2 nights × $100 = $200)

**2. Overlapping Booking Detection**
- Setup: Valid user, valid room, WITH overlapping existing booking
- Action: Try to create booking for same dates
- Assert: RuntimeException thrown with message "Room not available for selected dates"

**Mock Objects Used:**
- `BookingRepository`: Mock findOverlappingBookings behavior
- `RoomRepository`: Mock findById to return test room
- `UserRepository`: Mock findByEmail to return test user

---

## 🛠️ Configuration & Properties

### `application.properties`
```properties
# Application
spring.application.name=stayease

# Database (H2 Dev)
spring.datasource.url=jdbc:h2:mem:stayease;DB_CLOSE_DELAY=-1
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JWT
jwt.secret=changeit-changeit-changeit-changeit-123456
jwt.expiration-ms=86400000
```

### Profiles (Optional)
Create `application-prod.properties` for production:
```properties
spring.datasource.url=jdbc:postgresql://db-host:5432/stayease
spring.datasource.username=postgres_user
jwt.secret=${JWT_SECRET}  # Load from environment
```

Run with profile: `java -Dspring.profiles.active=prod -jar app.jar`

---

## 📡 Data Transfer Objects (DTOs)

### Why DTOs?
1. **Decoupling**: Controller doesn't expose JPA entities
2. **Security**: Hide internal fields (entity relationships)
3. **Validation**: Apply rules at API boundary
4. **Transformation**: Convert to JSON-friendly format

### Mappers (`service/mapper/`)
Manual mappers for simple transformations:

```java
public class BookingMapper {
    public static BookingDto toDto(Booking b) {
        BookingDto d = new BookingDto();
        d.setId(b.getId());
        d.setBookingRef(b.getBookingRef());
        // ...extract nested data (hotel name, etc.)
        return d;
    }
}
```

**Alternative:** Use MapStruct for auto-generation (not implemented in this foundation version)

---

## 🐛 Error Handling

### Global Exception Handler
Catches and formats all exceptions:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(...) { ... }
}
```

### Example Responses

**404 Not Found** (when room not found):
```json
{ "error": "Room with id 999 not found" }
```

**400 Bad Request** (availability conflict):
```json
{ "error": "Room not available for selected dates" }
```

**400 Bad Request** (validation failure):
```json
{
  "error": "validation_failed",
  "details": {
    "email": "must be a valid email address"
  }
}
```

---

## 🚀 Performance Considerations

### Query Optimization

**Lazy Loading**
- Room, Hotel, User relationships are lazy-loaded
- Prevents N+1 queries
- Explicitly fetch only when needed

**Custom JPQL Queries**
- `findAvailableRooms()` uses subquery (efficient)
- Single query for availability check (not loop + loop)

**Pagination**
- Hotel search supports `page` and `size` parameters
- Reduces result set transferred to client

### Caching (Future)
- Hotel search results could be cached (Redis)
- Room availability doesn't change frequently for past dates
- Booking data is real-time (no cache)

---

## 📦 Dependencies

### Core Framework
- `spring-boot-starter-web`: REST endpoints
- `spring-boot-starter-data-jpa`: Database ORM
- `spring-boot-starter-security`: Authentication framework

### Database
- `spring-boot-h2console`: H2 in-memory DB (dev)
- No PostgreSQL driver needed unless using production profile

### JWT
- `jjwt-api:0.11.5`: JWT creation/parsing
- `jjwt-impl:0.11.5`: Implementation
- `jjwt-jackson:0.11.5`: JSON serialization

### Validation & API Docs
- `spring-boot-starter-validation`: Jakarta validation annotations
- `springdoc-openapi-starter-webmvc-ui:2.1.0`: Swagger UI

---

## 🎓 Design Patterns Used

### 1. **Dependency Injection**
```java
@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;
    // Spring auto-injects dependencies
}
```

### 2. **Repository Pattern**
```java
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
}
// Spring auto-implements CRUD + custom methods
```

### 3. **Service Layer**
```java
// Controllers delegate to services
@PostMapping("/bookings")
public ResponseEntity<?> createBooking(@RequestBody BookingRequest req) {
    Booking b = bookingService.createBooking(...);  // Service handles logic
    return ResponseEntity.status(201).body(BookingMapper.toDto(b));
}
```

### 4. **DTO Mapper**
```java
// Controllers return DTOs, not entities
List<RoomDto> dtos = rooms.stream().map(RoomMapper::toDto).toList();
```

### 5. **Exception Handler Advice**
```java
@RestControllerAdvice  // AOP intercepts all exceptions
public class GlobalExceptionHandler { ... }
```

---

## 🔄 Workflow Examples

### Complete Booking Workflow
```
1. User registers
   POST /api/auth/register
   → User created with role=GUEST

2. User logs in
   POST /api/auth/login
   → JWT token returned

3. User searches hotels
   GET /api/hotels?city=Metropolis
   → Public endpoint, no auth needed
   → Returns: [HotelDto, HotelDto, ...]

4. User views available rooms
   GET /api/hotels/1/rooms?checkIn=2026-06-15&checkOut=2026-06-17
   → Calls findAvailableRooms() JPQL query
   → Returns: [RoomDto, RoomDto, ...]

5. User creates booking
   POST /api/bookings
   Headers: Authorization: Bearer <token>
   Body: { roomId: 1, checkInDate: "2026-06-15", checkOutDate: "2026-06-17" }
   
   Backend:
   a. Validate token (JwtFilter)
   b. Extract user email
   c. Check for overlapping bookings
   d. Calculate nights & total price
   e. Create Booking record
   → Response: BookingDto with bookingRef and totalPrice

6. User views bookings
   GET /api/bookings/mine
   Headers: Authorization: Bearer <token>
   → Returns: [BookingDto, BookingDto, ...]

7. User cancels booking
   PUT /api/bookings/1/cancel
   Headers: Authorization: Bearer <token>
   → Mark booking status = "CANCELLED"
   → Room available for those dates again
```

### Admin Hotel Management Workflow
```
1. Admin logs in
   POST /api/auth/login (admin@stayease.com / adminpass)
   → JWT token with role=ADMIN

2. Admin creates hotel
   POST /api/hotels
   Headers: Authorization: Bearer <admin-token>
   Headers: Content-Type: application/json
   Body: {
     "name": "New Hotel",
     "city": "New City",
     "starRating": 5,
     "managerId": 2  // Reference existing manager
   }
   → Validated by @PreAuthorize("hasRole('ADMIN')")
   → Hotel created in DB

3. Admin updates hotel
   PUT /api/hotels/1
   → Same authorization, ownership doesn't matter (admin can do anything)

4. Admin deletes hotel
   DELETE /api/hotels/1
   → Cascades delete to rooms and bookings
```

### Manager Room Management Workflow
```
1. Manager logs in
   POST /api/auth/login (manager@stayease.com / managerpass)
   → JWT token with role=MANAGER

2. Manager creates room for their hotel
   POST /api/hotels/1/rooms
   Headers: Authorization: Bearer <manager-token>
   Body: {
     "roomNumber": "201",
     "roomType": "DOUBLE",
     "pricePerNight": 100,
     "maxOccupancy": 2
   }
   
   Backend:
   a. Validate token
   b. Check @PreAuthorize("hasRole('MANAGER')")
   c. Load manager from token
   d. Load hotel by ID
   e. Verify manager.id == hotel.managerId (ownership check)
   f. Create Room with hotel reference
   → Response: RoomDto

3. Manager views upcoming bookings for their hotel
   (Could add endpoint: GET /api/bookings/hotel/{hotelId})
```

---

## 🎯 Testing Strategy

### Unit Tests (Mockito)
- Test service layer with mocked repositories
- Focus on business logic (availability check, price calculation)
- Each test should be independent and fast

### Integration Tests (MockMvc) - Optional
- Test full controller → service → repository chain
- Use `@SpringBootTest` with `MockMvc`
- Test HTTP layer (status codes, headers, error responses)

### Manual Testing
- Use Postman or curl for API testing
- Test JWT flow: register → login → use token
- Test role-based access: try admin endpoint with guest token (should 403)
- Test availability: create booking, try booking same dates (should 400)

---

## 📋 Key Files & Locations

| File | Purpose |
|------|---------|
| `src/main/resources/application.properties` | Database & JWT config |
| `src/main/java/com/hotel/stayease/security/JwtUtil.java` | Token generation & validation |
| `src/main/java/com/hotel/stayease/security/JwtFilter.java` | Token extraction & auth setup |
| `src/main/java/com/hotel/stayease/security/SecurityConfig.java` | Spring Security config |
| `src/main/java/com/hotel/stayease/service/BookingService.java` | Core booking logic |
| `src/main/java/com/hotel/stayease/repository/RoomRepository.java` | Availability JPQL query |
| `src/main/java/com/hotel/stayease/controller/GlobalExceptionHandler.java` | Error handling |
| `src/main/java/com/hotel/stayease/DataLoader.java` | Seed data on startup |
| `src/test/java/com/hotel/stayease/service/BookingServiceTest.java` | Unit tests |

---

## 🚀 Deployment Checklist

- [ ] Change JWT secret in `application.properties`
- [ ] Configure production database (PostgreSQL)
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` (don't auto-create in prod)
- [ ] Enable HTTPS/TLS
- [ ] Configure CORS if frontend is on different domain
- [ ] Add rate limiting on auth endpoints
- [ ] Set up logging to file/service
- [ ] Configure monitoring/alerts
- [ ] Add database backups
- [ ] Run full test suite before deployment
- [ ] Load-test with realistic user load

---

## 🔗 References

- Spring Boot: https://spring.io/projects/spring-boot
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- JWT: https://jwt.io/
- JJWT: https://github.com/jwtk/jjwt
- SpringDoc OpenAPI: https://springdoc.org/

---

**Last Updated**: June 2, 2026  
**Version**: 1.0  
**Status**: Foundation Complete ✅

