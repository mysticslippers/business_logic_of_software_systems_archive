package me.ifmo.backend.integration.bank;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ifmo.backend.exceptions.BusinessException;
import me.ifmo.backend.integration.bank.DTO.BankPaymentRequest;
import me.ifmo.backend.integration.bank.DTO.BankPaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpBankClient implements BankClient {

    private final RestTemplate restTemplate;

    @Value("${bank.base-url}")
    private String bankBaseUrl;

    @Override
    public BankPaymentResponse createPayment(BankPaymentRequest request) {
        String url = bankBaseUrl + "/api/bank/payments";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<BankPaymentRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<BankPaymentResponse> response = restTemplate.postForEntity(
                    url,
                    entity,
                    BankPaymentResponse.class
            );

            BankPaymentResponse body = response.getBody();

            if (body == null) {
                throw new BusinessException("Bank returned empty response when creating payment");
            }

            log.info(
                    "Payment created in bank: enrollmentId={}, providerPaymentId={}",
                    request.getEnrollmentId(),
                    body.getProviderPaymentId()
            );

            return body;
        } catch (RestClientException exception) {
            log.error(
                    "Failed to create payment in bank for enrollmentId={}: {}",
                    request.getEnrollmentId(),
                    exception.getMessage(),
                    exception
            );
            throw new BusinessException("Failed to create payment in external bank service");
        }
    }
}