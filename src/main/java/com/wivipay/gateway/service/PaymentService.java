package com.wivipay.gateway.service;

import com.wivipay.gateway.dto.PaymentRequest;
import com.wivipay.gateway.dto.PaymentResponse;
import com.wivipay.gateway.model.PaymentTransaction;
import com.wivipay.gateway.provider.PaymentProvider;
import com.wivipay.gateway.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class PaymentService {

    private final List<PaymentProvider> providers;
    private final PaymentTransactionRepository repository;
    private final TransactionLogService transactionLogService;
    private final BusinessValidationService businessValidationService;

    @Transactional
    public PaymentResponse authorize(PaymentRequest request) {
        // Validações de negócio
        businessValidationService.validatePaymentRequest(request);
        
        PaymentProvider provider = findProvider(request.getProvider());
        
        PaymentResponse response = provider.authorize(request);
        
        // Adicionar campos da requisição à resposta
        response.setDescription(request.getDescription());
        response.setCustomerId(request.getCustomerId());
        response.setMetadata(request.getMetadata());
        
        PaymentTransaction transaction = saveTransaction(response);
        
        // Log da transação
        transactionLogService.logPaymentAuthorization(transaction);
        
        return response;
    }

    @Transactional
    public PaymentResponse capture(UUID transactionId) {
        PaymentTransaction transaction = repository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));
        
        PaymentProvider provider = findProvider(transaction.getProvider());
        PaymentResponse response = provider.capture(transaction.getProviderTransactionId());
        
        updateTransaction(transaction, response);
        
        // Log da transação
        transactionLogService.logPaymentCapture(transaction);
        
        return response;
    }

    @Transactional
    public PaymentResponse refund(UUID transactionId, BigDecimal amount) {
        PaymentTransaction transaction = repository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));
        
        PaymentProvider provider = findProvider(transaction.getProvider());
        PaymentResponse response = provider.refund(transaction.getProviderTransactionId(), amount);
        
        updateTransaction(transaction, response);
        
        // Log da transação
        transactionLogService.logPaymentRefund(transaction, amount.toString());
        
        return response;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID transactionId) {
        PaymentTransaction transaction = repository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));
        
        return mapToPaymentResponse(transaction);
    }

    private PaymentProvider findProvider(String providerName) {
        return providers.stream()
                .filter(p -> p.supports(providerName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Provedor não suportado: " + providerName));
    }

    private PaymentTransaction saveTransaction(PaymentResponse response) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setProvider(response.getProvider());
        transaction.setProviderTransactionId(response.getProviderTransactionId());
        transaction.setAmount(response.getAmount());
        transaction.setCurrency(response.getCurrency());
        transaction.setStatus(response.getStatus());
        transaction.setPaymentMethod(response.getPaymentMethod());
        transaction.setRawResponse(response.toString());
        
        // Campos adicionais se disponíveis na requisição
        if (response.getDescription() != null) {
            transaction.setDescription(response.getDescription());
        }
        if (response.getCustomerId() != null) {
            transaction.setCustomerId(response.getCustomerId());
        }
        if (response.getMetadata() != null) {
            transaction.setMetadata(response.getMetadata());
        }
        
        repository.save(transaction);
        return transaction;
    }

    private void updateTransaction(PaymentTransaction transaction, PaymentResponse response) {
        transaction.setStatus(response.getStatus());
        transaction.setRawResponse(response.toString());
        repository.save(transaction);
    }

    private PaymentResponse mapToPaymentResponse(PaymentTransaction transaction) {
        PaymentResponse response = new PaymentResponse();
        response.setId(transaction.getId());
        response.setProvider(transaction.getProvider());
        response.setProviderTransactionId(transaction.getProviderTransactionId());
        response.setAmount(transaction.getAmount());
        response.setCurrency(transaction.getCurrency());
        response.setStatus(transaction.getStatus());
        response.setPaymentMethod(transaction.getPaymentMethod());
        response.setDescription(transaction.getDescription());
        response.setCustomerId(transaction.getCustomerId());
        response.setMetadata(transaction.getMetadata());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        return response;
    }
} 