package com.hypercell.event_ticketing_platform.Entity;

import com.hypercell.event_ticketing_platform.Enum.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BookingStatus status;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_category_id", nullable = false)
    private SeatCategoryEntity seatCategory;

    // 🟢 Changed from List to Set to prevent Hibernate duplicate joins/records
    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TicketEntity> tickets = new HashSet<>();

    // Helper method to synchronize both sides of bidirectional relation cleanly
    public void addTicket(TicketEntity ticket) {
        if (this.tickets == null) {
            this.tickets = new HashSet<>();
        }
        this.tickets.add(ticket);
        ticket.setBooking(this);
    }
}