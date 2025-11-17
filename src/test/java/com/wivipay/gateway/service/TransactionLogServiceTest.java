package com.wivipay.gateway.service;

import com.wivipay.gateway.model.PaymentTransaction;
import com.wivipay.gateway.model.TransactionLog;
import com.wivipay.gateway.repository.TransactionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class TransactionLogServiceTest {

    @Mock
    private TransactionLogRepository repository;

    @InjectMocks
    private TransactionLogService transactionLogService;

    private PaymentTransaction transaction;
    private TransactionLog transactionLog;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        transaction = new PaymentTransaction();
        transaction.setId(transactionId);
        transaction.setProvider("stripe");
        transaction.setAmount(BigDecimal.valueOf(100.00));
        transaction.setCurrency("BRL");
        transaction.setStatus(PaymentTransaction.PaymentStatus.AUTHORIZED);

        transactionLog = new TransactionLog();
        transactionLog.setId(UUID.randomUUID());
        transactionLog.setTransaction(transaction);
        transactionLog.setStatus("AUTHORIZED");
        transactionLog.setMessage("Pagamento autorizado com sucesso");
        transactionLog.setCreatedAt(now);
    }

    @Test
    void shouldLogTransactionStatusSuccessfully() {
        when(repository.save(any(TransactionLog.class))).thenReturn(transactionLog);

        TransactionLog result = transactionLogService.logTransactionStatus(
                transaction, "AUTHORIZED", "Pagamento autorizado com sucesso");

        assertNotNull(result);
        assertEquals("AUTHORIZED", result.getStatus());
        assertEquals("Pagamento autorizado com sucesso", result.getMessage());
        verify(repository).save(any(TransactionLog.class));
    }

    @Test
    void shouldGetTransactionLogsSuccessfully() {
        when(repository.findByTransactionIdOrderByCreatedAtDesc(transactionId))
                .thenReturn(List.of(transactionLog));

        List<TransactionLog> result = transactionLogService.getTransactionLogs(transactionId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(transactionId, result.get(0).getTransaction().getId());
    }

    @Test
    void shouldGetTransactionLogsByStatusSuccessfully() {
        when(repository.findByTransactionIdAndStatus(transactionId, "AUTHORIZED"))
                .thenReturn(List.of(transactionLog));

        List<TransactionLog> result = transactionLogService.getTransactionLogsByStatus(transactionId, "AUTHORIZED");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AUTHORIZED", result.get(0).getStatus());
    }

    @Test
    void shouldLogPaymentAuthorizationSuccessfully() {
        when(repository.save(any(TransactionLog.class))).thenReturn(transactionLog);

        assertDoesNotThrow(() -> transactionLogService.logPaymentAuthorization(transaction));
        verify(repository).save(any(TransactionLog.class));
    }

    @Test
    void shouldLogPaymentCaptureSuccessfully() {
        when(repository.save(any(TransactionLog.class))).thenReturn(transactionLog);

        assertDoesNotThrow(() -> transactionLogService.logPaymentCapture(transaction));
        verify(repository).save(any(TransactionLog.class));
    }

    @Test
    void shouldLogPaymentRefundSuccessfully() {
        when(repository.save(any(TransactionLog.class))).thenReturn(transactionLog);

        assertDoesNotThrow(() -> transactionLogService.logPaymentRefund(transaction, "100.00"));
        verify(repository).save(any(TransactionLog.class));
    }

    @Test
    void shouldLogPaymentFailureSuccessfully() {
        when(repository.save(any(TransactionLog.class))).thenReturn(transactionLog);

        assertDoesNotThrow(() -> transactionLogService.logPaymentFailure(transaction, "Cartão recusado"));
        verify(repository).save(any(TransactionLog.class));
    }

    @Test
    void shouldLogPaymentPendingSuccessfully() {
        when(repository.save(any(TransactionLog.class))).thenReturn(transactionLog);

        assertDoesNotThrow(() -> transactionLogService.logPaymentPending(transaction));
        verify(repository).save(any(TransactionLog.class));
    }

    @Test
    void shouldReturnEmptyListWhenNoLogsFound() {
        when(repository.findByTransactionIdOrderByCreatedAtDesc(transactionId))
                .thenReturn(List.of());

        List<TransactionLog> result = transactionLogService.getTransactionLogs(transactionId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenNoLogsByStatusFound() {
        when(repository.findByTransactionIdAndStatus(transactionId, "FAILED"))
                .thenReturn(List.of());

        List<TransactionLog> result = transactionLogService.getTransactionLogsByStatus(transactionId, "FAILED");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleMultipleLogsForSameTransaction() {
        TransactionLog log1 = new TransactionLog();
        log1.setStatus("AUTHORIZED");
        log1.setMessage("Pagamento autorizado");

        TransactionLog log2 = new TransactionLog();
        log2.setStatus("CAPTURED");
        log2.setMessage("Pagamento capturado");

        when(repository.findByTransactionIdOrderByCreatedAtDesc(transactionId))
                .thenReturn(List.of(log2, log1)); // Ordem cronológica reversa

        List<TransactionLog> result = transactionLogService.getTransactionLogs(transactionId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("CAPTURED", result.get(0).getStatus());
        assertEquals("AUTHORIZED", result.get(1).getStatus());
    }
}
