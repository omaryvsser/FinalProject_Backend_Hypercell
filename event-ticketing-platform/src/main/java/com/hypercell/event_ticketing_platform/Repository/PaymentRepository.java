package com.hypercell.event_ticketing_platform.Repository;

import com.hypercell.event_ticketing_platform.Entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByBookingId(Long bookingId);
}