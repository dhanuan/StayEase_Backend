# StayEase (Foundation) - Hotel Booking Application

A complete full-stack hotel room booking web application demonstrating foundational skills in **Spring Boot**, **REST APIs**, **JWT Authentication**, **JPA/Hibernate**, and basic **PostgreSQL/H2** database management.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Gradle 7.x+
- Windows PowerShell (or any shell)

### Run the Application

```powershell
.\gradlew.bat bootRun
```

The application will start on **http://localhost:8080**

## 📚 Architecture & Tech Stack

- **Frontend**: React/Angular (not included in this backend scaffold)
- **Backend**: Spring Boot 4.0.6 with Spring Data JPA
- **Database**: H2 (development) / PostgreSQL (production ready)
- **Authentication**: JWT (JSON Web Tokens)
- **Build Tool**: Gradle
- **Testing**: JUnit 5 + Mockito
- **API Docs**: SpringDoc OpenAPI (Swagger UI)

## ���� Authentication & Seeded Accounts

### Seeded Accounts (Available on startup)

| Email | Password | Role | Notes |
|-------|----------|------|-------|
| `admin@stayease.com` | `adminpass` | ADMIN | Can manage hotels |
| `manager@stayease.com` | `managerpass` | MANAGER | Can manage rooms for their hotel |
| (Register your own) | (Your choice) | GUEST | Default role for new registrations |

### JWT Workflow

1. **Register** → POST `/api/auth/register` → Email returned
2. **Login** → POST `/api/auth/login` → JWT token returned
3. **Use Token** → Pass `Authorization: Bearer <token>` in header for authenticated endpoints
4. **Token expiry** → 24 hours (configurable in `application.properties`)

**JWT Secret** is read from `application.properties` (`jwt.secret` property). For production, load from environment variables.

## 🎯 API Endpoints

### Authentication (Public)
```
POST   /api/auth/register       Register new guest account
POST   /api/auth/login          Login and get JWT token
```

### Hotels (Public: GET, Protected: POST/PUT/DELETE ADMIN only)
```
GET    /api/hotels              Search hotels by city (supports minStars filter, pagination)
GET    /api/hotels/{id}/rooms   Get available rooms for date range
POST   /api/hotels              Create hotel (ADMIN only)
PUT    /api/hotels/{id}         Update hotel (ADMIN only)
DELETE /api/hotels/{id}         Delete hotel (ADMIN only)
```

### Rooms (Protected: MANAGER only)
```
POST   /api/hotels/{hotelId}/rooms    Create room for hotel
PUT    /api/rooms/{id}                Update room details
DELETE /api/rooms/{id}                Delete room
PATCH  /api/rooms/{id}/status         Toggle room active/inactive status
```

### Bookings (Protected: Authenticated users)
```
POST   /api/bookings            Create a booking
GET    /api/bookings/mine       Get my bookings
PUT    /api/bookings/{id}/cancel Cancel a booking
```

## 📝 Example API Usage

### 1. Register a Guest Account
```powershell
$registerBody = @{
	name = "Jane Doe"
	email = "jane@example.com"
	password = "SecurePass123"
} | ConvertTo-Json

Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/register' `
  -Method POST -ContentType 'application/json' -Body $registerBody
