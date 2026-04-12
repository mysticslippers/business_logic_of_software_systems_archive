package me.ifmo.backend.controllers;

import lombok.RequiredArgsConstructor;
import me.ifmo.backend.DTO.payment.PaymentDTO;
import me.ifmo.backend.mappers.PaymentMapper;
import me.ifmo.backend.services.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @PostMapping("/enrollment/{enrollmentId}")
    public PaymentDTO createPayment(@PathVariable Long enrollmentId) {
        return paymentMapper.toPaymentDTO(paymentService.createPayment(enrollmentId));
    }

    @GetMapping("/{id}")
    public PaymentDTO getPaymentById(@PathVariable Long id) {
        return paymentMapper.toPaymentDTO(paymentService.getPaymentById(id));
    }

    @GetMapping("/enrollment/{enrollmentId}")
    public PaymentDTO getPaymentByEnrollmentId(@PathVariable Long enrollmentId) {
        return paymentMapper.toPaymentDTO(paymentService.getPaymentByEnrollmentId(enrollmentId));
    }
}