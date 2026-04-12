package me.ifmo.backend.DTO.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentWebhookRequest {

    @NotBlank(message = "Provider payment id must not be blank")
    private String providerPaymentId;

    @NotBlank(message = "Status must not be blank")
    private String status;

    private String failureReason;
}