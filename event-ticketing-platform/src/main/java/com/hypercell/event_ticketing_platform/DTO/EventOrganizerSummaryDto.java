package com.hypercell.event_ticketing_platform.DTO;

import java.time.LocalDateTime;
import java.util.List;

public class EventOrganizerSummaryDto {
    private Long eventId;
    private String eventTitle;
    private Integer totalTicketsSold;
    private List<BookingUserDetailDto> bookings;

    public EventOrganizerSummaryDto() {}

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public Integer getTotalTicketsSold() { return totalTicketsSold; }
    public void setTotalTicketsSold(Integer totalTicketsSold) { this.totalTicketsSold = totalTicketsSold; }

    public List<BookingUserDetailDto> getBookings() { return bookings; }
    public void setBookings(List<BookingUserDetailDto> bookings) { this.bookings = bookings; }

    public static class BookingUserDetailDto {
        private Long bookingId;
        private Long userId;
        private String userName;
        private String userEmail;
        private Integer quantity;
        private LocalDateTime bookingDate;

        public BookingUserDetailDto() {}

        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public LocalDateTime getBookingDate() { return bookingDate; }
        public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
    }
}