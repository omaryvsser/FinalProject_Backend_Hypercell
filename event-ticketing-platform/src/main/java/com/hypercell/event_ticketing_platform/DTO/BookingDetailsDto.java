package com.hypercell.event_ticketing_platform.DTO;

import java.time.LocalDateTime;

// =========================================================
// [ADDED FOR ORGANIZER FEATURE] - DTO for individual booking details
// =========================================================
public class BookingDetailsDto {
    private Long bookingId;
    private Long userId;
    private String userName;
    private String userEmail;
    private Integer seatsBooked;
    private LocalDateTime bookingTime;

    public BookingDetailsDto(Long bookingId, Long userId, String userName, String userEmail, Integer seatsBooked, LocalDateTime bookingTime) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.seatsBooked = seatsBooked;
        this.bookingTime = bookingTime;
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public Integer getSeatsBooked() { return seatsBooked; }
    public void setSeatsBooked(Integer seatsBooked) { this.seatsBooked = seatsBooked; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }
}