package me.ifmo.backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.ifmo.backend.DTO.payment.PaymentWebhookRequest;
import me.ifmo.backend.exceptions.BusinessException;
import me.ifmo.backend.services.PaymentCallbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments/webhook")
public class PaymentWebhookController {

    private final PaymentCallbackService paymentCallbackService;

    @PostMapping
    public ResponseEntity<Void> handlePaymentWebhook(@RequestBody @Valid PaymentWebhookRequest request) {
        String status = request.getStatus().trim().toUpperCase();

        switch (status) {
            case "PAID" -> paymentCallbackService.markPaymentAsPaid(request.getProviderPaymentId());
            case "FAILED" -> paymentCallbackService.markPaymentAsFailed(
                    request.getProviderPaymentId(),
                    request.getFailureReason()
            );
            default -> throw new BusinessException("Unsupported payment status: " + request.getStatus());
        }

        return ResponseEntity.ok().build();
    }
}