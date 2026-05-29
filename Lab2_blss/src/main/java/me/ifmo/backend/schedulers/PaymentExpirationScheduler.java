package me.ifmo.backend.schedulers;

import lombok.RequiredArgsConstructor;
import me.ifmo.backend.services.PaymentBatchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentExpirationScheduler {

    private final PaymentBatchService paymentBatchService;

    @Scheduled(fixedRate = 60000)
    public void expirePendingPayments() {
        paymentBatchService.expirePendingPaymentsBatch(100);
    }
}