```

### 2. Login and Get Token
```powershell
$loginBody = @{
	email = "admin@stayease.com"
	password = "adminpass"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' `
  -Method POST -ContentType 'application/json' -Body $loginBody

$token = $response.token
Write-Host "Token: $token"
```

### 3. Search Hotels
```powershell
# Search all hotels in Metropolis
Invoke-RestMethod -Uri 'http://localhost:8080/api/hotels?city=Metropolis' -Method GET

# Search with star rating filter (3 stars or higher)
Invoke-RestMethod -Uri 'http://localhost:8080/api/hotels?city=Metropolis&minStars=3' -Method GET

# Pagination (page 0, 20 results per page)
Invoke-RestMethod -Uri 'http://localhost:8080/api/hotels?city=Metropolis&page=0&size=20' -Method GET
```

### 4. Get Available Rooms for Date Range
```powershell
Invoke-RestMethod -Uri 'http://localhost:8080/api/hotels/1/rooms?checkIn=2026-06-15&checkOut=2026-06-17' `
  -Method GET
```

### 5. Create a Booking
```powershell
$headers = @{ Authorization = "Bearer $token" }
$bookingBody = @{
	roomId = 1
	checkInDate = "2026-06-15"
	checkOutDate = "2026-06-17"
} | ConvertTo-Json

Invoke-RestMethod -Uri 'http://localhost:8080/api/bookings' `
  -Method POST -ContentType 'application/json' -Headers $headers -Body $bookingBody
```
Response includes:
- Booking ID & Reference
- Total Price (calculated as: nights × pricePerNight)
- Hotel & Room Info
- Status

### 6. View My Bookings
```powershell
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri 'http://localhost:8080/api/bookings/mine' -Method GET -Headers $headers
```

### 7. Cancel a Booking
```powershell
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri 'http://localhost:8080/api/bookings/1/cancel' -Method PUT -Headers $headers
```

## 📊 Database

### H2 Console (Development)
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:stayease`
- Username: `sa`
- Password: (blank)

### Database Schema
Automatically created on startup (Hibernate `ddl-auto=update`):
- `app_user` - User accounts with roles (GUEST, MANAGER, ADMIN)
- `hotel` - Hotels managed by managers
- `room` - Hotel rooms with pricing
- `booking` - Guest bookings with date ranges and status

**Key Relationships:**
- Hotel (1) ← → (M) Room
- User (1) ← → (M) Booking
- Room (1) ← → (M) Booking

## ✅ Business Logic Highlights

### Room Availability Check
- Availability is calculated as: Rooms NOT with overlapping, non-cancelled bookings
- Query: `SELECT r FROM Room WHERE r.hotel.id = :hotelId AND r NOT IN (SELECT b.room FROM Booking b WHERE b.status != 'CANCELLED' AND overlaps date range)`

### Booking Confirmation
- Total Price = (Check-out Date − Check-in Date) × Price Per Night
- Booking Reference = UUID (unique identifier)
- Status defaults to CONFIRMED; can be cancelled

### Cancellation
- Marks booking status to CANCELLED (doesn't delete it)
- Room becomes available again for those dates

## 🧪 Testing

### Run Tests
```powershell
.\gradlew.bat test
```

### Test Coverage
- **BookingServiceTest**: Unit tests for availability check and booking creation
- **Integration tests**: Can be added for controllers (MockMvc)
- **Target coverage**: ≥60% method-level

## 📖 API Documentation

### Swagger UI
- Available at: **http://localhost:8080/swagger-ui.html**
- OpenAPI spec at: **http://localhost:8080/v3/api-docs**

### Example curl Commands

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@stayease.com","password":"adminpass"}'

# Search hotels
curl -X GET 'http://localhost:8080/api/hotels?city=Metropolis'

# Get available rooms
curl -X GET 'http://localhost:8080/api/hotels/1/rooms?checkIn=2026-06-15&checkOut=2026-06-17'

# Create booking (with token)
curl -X POST http://localhost:8080/api/bookings \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"roomId":1,"checkInDate":"2026-06-15","checkOutDate":"2026-06-17"}'
```

## 🏗️ Project Structure

```
src/main/java/com/hotel/stayease/
├── config/                    # OpenAPI configuration
├── controller/                # REST endpoints
├── dto/                        # Data Transfer Objects
├── model/                      # JPA entities
├── repository/                 # Spring Data JPA repositories
├── security/                   # JWT utilities & filters
├── service/                    # Business logic
│   └── mapper/                 # DTO mappers (manual)
└── DataLoader.java             # Seed data on startup

src/main/resources/
└── application.properties       # DB & JWT config

src/test/
└── java/com/hotel/stayease/service/
	└── BookingServiceTest.java  # Unit tests
```

## ⚙️ Configuration

### JWT Settings (`application.properties`)
```properties
jwt.secret=changeit-changeit-changeit-changeit-123456    # Change for production!
jwt.expiration-ms=86400000                                 # 24 hours
```

### Database Settings
```properties
spring.datasource.url=jdbc:h2:mem:stayease                # H2 in-memory
spring.jpa.hibernate.ddl-auto=update                       # Auto-create schema
```

## 🔒 Security Considerations

✅ **Implemented:**
- JWT-based stateless authentication
- Password hashing with BCrypt
- Role-based access control via `@PreAuthorize`
- Input validation on DTOs (email, password length, etc.)
- CSRF protection disabled for stateless API
- Global exception handler for consistent error JSON

⚠️ **For Production:**
- Load JWT secret from environment variables, not properties files
- Use HTTPS/TLS for all endpoints
- Implement rate limiting on auth endpoints
- Add audit logging for bookings & admin actions
- Use a secrets vault (AWS Secrets Manager, Vault, etc.)
- Add API key rotation

## 🎓 Key Features Demonstrated

### Foundation Skills
1. **REST API Design** - Clean controllers, proper HTTP methods & status codes
2. **Spring Boot** - Dependency injection, auto-configuration, profiles
3. **JPA/Hibernate** - Entity relationships, custom JPQL queries, lazy loading
4. **Authentication** - JWT generation, token validation, secure password storage
5. **Validation** - Jakarta validation annotations on DTOs
6. **Error Handling** - Global exception handler for structured error JSON
7. **Testing** - Unit tests with Mockito, service layer isolation
8. **Git Practices** - Feature branches, meaningful commits (expected)

## 🚀 Next Steps (Optional Enhancements)

- **DTO Mappers**: Replace manual mappers with MapStruct for reduced boilerplate
- **Integration Tests**: Add MockMvc tests for controller endpoints
- **Pagination**: Implement Spring Data Pageable for hotel search
- **Caching**: Add Redis cache for hotel search results
- **Search Filters**: Add more filters (city + star rating, price range, etc.)
- **Booking Confirmation Email**: Send confirmation emails on successful bookings
- **Manager Dashboard**: Endpoint to view upcoming bookings by hotel
- **Admin Analytics**: API for admin to view booking statistics
- **Frontend**: React/Angular UI to consume these APIs

## 📞 Support

For issues or questions:
1. Check the logs: Application startup logs show schema creation and JWT initialization
2. Verify JWT secret is set in `application.properties`
3. Ensure port 8080 is not in use: `netstat -ano | findstr ":8080"`
4. Review GlobalExceptionHandler for error response format
5. Check test results: `.\gradlew.bat test`

## 📄 License

This is a capstone project for Foundation Skills certification.
