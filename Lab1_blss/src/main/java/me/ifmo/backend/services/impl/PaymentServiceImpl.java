package me.ifmo.backend.services.impl;

import lombok.RequiredArgsConstructor;
import me.ifmo.backend.entities.Enrollment;
import me.ifmo.backend.entities.Payment;
import me.ifmo.backend.entities.enums.EnrollmentStatus;
import me.ifmo.backend.entities.enums.PaymentStatus;
import me.ifmo.backend.exceptions.BusinessException;
import me.ifmo.backend.exceptions.NotFoundException;
import me.ifmo.backend.integration.bank.BankClient;
import me.ifmo.backend.integration.bank.DTO.BankPaymentRequest;
import me.ifmo.backend.integration.bank.DTO.BankPaymentResponse;
import me.ifmo.backend.repositories.EnrollmentRepository;
import me.ifmo.backend.repositories.PaymentRepository;
import me.ifmo.backend.services.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final BankClient bankClient;

    @Value("${bank.callback-url}")
    private String callbackUrl;

    @Override
    public Payment createPayment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new NotFoundException(
                        "Enrollment with id " + enrollmentId + " not found"
                ));

        if (paymentRepository.findByEnrollmentId(enrollmentId).isPresent()) {
            throw new BusinessException(
                    "Payment for enrollment with id " + enrollmentId + " already exists"
            );
        }

        if (enrollment.getStatus() != EnrollmentStatus.PENDING_PAYMENT) {
            throw new BusinessException(
                    "Payment can only be created for enrollment in PENDING_PAYMENT status"
            );
        }

        BankPaymentRequest bankPaymentRequest = BankPaymentRequest.builder()
                .enrollmentId(enrollment.getId())
                .amount(enrollment.getCourse().getPrice())
                .currency(enrollment.getCourse().getCurrency())
                .callbackUrl(callbackUrl)
                .build();

        BankPaymentResponse bankPaymentResponse = bankClient.createPayment(bankPaymentRequest);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(1);

        Payment payment = Payment.builder()
                .enrollment(enrollment)
                .providerPaymentId(bankPaymentResponse.getProviderPaymentId())
                .amount(enrollment.getCourse().getPrice())
                .currency(enrollment.getCourse().getCurrency())
                .status(PaymentStatus.CREATED)
                .paymentUrl(bankPaymentResponse.getPaymentUrl())
                .expiresAt(expiresAt)
                .createdAt(now)
                .updatedAt(now)
                .build();

        enrollment.setPaymentExpiresAt(expiresAt);
        enrollment.setUpdatedAt(now);

        enrollmentRepository.save(enrollment);

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Payment with id " + id + " not found"
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentByEnrollmentId(Long enrollmentId) {
        return paymentRepository.findByEnrollmentId(enrollmentId)
                .orElseThrow(() -> new NotFoundException(
                        "Payment for enrollment with id " + enrollmentId + " not found"
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentByProviderPaymentId(String providerPaymentId) {
        return paymentRepository.findByProviderPaymentId(providerPaymentId)
                .orElseThrow(() -> new NotFoundException(
                        "Payment with provider payment id " + providerPaymentId + " not found"
                ));
    }
}