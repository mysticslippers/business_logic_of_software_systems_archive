package me.ifmo.backend.mappers;

import me.ifmo.backend.DTO.payment.PaymentDTO;
import me.ifmo.backend.entities.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "enrollmentId", source = "enrollment.id")
    PaymentDTO toPaymentDTO(Payment payment);
}