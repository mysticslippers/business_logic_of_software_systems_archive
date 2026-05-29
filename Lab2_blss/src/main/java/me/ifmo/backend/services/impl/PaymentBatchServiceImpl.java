package me.ifmo.backend.services.impl;

import lombok.RequiredArgsConstructor;
import me.ifmo.backend.entities.Enrollment;
import me.ifmo.backend.entities.Payment;
import me.ifmo.backend.entities.enums.EnrollmentStatus;
import me.ifmo.backend.entities.enums.PaymentStatus;
import me.ifmo.backend.repositories.EnrollmentRepository;
import me.ifmo.backend.repositories.PaymentRepository;
import me.ifmo.backend.services.PaymentBatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentBatchServiceImpl implements PaymentBatchService {

    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public int expirePendingPaymentsBatch(int batchSize) {
        LocalDateTime now = LocalDateTime.now();

        List<Payment> expiredPayments = paymentRepository
                .findTop100ByStatusInAndExpiresAtBeforeOrderByExpiresAtAsc(
                        List.of(PaymentStatus.CREATED, PaymentStatus.PENDING),
                        now
                );

        if (batchSize > 0 && expiredPayments.size() > batchSize) {
            expiredPayments = expiredPayments.subList(0, batchSize);
        }

        for (Payment payment : expiredPayments) {
            payment.setStatus(PaymentStatus.EXPIRED);
            payment.setUpdatedAt(now);

            Enrollment enrollment = payment.getEnrollment();
            if (enrollment.getStatus() == EnrollmentStatus.PENDING_PAYMENT) {
                enrollment.setStatus(EnrollmentStatus.PAYMENT_EXPIRED);
                enrollment.setUpdatedAt(now);
                enrollmentRepository.save(enrollment);
            }

            paymentRepository.save(payment);
        }

        return expiredPayments.size();
    }
}