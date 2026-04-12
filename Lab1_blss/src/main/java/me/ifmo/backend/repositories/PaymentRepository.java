package me.ifmo.backend.repositories;

import me.ifmo.backend.entities.Payment;
import me.ifmo.backend.entities.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByEnrollmentId(Long enrollmentId);

    Optional<Payment> findByProviderPaymentId(String providerPaymentId);

    List<Payment> findAllByStatusInAndExpiresAtBefore(List<PaymentStatus> statuses, LocalDateTime time);
}