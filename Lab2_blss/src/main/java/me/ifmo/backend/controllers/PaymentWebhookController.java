package me.ifmo.backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.ifmo.backend.DTO.payment.PaymentWebhookRequest;
import me.ifmo.backend.exceptions.BusinessException;
import me.ifmo.backend.services.impl.PaymentTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments/webhook")
public class PaymentWebhookController {

    private final PaymentTransactionService paymentTransactionService;

    @PostMapping
    @PreAuthorize("hasAuthority('PAYMENT_CALLBACK_HANDLE')")
    public ResponseEntity<Void> handlePaymentWebhook(@RequestBody @Valid PaymentWebhookRequest request) {
        String status = request.getStatus().trim().toUpperCase();

        switch (status) {
            case "PAID" -> paymentTransactionService.processPaidWebhook(
                    request.getProviderPaymentId()
            );
            case "FAILED" -> paymentTransactionService.processFailedWebhook(
                    request.getProviderPaymentId(),
                    request.getFailureReason()
            );
            default -> throw new BusinessException("Unsupported payment status: " + request.getStatus());
        }

        return ResponseEntity.ok().build();
    }
}