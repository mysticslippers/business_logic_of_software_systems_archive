package me.ifmo.backend.services;

public interface PaymentBatchService {

    int expirePendingPaymentsBatch(int batchSize);
}