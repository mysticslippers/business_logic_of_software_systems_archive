package me.ifmo.backend.DTO.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Size(max = 100, message = "Provider payment id must not be longer than 100 characters")
    private String providerPaymentId;

    @NotBlank(message = "Status must not be blank")
    @Size(max = 32, message = "Status must not be longer than 32 characters")
    private String status;

    @Size(max = 255, message = "Failure reason must not be longer than 255 characters")
    private String failureReason;
}