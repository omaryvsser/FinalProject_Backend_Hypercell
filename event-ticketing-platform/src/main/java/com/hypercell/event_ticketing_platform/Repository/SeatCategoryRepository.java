package com.hypercell.event_ticketing_platform.Repository;

import com.hypercell.event_ticketing_platform.Entity.SeatCategoryEntity;
import com.hypercell.event_ticketing_platform.Enum.SeatCategoryName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatCategoryRepository extends JpaRepository<SeatCategoryEntity, Long> {

    List<SeatCategoryEntity> findByEventId(Long eventId);

    Optional<SeatCategoryEntity> findByIdAndEventId(Long id, Long eventId);

    boolean existsByEventIdAndName(Long eventId, SeatCategoryName name);
}
