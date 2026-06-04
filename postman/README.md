# StayEase Backend — Postman Collections (Controller-wise)

**Base URL:** `http://localhost:8081`

Create a Postman Environment with:
- `baseUrl` = `http://localhost:8081`
- `token`   = (auto-set by the Login request's test script)

All non-public endpoints require header: `Authorization: Bearer {{token}}`

---

## 1) AuthController — `/api/auth` (Public)

### 1.1 Register
- **POST** `{{baseUrl}}/api/auth/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```
- **Response:** `201 Created` — returns email string.
- **Validation:** `name` not blank; valid `email`; `password` min 6 chars.

### 1.2 Login
- **POST** `{{baseUrl}}/api/auth/login`
- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```
- **Response:** `200 OK` — `{ "token": "<JWT>" }`
- **Tests tab (auto-save token):**
```js
const j = pm.response.json();
if (j.token) pm.environment.set("token", j.token);
```

---

## 2) HotelController — `/api/hotels`

GETs are **public**; POST/PUT/DELETE require **ROLE_ADMIN**.

### 2.1 Search Hotels (Public)
- **GET** `{{baseUrl}}/api/hotels?city=Mumbai&minStars=3&page=0&size=20`
- **Query Params:**
  - `city` (required)
  - `minStars` (optional, integer)
  - `page` (optional, default `0`)
  - `size` (optional, default `20`)
- **Response:** `200 OK` — list of `HotelDto`.

### 2.2 Get Available Rooms in Hotel (Public)
- **GET** `{{baseUrl}}/api/hotels/{id}/rooms?checkIn=2026-06-10&checkOut=2026-06-15`
- **Response:** `200 OK` — list of `RoomDto`.

### 2.3 Create Hotel (ADMIN)
- **POST** `{{baseUrl}}/api/hotels`
- **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body:**
```json
{
  "name": "Grand Plaza",
  "city": "Mumbai",
  "starRating": 5,
  "description": "Luxury hotel in the heart of the city",
  "coverImageUrl": "https://example.com/cover.jpg",
  "managerId": 2
}
```
- **Response:** `201 Created` — `HotelDto`.

### 2.4 Update Hotel (ADMIN)
- **PUT** `{{baseUrl}}/api/hotels/{id}`
- **Headers:** same as create.
- **Body:** same shape as create.
- **Response:** `200 OK` — updated `HotelDto`.

### 2.5 Delete Hotel (ADMIN)
- **DELETE** `{{baseUrl}}/api/hotels/{id}`
- **Headers:** `Authorization: Bearer {{token}}`
- **Response:** `204 No Content`.

---

## 3) RoomController — `/api/hotels/{hotelId}/rooms` & `/api/rooms/*`

All require **ROLE_MANAGER**, and the manager must own the hotel (`hotel.managerId == user.id`).

### 3.1 Create Room in Hotel (MANAGER)
- **POST** `{{baseUrl}}/api/hotels/{hotelId}/rooms`
- **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body:**
```json
{
  "roomNumber": "101",
  "roomType": "DOUBLE",
  "pricePerNight": 2499.00,
  "maxOccupancy": 2,
  "description": "Sea-view double room",
  "imageUrl": "https://example.com/room101.jpg",
  "active": true
}
```
- **Response:** `201 Created` — `RoomDto`. `403` if not the hotel manager.

### 3.2 Update Room (MANAGER)
- **PUT** `{{baseUrl}}/api/rooms/{id}`
- **Body:**
```json
{
  "roomNumber": "101",
  "roomType": "SUITE",
  "pricePerNight": 4999.00,
  "maxOccupancy": 3,
  "description": "Upgraded to suite",
  "imageUrl": "https://example.com/room101.jpg",
  "active": true
}
```
- **Response:** `200 OK` — updated `RoomDto`.

### 3.3 Delete Room (MANAGER)
- **DELETE** `{{baseUrl}}/api/rooms/{id}`
- **Response:** `204 No Content`.

### 3.4 Toggle Room Active Status (MANAGER)
- **PATCH** `{{baseUrl}}/api/rooms/{id}/status`
- **Body:** none
- **Response:** `200 OK` — `RoomDto` with `active` flipped.

---

## 4) BookingController — `/api/bookings` (Authenticated)

### 4.1 Create Booking
- **POST** `{{baseUrl}}/api/bookings`
- **Headers:** `Authorization: Bearer {{token}}`, `Content-Type: application/json`
- **Body:**
```json
{
  "roomId": 10,
  "checkInDate": "2026-06-10",
  "checkOutDate": "2026-06-15"
}
```
- **Validation:** `checkInDate` must be today or future.
- **Response:** `201 Created` — `BookingDto`.

### 4.2 Get My Bookings
- **GET** `{{baseUrl}}/api/bookings/mine`
- **Response:** `200 OK` — list of `BookingDto` for current user.

### 4.3 Cancel Booking
- **PUT** `{{baseUrl}}/api/bookings/{id}/cancel`
- **Response:** `204 No Content`. Only the booking owner may cancel.

---

## 5) Endpoint Matrix

| Controller        | Method | Path                                          | Auth                    |
|-------------------|--------|-----------------------------------------------|-------------------------|
| AuthController    | POST   | `/api/auth/register`                          | Public                  |
| AuthController    | POST   | `/api/auth/login`                             | Public                  |
| HotelController   | GET    | `/api/hotels?city=...`                        | Public                  |
| HotelController   | GET    | `/api/hotels/{id}/rooms?checkIn=&checkOut=`   | Public                  |
| HotelController   | POST   | `/api/hotels`                                 | ROLE_ADMIN              |
| HotelController   | PUT    | `/api/hotels/{id}`                            | ROLE_ADMIN              |
| HotelController   | DELETE | `/api/hotels/{id}`                            | ROLE_ADMIN              |
| RoomController    | POST   | `/api/hotels/{hotelId}/rooms`                 | ROLE_MANAGER (owner)    |
| RoomController    | PUT    | `/api/rooms/{id}`                             | ROLE_MANAGER (owner)    |
| RoomController    | DELETE | `/api/rooms/{id}`                             | ROLE_MANAGER (owner)    |
| RoomController    | PATCH  | `/api/rooms/{id}/status`                      | ROLE_MANAGER (owner)    |
| BookingController | POST   | `/api/bookings`                               | Authenticated           |
| BookingController | GET    | `/api/bookings/mine`                          | Authenticated           |
| BookingController | PUT    | `/api/bookings/{id}/cancel`                   | Authenticated (owner)   |

---

## 6) How to Import the Collection

1. Open Postman → **Import**.
2. Choose the file `StayEase.postman_collection.json` (in this folder).
3. Create an environment with `baseUrl` and `token` variables.
4. Run **Auth → Login** first; the test script auto-populates `{{token}}`.

### Notes
- Server port is **8081** (see `src/main/resources/application.properties`).
- Roles in Spring are stored as `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_USER`; `@PreAuthorize("hasRole('ADMIN')")` matches `ROLE_ADMIN`.
- Dates use ISO format `YYYY-MM-DD`.
- H2 console available at `/h2-console` (may require security config tweak to access without auth).

