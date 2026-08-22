package com.hypercell.event_ticketing_platform.Repository;

import com.hypercell.event_ticketing_platform.Entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<SeatEntity, Long> {

    List<SeatEntity> findByVenueIdOrderByRowNameAscSeatNumberAsc(Long venueId);

    List<SeatEntity> findByIdIn(List<Long> ids);

    boolean existsByVenueId(Long venueId);

    long countByVenueId(Long venueId);

    Optional<SeatEntity> findByVenueIdAndSeatCode(Long venueId, String seatCode);
}
