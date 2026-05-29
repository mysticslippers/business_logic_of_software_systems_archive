package me.ifmo.backend.aop;

import lombok.extern.slf4j.Slf4j;
import me.ifmo.backend.integration.bank.DTO.BankPaymentRequest;
import me.ifmo.backend.integration.bank.DTO.BankPaymentResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class BankClientLoggingAspect {

    @AfterReturning(
            pointcut = "execution(* me.ifmo.backend.integration.bank.BankClient.createPayment(..))",
            returning = "result"
    )
    public void logCreatePaymentSuccess(JoinPoint joinPoint, BankPaymentResponse result) {
        Object[] args = joinPoint.getArgs();

        if (args.length > 0 && args[0] instanceof BankPaymentRequest request) {
            log.info(
                    "Payment created in bank: enrollmentId={}, providerPaymentId={}",
                    request.getEnrollmentId(),
                    result.getProviderPaymentId()
            );
        }
    }

    @AfterThrowing(
            pointcut = "execution(* me.ifmo.backend.integration.bank.BankClient.createPayment(..))",
            throwing = "exception"
    )
    public void logCreatePaymentError(JoinPoint joinPoint, Exception exception) {
        Object[] args = joinPoint.getArgs();

        if (args.length > 0 && args[0] instanceof BankPaymentRequest request) {
            log.error(
                    "Failed to create payment in bank for enrollmentId={}: {}",
                    request.getEnrollmentId(),
                    exception.getMessage(),
                    exception
            );
        }
    }
}