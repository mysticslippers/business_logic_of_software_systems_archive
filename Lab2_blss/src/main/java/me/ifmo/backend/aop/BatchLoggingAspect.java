package me.ifmo.backend.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class BatchLoggingAspect {

    @AfterReturning(
            pointcut = "execution(* me.ifmo.backend.schedulers.PaymentExpirationScheduler.expirePendingPayments(..))"
    )
    public void logSchedulerSuccess() {
        log.info("Payment expiration scheduler finished successfully");
    }

    @AfterThrowing(
            pointcut = "execution(* me.ifmo.backend.schedulers.PaymentExpirationScheduler.expirePendingPayments(..))",
            throwing = "exception"
    )
    public void logSchedulerError(Exception exception) {
        log.error("Payment expiration scheduler failed: {}", exception.getMessage(), exception);
    }

    @AfterReturning(
            pointcut = "execution(* me.ifmo.backend.services.PaymentBatchService.expirePendingPaymentsBatch(..))",
            returning = "result"
    )
    public void logBatchSuccess(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        Integer batchSize = null;

        if (args.length > 0 && args[0] instanceof Integer value) {
            batchSize = value;
        }

        log.info(
                "Expired payments batch finished: batchSize={}, processed={}",
                batchSize,
                result
        );
    }

    @AfterThrowing(
            pointcut = "execution(* me.ifmo.backend.services.PaymentBatchService.expirePendingPaymentsBatch(..))",
            throwing = "exception"
    )
    public void logBatchError(JoinPoint joinPoint, Exception exception) {
        Object[] args = joinPoint.getArgs();
        Integer batchSize = null;

        if (args.length > 0 && args[0] instanceof Integer value) {
            batchSize = value;
        }

        log.error(
                "Expired payments batch failed: batchSize={}, error={}",
                batchSize,
                exception.getMessage(),
                exception
        );
    }
}