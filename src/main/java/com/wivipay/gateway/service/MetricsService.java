package com.wivipay.gateway.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // Métricas de pagamentos
    private Counter paymentAuthorizationCounter;
    private Counter paymentCaptureCounter;
    private Counter paymentRefundCounter;
    private Counter paymentFailureCounter;
    private Timer paymentProcessingTimer;

    // Métricas de clientes
    private Counter customerCreationCounter;
    private Counter customerUpdateCounter;
    private Counter customerDeletionCounter;
    private Timer customerOperationTimer;

    // Métricas de cartões
    private Counter creditCardCreationCounter;
    private Counter creditCardUpdateCounter;
    private Counter creditCardDeletionCounter;
    private Timer creditCardOperationTimer;

    // Métricas de logs
    private Counter transactionLogCounter;
    private Counter errorLogCounter;

    @PostConstruct
    public void initMetrics() {
        // Inicializar métricas de pagamentos
        paymentAuthorizationCounter = Counter.builder("wivipay.payments.authorizations")
                .description("Número de autorizações de pagamento")
                .register(meterRegistry);

        paymentCaptureCounter = Counter.builder("wivipay.payments.captures")
                .description("Número de capturas de pagamento")
                .register(meterRegistry);

        paymentRefundCounter = Counter.builder("wivipay.payments.refunds")
                .description("Número de reembolsos de pagamento")
                .register(meterRegistry);

        paymentFailureCounter = Counter.builder("wivipay.payments.failures")
                .description("Número de falhas de pagamento")
                .register(meterRegistry);

        paymentProcessingTimer = Timer.builder("wivipay.payments.processing.time")
                .description("Tempo de processamento de pagamentos")
                .register(meterRegistry);

        // Inicializar métricas de clientes
        customerCreationCounter = Counter.builder("wivipay.customers.creations")
                .description("Número de clientes criados")
                .register(meterRegistry);

        customerUpdateCounter = Counter.builder("wivipay.customers.updates")
                .description("Número de clientes atualizados")
                .register(meterRegistry);

        customerDeletionCounter = Counter.builder("wivipay.customers.deletions")
                .description("Número de clientes deletados")
                .register(meterRegistry);

        customerOperationTimer = Timer.builder("wivipay.customers.operation.time")
                .description("Tempo de operações com clientes")
                .register(meterRegistry);

        // Inicializar métricas de cartões
        creditCardCreationCounter = Counter.builder("wivipay.credit_cards.creations")
                .description("Número de cartões de crédito criados")
                .register(meterRegistry);

        creditCardUpdateCounter = Counter.builder("wivipay.credit_cards.updates")
                .description("Número de cartões de crédito atualizados")
                .register(meterRegistry);

        creditCardDeletionCounter = Counter.builder("wivipay.credit_cards.deletions")
                .description("Número de cartões de crédito deletados")
                .register(meterRegistry);

        creditCardOperationTimer = Timer.builder("wivipay.credit_cards.operation.time")
                .description("Tempo de operações com cartões de crédito")
                .register(meterRegistry);

        // Inicializar métricas de logs
        transactionLogCounter = Counter.builder("wivipay.transaction_logs.created")
                .description("Número de logs de transação criados")
                .register(meterRegistry);

        errorLogCounter = Counter.builder("wivipay.errors.total")
                .description("Número total de erros")
                .register(meterRegistry);
    }

    // Métricas de pagamentos
    public void incrementPaymentAuthorization() {
        paymentAuthorizationCounter.increment();
        log.debug("Métrica de autorização de pagamento incrementada");
    }

    public void incrementPaymentCapture() {
        paymentCaptureCounter.increment();
        log.debug("Métrica de captura de pagamento incrementada");
    }

    public void incrementPaymentRefund() {
        paymentRefundCounter.increment();
        log.debug("Métrica de reembolso de pagamento incrementada");
    }

    public void incrementPaymentFailure() {
        paymentFailureCounter.increment();
        log.debug("Métrica de falha de pagamento incrementada");
    }

    public Timer.Sample startPaymentProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopPaymentProcessingTimer(Timer.Sample sample) {
        sample.stop(paymentProcessingTimer);
        log.debug("Timer de processamento de pagamento parado");
    }

    // Métricas de clientes
    public void incrementCustomerCreation() {
        customerCreationCounter.increment();
        log.debug("Métrica de criação de cliente incrementada");
    }

    public void incrementCustomerUpdate() {
        customerUpdateCounter.increment();
        log.debug("Métrica de atualização de cliente incrementada");
    }

    public void incrementCustomerDeletion() {
        customerDeletionCounter.increment();
        log.debug("Métrica de deleção de cliente incrementada");
    }

    public Timer.Sample startCustomerOperationTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopCustomerOperationTimer(Timer.Sample sample) {
        sample.stop(customerOperationTimer);
        log.debug("Timer de operação com cliente parado");
    }

    // Métricas de cartões
    public void incrementCreditCardCreation() {
        creditCardCreationCounter.increment();
        log.debug("Métrica de criação de cartão incrementada");
    }

    public void incrementCreditCardUpdate() {
        creditCardUpdateCounter.increment();
        log.debug("Métrica de atualização de cartão incrementada");
    }

    public void incrementCreditCardDeletion() {
        creditCardDeletionCounter.increment();
        log.debug("Métrica de deleção de cartão incrementada");
    }

    public Timer.Sample startCreditCardOperationTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopCreditCardOperationTimer(Timer.Sample sample) {
        sample.stop(creditCardOperationTimer);
        log.debug("Timer de operação com cartão parado");
    }

    // Métricas de logs
    public void incrementTransactionLog() {
        transactionLogCounter.increment();
        log.debug("Métrica de log de transação incrementada");
    }

    public void incrementError() {
        errorLogCounter.increment();
        log.debug("Métrica de erro incrementada");
    }

    // Métricas customizadas
    public void recordPaymentAmount(String provider, String currency, double amount) {
        meterRegistry.gauge("wivipay.payments.amount", 
            Tags.of("provider", provider, "currency", currency), 
            amount);
        log.debug("Métrica de valor de pagamento registrada: {} {} {}", provider, currency, amount);
    }

    public void recordCustomerCount(long count) {
        meterRegistry.gauge("wivipay.customers.total", count);
        log.debug("Métrica de total de clientes registrada: {}", count);
    }

    public void recordCreditCardCount(long count) {
        meterRegistry.gauge("wivipay.credit_cards.total", count);
        log.debug("Métrica de total de cartões registrada: {}", count);
    }

    public void recordTransactionCount(long count) {
        meterRegistry.gauge("wivipay.transactions.total", count);
        log.debug("Métrica de total de transações registrada: {}", count);
    }
}
