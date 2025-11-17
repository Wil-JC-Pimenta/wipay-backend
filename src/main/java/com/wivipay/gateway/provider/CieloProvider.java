package com.wivipay.gateway.provider;

import com.wivipay.gateway.dto.PaymentRequest;
import com.wivipay.gateway.dto.PaymentResponse;
import com.wivipay.gateway.model.PaymentTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CieloProvider implements PaymentProvider {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String merchantId;
    private final String merchantKey;

    public CieloProvider(
            RestTemplate restTemplate,
            @Value("${cielo.api.url:https://apisandbox.cieloecommerce.cielo.com.br}") String apiUrl,
            @Value("${cielo.merchant.id}") String merchantId,
            @Value("${cielo.merchant.key}") String merchantKey) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.merchantId = merchantId;
        this.merchantKey = merchantKey;
    }

    @Override
    public String getName() {
        return "cielo";
    }

    @Override
    @SuppressWarnings({"unchecked", "null"})
    public PaymentResponse authorize(PaymentRequest request) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("MerchantOrderId", request.getCustomerId());
            payload.put("Payment", Map.of(
                "Type", "CreditCard",
                "Amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue(),
                "Currency", request.getCurrency(),
                "Installments", 1,
                "CreditCard", Map.of(
                    "CardToken", request.getPaymentMethod(),
                    "Brand", "Visa"
                ),
                "Capture", false
            ));

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                apiUrl + "/1/sales",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Resposta vazia da API Cielo");
            }
            Map<String, Object> payment = (Map<String, Object>) responseBody.get("Payment");

            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setProvider("cielo");
            paymentResponse.setProviderTransactionId(payment.get("PaymentId").toString());
            paymentResponse.setAmount(request.getAmount());
            paymentResponse.setCurrency(request.getCurrency());
            paymentResponse.setStatus(PaymentTransaction.PaymentStatus.AUTHORIZED);
            paymentResponse.setPaymentMethod(request.getPaymentMethod());

            return paymentResponse;
        } catch (Exception e) {
            log.error("Erro ao autorizar pagamento na Cielo", e);
            throw new RuntimeException("Erro ao processar pagamento: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "null"})
    public PaymentResponse capture(String transactionId) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                apiUrl + "/1/sales/" + transactionId + "/capture",
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Resposta vazia da API Cielo");
            }
            Map<String, Object> payment = (Map<String, Object>) responseBody.get("Payment");

            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setProvider("cielo");
            paymentResponse.setProviderTransactionId(transactionId);
            paymentResponse.setAmount(BigDecimal.valueOf((Integer) payment.get("CapturedAmount")).divide(BigDecimal.valueOf(100)));
            paymentResponse.setCurrency("BRL");
            paymentResponse.setStatus(PaymentTransaction.PaymentStatus.CAPTURED);

            return paymentResponse;
        } catch (Exception e) {
            log.error("Erro ao capturar pagamento na Cielo", e);
            throw new RuntimeException("Erro ao capturar pagamento: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "null"})
    public PaymentResponse refund(String transactionId, BigDecimal amount) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                apiUrl + "/1/sales/" + transactionId + "/void?amount=" + amount.multiply(BigDecimal.valueOf(100)).intValue(),
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Resposta vazia da API Cielo");
            }
            @SuppressWarnings("unused")
            Map<String, Object> payment = (Map<String, Object>) responseBody.get("Payment");

            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setProvider("cielo");
            paymentResponse.setProviderTransactionId(transactionId);
            paymentResponse.setAmount(amount);
            paymentResponse.setCurrency("BRL");
            paymentResponse.setStatus(PaymentTransaction.PaymentStatus.REFUNDED);

            return paymentResponse;
        } catch (Exception e) {
            log.error("Erro ao estornar pagamento na Cielo", e);
            throw new RuntimeException("Erro ao estornar pagamento: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(String providerName) {
        return "cielo".equalsIgnoreCase(providerName);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("MerchantId", merchantId);
        headers.set("MerchantKey", merchantKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }
} 