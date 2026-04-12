package me.ifmo.backend.services;

public interface PaymentCallbackService {

    void markPaymentAsPaid(String providerPaymentId);

    void markPaymentAsFailed(String providerPaymentId, String failureReason);

    void markExpiredPayments();
}