package com.hypercell.event_ticketing_platform.Repository;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hypercell.event_ticketing_platform.Entity.EventEntity;
import com.hypercell.event_ticketing_platform.Enum.EventStatus;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {

    @Query("SELECT e FROM EventEntity e WHERE e.status = :status " +
            "AND (:category IS NULL OR e.category = :category) " +
            "AND (cast(:startDate as timestamp) IS NULL OR e.startDate >= :startDate)")
        Page<EventEntity> findPublicEvents(
                        @Param("status") EventStatus status,
                        @Param("category") String category,
                        @Param("startDate") LocalDateTime startDate,
                        Pageable pageable);
        // معنى @Param("status") بالبلدي جداً:

        // "يا سبرنج، النص اللي جوه الاستعلام وفيه :status، اربطه بالمتغير status اللي
        // مكتوب هنا ده."
        // ده المنفذ اللي بيمسك الشروط دي ويبعتها للاستعلام اللي فوق عشان يتنفذ، ومعاهم
        // أداة الـ Pageable عشان ترجع النتائج مقسمة صفحات (Pagination) وما تتقلش
        // الموقنع.
        Optional<EventEntity> findByIdAndOrganizerId(Long id, Long organizerId);
}
