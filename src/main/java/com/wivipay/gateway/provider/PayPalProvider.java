package com.wivipay.gateway.provider;

import com.wivipay.gateway.dto.PaymentRequest;
import com.wivipay.gateway.dto.PaymentResponse;
import com.wivipay.gateway.model.PaymentTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PayPalProvider implements PaymentProvider {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String clientId;
    private final String clientSecret;
    private String accessToken;

    public PayPalProvider(
            RestTemplate restTemplate,
            @Value("${paypal.api.url:https://api-m.sandbox.paypal.com}") String apiUrl,
            @Value("${paypal.client.id}") String clientId,
            @Value("${paypal.client.secret}") String clientSecret) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public String getName() {
        return "paypal";
    }

    @Override
    @SuppressWarnings("null")
    public PaymentResponse authorize(PaymentRequest request) {
        try {
            String token = getAccessToken();

            Map<String, Object> payload = new HashMap<>();
            payload.put("intent", "AUTHORIZE");
            
            Map<String, Object> amount = new HashMap<>();
            amount.put("currency_code", request.getCurrency());
            amount.put("value", request.getAmount().toString());
            
            Map<String, Object> purchaseUnit = new HashMap<>();
            purchaseUnit.put("amount", amount);
            purchaseUnit.put("reference_id", request.getCustomerId());
            
            payload.put("purchase_units", List.of(purchaseUnit));
            
            Map<String, Object> tokenMap = new HashMap<>();
            tokenMap.put("id", request.getPaymentMethod());
            tokenMap.put("type", "PAYMENT_METHOD_TOKEN");
            
            Map<String, Object> paymentSource = new HashMap<>();
            paymentSource.put("token", tokenMap);
            
            payload.put("payment_source", paymentSource);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                apiUrl + "/v2/checkout/orders",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Resposta vazia da API PayPal");
            }

            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setProvider("paypal");
            paymentResponse.setProviderTransactionId(responseBody.get("id").toString());
            paymentResponse.setAmount(request.getAmount());
            paymentResponse.setCurrency(request.getCurrency());
            paymentResponse.setStatus(PaymentTransaction.PaymentStatus.AUTHORIZED);
            paymentResponse.setPaymentMethod(request.getPaymentMethod());

            return paymentResponse;
        } catch (Exception e) {
            log.error("Erro ao autorizar pagamento no PayPal", e);
            throw new RuntimeException("Erro ao processar pagamento: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "null"})
    public PaymentResponse capture(String transactionId) {
        try {
            String token = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                apiUrl + "/v2/checkout/orders/" + transactionId + "/capture",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Resposta vazia da API PayPal");
            }
            List<Map<String, Object>> purchaseUnits = (List<Map<String, Object>>) responseBody.get("purchase_units");
            if (purchaseUnits == null || purchaseUnits.isEmpty()) {
                throw new RuntimeException("Resposta inválida da API PayPal: purchase_units vazio");
            }
            Map<String, Object> amount = (Map<String, Object>) purchaseUnits.get(0).get("amount");

            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setProvider("paypal");
            paymentResponse.setProviderTransactionId(transactionId);
            paymentResponse.setAmount(new BigDecimal(amount.get("value").toString()));
            paymentResponse.setCurrency(amount.get("currency_code").toString());
            paymentResponse.setStatus(PaymentTransaction.PaymentStatus.CAPTURED);

            return paymentResponse;
        } catch (Exception e) {
            log.error("Erro ao capturar pagamento no PayPal", e);
            throw new RuntimeException("Erro ao capturar pagamento: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("null")
    public PaymentResponse refund(String transactionId, BigDecimal amount) {
        try {
            String token = getAccessToken();

            Map<String, Object> payload = new HashMap<>();
            Map<String, Object> amountMap = new HashMap<>();
            amountMap.put("value", amount.toString());
            amountMap.put("currency_code", "BRL");
            payload.put("amount", amountMap);
            payload.put("note_to_payer", "Refund for order " + transactionId);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                apiUrl + "/v2/payments/captures/" + transactionId + "/refund",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Resposta vazia da API PayPal");
            }

            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setProvider("paypal");
            paymentResponse.setProviderTransactionId(responseBody.get("id").toString());
            paymentResponse.setAmount(amount);
            paymentResponse.setCurrency("BRL");
            paymentResponse.setStatus(PaymentTransaction.PaymentStatus.REFUNDED);

            return paymentResponse;
        } catch (Exception e) {
            log.error("Erro ao estornar pagamento no PayPal", e);
            throw new RuntimeException("Erro ao estornar pagamento: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(String providerName) {
        return "paypal".equalsIgnoreCase(providerName);
    }

    @SuppressWarnings("null")
    private String getAccessToken() {
        if (accessToken != null) {
            return accessToken;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            apiUrl + "/v1/oauth2/token",
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            throw new RuntimeException("Resposta vazia da API PayPal ao obter token");
        }
        accessToken = responseBody.get("access_token").toString();

        return accessToken;
    }
} 