package me.ifmo.backend.services;

import me.ifmo.backend.entities.Payment;

public interface PaymentService {

    Payment createPayment(Long enrollmentId);

    Payment getPaymentById(Long id);

    Payment getPaymentByEnrollmentId(Long enrollmentId);
}