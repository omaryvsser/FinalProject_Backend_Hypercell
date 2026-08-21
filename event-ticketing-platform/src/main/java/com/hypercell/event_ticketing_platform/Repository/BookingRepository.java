package com.hypercell.event_ticketing_platform.Repository;

import com.hypercell.event_ticketing_platform.Entity.BookingEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    List<BookingEntity> findByUserId(Long userId);

    List<BookingEntity> findByEventId(Long eventId);

    @Query("SELECT b FROM BookingEntity b WHERE b.event.organizer.id = :organizerId")
    Page<BookingEntity> findByOrganizerId(@Param("organizerId") Long organizerId, Pageable pageable);

    @Query("SELECT COUNT(b) FROM BookingEntity b WHERE b.event.organizer.id = :organizerId")
    long countByOrganizerId(@Param("organizerId") Long organizerId);
}

