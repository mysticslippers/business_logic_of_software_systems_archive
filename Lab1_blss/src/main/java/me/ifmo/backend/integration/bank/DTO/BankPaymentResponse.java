package me.ifmo.backend.integration.bank.DTO;

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
public class BankPaymentResponse {

    private String providerPaymentId;
    private String paymentUrl;
}