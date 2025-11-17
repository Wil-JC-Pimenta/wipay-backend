package com.wivipay.gateway.service;

import com.wivipay.gateway.dto.PaymentRequest;
import com.wivipay.gateway.dto.PaymentResponse;
import com.wivipay.gateway.model.PaymentTransaction;
import com.wivipay.gateway.provider.PaymentProvider;
import com.wivipay.gateway.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class PaymentServiceTest {

    @Mock
    private PaymentTransactionRepository repository;

    @Mock
    private PaymentProvider stripeProvider;

    @Mock
    private TransactionLogService transactionLogService;

    @Mock
    private BusinessValidationService businessValidationService;

    private PaymentService paymentService;

    private PaymentRequest request;
    private PaymentResponse response;
    private PaymentTransaction transaction;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        
        request = new PaymentRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setCurrency("BRL");
        request.setPaymentMethod("card_token");
        request.setProvider("stripe");

        response = new PaymentResponse();
        response.setId(transactionId);
        response.setProvider("stripe");
        response.setProviderTransactionId("ch_123");
        response.setAmount(BigDecimal.valueOf(100));
        response.setCurrency("BRL");
        response.setStatus(PaymentTransaction.PaymentStatus.AUTHORIZED);
        response.setPaymentMethod("card_token");

        transaction = new PaymentTransaction();
        transaction.setId(transactionId);
        transaction.setProvider("stripe");
        transaction.setProviderTransactionId("ch_123");
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setCurrency("BRL");
        transaction.setStatus(PaymentTransaction.PaymentStatus.AUTHORIZED);
        transaction.setPaymentMethod("card_token");

        // Configurar mocks de forma mais flexível
        lenient().when(stripeProvider.supports(anyString())).thenReturn(false);
        lenient().when(stripeProvider.supports("stripe")).thenReturn(true);
        lenient().doNothing().when(businessValidationService).validatePaymentRequest(any(PaymentRequest.class));
        
        paymentService = new PaymentService(List.of(stripeProvider), repository, transactionLogService, businessValidationService);
    }

    @Test
    void shouldAuthorizePaymentSuccessfully() {
        when(stripeProvider.authorize(request)).thenReturn(response);
        when(repository.save(any(PaymentTransaction.class))).thenReturn(transaction);

        PaymentResponse result = paymentService.authorize(request);

        assertNotNull(result);
        assertEquals(response.getId(), result.getId());
        assertEquals(response.getStatus(), result.getStatus());
        verify(repository).save(any(PaymentTransaction.class));
    }

    @Test
    void shouldCapturePaymentSuccessfully() {
        when(repository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(stripeProvider.capture("ch_123")).thenReturn(response);

        PaymentResponse result = paymentService.capture(transactionId);

        assertNotNull(result);
        assertEquals(PaymentTransaction.PaymentStatus.AUTHORIZED, result.getStatus());
        verify(repository).save(any(PaymentTransaction.class));
    }

    @Test
    void shouldRefundPaymentSuccessfully() {
        response.setStatus(PaymentTransaction.PaymentStatus.REFUNDED);
        
        when(repository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(stripeProvider.refund("ch_123", BigDecimal.valueOf(100))).thenReturn(response);

        PaymentResponse result = paymentService.refund(transactionId, BigDecimal.valueOf(100));

        assertNotNull(result);
        assertEquals(PaymentTransaction.PaymentStatus.REFUNDED, result.getStatus());
        verify(repository).save(any(PaymentTransaction.class));
    }

    @Test
    void shouldThrowExceptionWhenProviderNotFound() {
        request.setProvider("invalid_provider");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentService.authorize(request));

        assertEquals("Provedor não suportado: invalid_provider", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {
        when(repository.findById(any(UUID.class))).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentService.capture(UUID.randomUUID()));

        assertEquals("Transação não encontrada", exception.getMessage());
    }
}
