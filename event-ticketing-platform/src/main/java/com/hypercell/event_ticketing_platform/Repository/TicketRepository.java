package com.hypercell.event_ticketing_platform.Repository;

import com.hypercell.event_ticketing_platform.Entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

    List<TicketEntity> findByBookingUserId(Long userId);

    @Query("SELECT t.seat.id FROM TicketEntity t WHERE t.booking.event.id = :eventId AND t.booking.status <> 'CANCELLED' AND t.isBooked = true AND t.seat IS NOT NULL")
    List<Long> findBookedSeatIdsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(t) > 0 FROM TicketEntity t WHERE t.booking.event.id = :eventId AND t.seat.id IN :seatIds AND t.booking.status <> 'CANCELLED' AND t.isBooked = true")
    boolean areAnySeatsBookedForEvent(@Param("eventId") Long eventId, @Param("seatIds") List<Long> seatIds);
}