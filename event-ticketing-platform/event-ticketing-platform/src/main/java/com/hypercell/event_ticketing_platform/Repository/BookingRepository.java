package com.hypercell.event_ticketing_platform.Repository;

import com.hypercell.event_ticketing_platform.Entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    // دول عشان نقدر نبحث بيهم عن حجوزات اليوزر أو الإيفنت
    List<BookingEntity> findByUserId(Long userId);

    List<BookingEntity> findByEventId(Long eventId);
}