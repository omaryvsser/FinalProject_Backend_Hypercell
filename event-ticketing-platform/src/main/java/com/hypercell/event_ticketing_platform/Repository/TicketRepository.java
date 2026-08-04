package com.hypercell.event_ticketing_platform.Repository;

import com.hypercell.event_ticketing_platform.Entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

    List<TicketEntity> findByBookingUserId(Long userId);
}