package me.ifmo.backend.integration.bank;

import me.ifmo.backend.integration.bank.DTO.BankPaymentRequest;
import me.ifmo.backend.integration.bank.DTO.BankPaymentResponse;

public interface BankClient {

    BankPaymentResponse createPayment(BankPaymentRequest request);
}