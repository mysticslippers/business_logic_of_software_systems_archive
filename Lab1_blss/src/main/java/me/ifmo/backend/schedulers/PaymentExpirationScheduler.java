package me.ifmo.backend.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ifmo.backend.services.PaymentCallbackService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentExpirationScheduler {

    private final PaymentCallbackService paymentCallbackService;

    @Scheduled(fixedRate = 60000)
    public void expirePendingPayments() {
        log.info("Starting expired payments check");

        paymentCallbackService.markExpiredPayments();

        log.info("Expired payments check finished");
    }
}