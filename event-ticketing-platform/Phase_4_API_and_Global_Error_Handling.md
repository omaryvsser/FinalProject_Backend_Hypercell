# 🎓 Capstone Defense Prep — Phase 4: API Layer & Global Error Handling

**Project Name:** Event Ticketing Platform  
**Target Audience:** Capstone Defense Panel & Technical Evaluators  
**Author:** Senior Java/Spring Boot Architect  

---

## 🌐 1. REST Controllers & API Design

The API Layer acts as the public boundary between the Angular frontend and the backend Java service ecosystem.

```
[ Angular Client ]
       |
       |  1. HTTP POST /api/bookings (JSON Payload)
       v
[ BookingController (@RestController) ]
       |
       |  2. Enforces @Valid DTO payload
       v
[ BookingService (@Service) ]
       |
       |  3. Processes business logic
       v
[ Database / PostgreSQL ]
```

### Core REST Controller Endpoints:

1. **[AuthController.java](file:///Users/macbookpro/Documents/DATA/Hypercell%20Intern/FINAL%20PROJECT/Backend/FinalProject_Backend_Hypercell/event-ticketing-platform/src/main/java/com/hypercell/event_ticketing_platform/Controller/AuthController.java)**
   - `POST /api/v1/auth/register` $\rightarrow$ Registers new customer/user account (`200 OK`).
   - `POST /api/v1/auth/login` $\rightarrow$ Authenticates credentials and returns JWT bearer token (`200 OK`).
2. **[EventPublicController.java](file:///Users/macbookpro/Documents/DATA/Hypercell%20Intern/FINAL%20PROJECT/Backend/FinalProject_Backend_Hypercell/event-ticketing-platform/src/main/java/com/hypercell/event_ticketing_platform/Controller/EventPublicController.java)**
   - `GET /api/public/events` $\rightarrow$ Returns paginated list of published events with category & date filtering (`200 OK`).
   - `GET /api/public/events/{id}` $\rightarrow$ Returns single event details and seat tier availability (`200 OK`).
3. **[EventManagementController.java](file:///Users/macbookpro/Documents/DATA/Hypercell%20Intern/FINAL%20PROJECT/Backend/FinalProject_Backend_Hypercell/event-ticketing-platform/src/main/java/com/hypercell/event_ticketing_platform/Controller/EventManagementController.java)**
   - `POST /api/v1/events` $\rightarrow$ Creates new event (`201 CREATED`).
   - `PUT /api/v1/events/{id}` $\rightarrow$ Updates existing event details (`200 OK`).
   - `PATCH /api/v1/events/{id}/status` $\rightarrow$ Transitions event status (`DRAFT`, `PUBLISHED`, `CANCELLED`) (`200 OK`).
   - `DELETE /api/v1/events/{id}` $\rightarrow$ Deletes event & linked records (`204 NO CONTENT`).
4. **[VenueController.java](file:///Users/macbookpro/Documents/DATA/Hypercell%20Intern/FINAL%20PROJECT/Backend/FinalProject_Backend_Hypercell/event-ticketing-platform/src/main/java/com/hypercell/event_ticketing_platform/Controller/VenueController.java)**
   - `GET /api/venues` $\rightarrow$ Lists all venues (`200 OK`).
   - `POST /api/venues` $\rightarrow$ Creates new physical venue (`201 CREATED`).
   - `PUT /api/venues/{id}` $\rightarrow$ Updates venue details (`200 OK`).
   - `DELETE /api/venues/{id}` $\rightarrow$ Deletes venue (`204 NO CONTENT`).
5. **[SeatCategoryController.java](file:///Users/macbookpro/Documents/DATA/Hypercell%20Intern/FINAL%20PROJECT/Backend/FinalProject_Backend_Hypercell/event-ticketing-platform/src/main/java/com/hypercell/event_ticketing_platform/Controller/SeatCategoryController.java)**
   - `GET /api/seat-categories/event/{eventId}` $\rightarrow$ Returns seat categories for event (`200 OK`).
   - `POST /api/seat-categories` $\rightarrow$ Adds seat category tier to event (`201 CREATED`).
   - `PUT /api/seat-categories/{id}` $\rightarrow$ Modifies seat category capacity/price (`200 OK`).
6. **[BookingController.java](file:///Users/macbookpro/Documents/DATA/Hypercell%20Intern/FINAL%20PROJECT/Backend/FinalProject_Backend_Hypercell/event-ticketing-platform/src/main/java/com/hypercell/event_ticketing_platform/Controller/BookingController.java)**
   - `POST /api/bookings` $\rightarrow$ Creates seat booking transaction (`200 OK`).
   - `PATCH /api/bookings/{id}/cancel` $\rightarrow$ Cancels booking and restores capacity (`200 OK`).
   - `GET /api/bookings/user/{userId}` $\rightarrow$ Lists bookings for user (`200 OK`).
   - `GET /api/bookings/event/{eventId}` $\rightarrow$ Lists bookings for event (`200 OK`).
7. **[PaymentController.java](file:///Users/macbookpro/Documents/DATA/Hypercell%20Intern/FINAL%20PROJECT/Backend/FinalProject_Backend_Hypercell/event-ticketing-platform/src/main/java/com/hypercell/event_ticketing_platform/Controller/PaymentController.java)**
   - `POST /api/payments/process` $\rightarrow$ Processes booking payment and triggers QR ticket issuance (`200 OK`).
8. **[TicketController.java](file:///Users/macbookpro/Documents/DATA/Hypercell%20Intern/FINAL%20PROJECT/Backend/FinalProject_Backend_Hypercell/event-ticketing-platform/src/main/java/com/hypercell/event_ticketing_platform/Controller/TicketController.java)**
   - `GET /api/tickets/user/{userId}` $\rightarrow$ Returns customer ticket pass list with QR validation codes (`200 OK`).
9. **[FileUploadController.java](file:///Users/macbookpro/Documents/DATA/Hypercell%20Intern/FINAL%20PROJECT/Backend/FinalProject_Backend_Hypercell/event-ticketing-platform/src/main/java/com/hypercell/event_ticketing_platform/Controller/FileUploadController.java)**
   - `POST /api/v1/files/upload` $\rightarrow$ Stores multipart event poster image and returns public static access URL (`200 OK`).
10. **[UserManagementController.java](file:///Users/macbookpro/Documents/DATA/Hypercell%20Intern/FINAL%20PROJECT/Backend/FinalProject_Backend_Hypercell/event-ticketing-platform/src/main/java/com/hypercell/event_ticketing_platform/Controller/UserManagementController.java)**
    - `GET /api/admin/users` $\rightarrow$ Lists all system users (`200 OK`).
    - `PUT /api/admin/users/{id}/role` $\rightarrow$ Updates user role with admin self-demotion protection (`200 OK`).

---

## ⚠️ 2. Global Exception Handling Architecture

💡 **Analogy for Defense Panel:**
> Without global exception handling, an unexpected database error dumps a messy, unreadable Java stack trace (`500 Internal Server Error`) to the user. **Global Exception Handling** acts as an *API Translation Layer* — catching internal exceptions and converting them into clean, standardized JSON error messages that the Angular frontend can gracefully display to users.

```
                 [ Backend Exception Occurs ]
                              |
                              v
             [ GlobalExceptionHandler (@RestControllerAdvice) ]
                              |
     +------------------------+------------------------+
     |                        |                        |
     v                        v                        v
[ BadCredentialsException ] [ ResourceNotFound ]   [ Validation Error ]
     |                        |                        |
     v                        v                        v
 401 Unauthorized         404 Not Found            400 Bad Request
 (Invalid Credentials)    (Event Not Found)        (Field Errors Map)
```

---

## 📋 3. Centralized Exception Handler Implementation

In [GlobalExceptionHandler.java](file:///Users/macbookpro/Documents/DATA/Hypercell%20Intern/FINAL%20PROJECT/Backend/FinalProject_Backend_Hypercell/event-ticketing-platform/src/main/java/com/hypercell/event_ticketing_platform/Exception/GlobalExceptionHandler.java), the `@RestControllerAdvice` annotation intercepts all exceptions thrown anywhere across Controllers or Services.

### Complete Exception-to-HTTP Status Mapping Table:

| Java Exception Class | HTTP Status Code | Frontend User Response |
| :--- | :---: | :--- |
| `BadCredentialsException` | `401 UNAUTHORIZED` | `"Invalid email or password"` |
| `AuthenticationException` | `401 UNAUTHORIZED` | `"Authentication failed"` |
| `AccessDeniedException` | `403 FORBIDDEN` | `"You do not have permission to access this resource"` |
| `ResourceNotFoundException` | `404 NOT FOUND` | `"Resource not found with id: X"` |
| `UserAlreadyExistsException` | `409 CONFLICT` | `"A user with this email/username already exists"` |
| `ResourceAlreadyExistsException` | `409 CONFLICT` | `"Seat category VIP already exists for event ID: X"` |
| `DataIntegrityViolationException` | `409 CONFLICT` | `"A database constraint violation occurred"` |
| `MethodArgumentNotValidException` | `400 BAD REQUEST` | Maps invalid field names to error messages (e.g. `{"quantity": "Must be at least 1"}`) |
| `IllegalArgumentException` | `400 BAD REQUEST` | `"Cannot reduce total seats below already booked count"` |
| `Exception` (Generic Fallback) | `500 INTERNAL SERVER ERROR` | `"An unexpected error occurred"` |

### Standardized JSON Error Structure:
Every error response returned to the frontend adheres to a strict, predictable JSON format:

```json
{
  "timestamp": "2026-08-10T22:41:35.123",
  "status": 404,
  "error": "Not Found",
  "message": "Event not found with id: 999",
  "path": "/api/v1/events/999"
}
```

---

### Summary Checklist for Phase 4 Defense Questions:
- [x] **REST Best Practices:** Meaningful HTTP verbs (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`) and proper HTTP status codes (`200`, `201`, `204`).
- [x] **Input Validation:** `@Valid` triggers Jakarta Bean Validation constraints on DTO parameters.
- [x] **Global Exception Handling:** `@RestControllerAdvice` translates runtime Java exceptions into clean, uniform JSON responses for frontend consumption.
