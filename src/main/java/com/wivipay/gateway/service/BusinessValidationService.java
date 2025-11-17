package com.wivipay.gateway.service;

import com.wivipay.gateway.dto.PaymentRequest;
import com.wivipay.gateway.model.CreditCard;
import com.wivipay.gateway.repository.CustomerRepository;
import com.wivipay.gateway.repository.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessValidationService {
    
    private final CustomerRepository customerRepository;
    @SuppressWarnings("unused")
    private final CreditCardRepository creditCardRepository;
    
    // Moedas suportadas por provedor
    private static final List<String> STRIPE_CURRENCIES = Arrays.asList("BRL", "USD", "EUR", "GBP");
    private static final List<String> CIELO_CURRENCIES = Arrays.asList("BRL");
    private static final List<String> PAYPAL_CURRENCIES = Arrays.asList("BRL", "USD", "EUR", "GBP");
    
    // Limites de valores
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal MAX_AMOUNT_BRL = new BigDecimal("999999.99");
    private static final BigDecimal MAX_AMOUNT_OTHER = new BigDecimal("999999.99");
    
    public void validatePaymentRequest(PaymentRequest request) {
        validateAmount(request.getAmount());
        validateCurrency(request.getCurrency());
        validateProvider(request.getProvider());
        validateProviderCurrencyCompatibility(request.getProvider(), request.getCurrency());
        validateAmountCurrencyCompatibility(request.getAmount(), request.getCurrency());
        validatePaymentMethod(request.getPaymentMethod(), request.getProvider());
        validateCustomer(request.getCustomerId());
    }
    
    public void validateCustomer(String customerId) {
        if (customerId != null && !customerRepository.existsByExternalId(customerId)) {
            throw new RuntimeException("Cliente não encontrado: " + customerId);
        }
    }
    
    public void validateCreditCard(CreditCard creditCard) {
        validateCardExpiration(creditCard.getExpirationMonth(), creditCard.getExpirationYear());
        validateCardBrand(creditCard.getBrand());
        validateLastFourDigits(creditCard.getLastFourDigits());
    }
    
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new RuntimeException("Valor é obrigatório");
        }
        
        if (amount.compareTo(MIN_AMOUNT) < 0) {
            throw new RuntimeException("Valor deve ser maior que " + MIN_AMOUNT);
        }
    }
    
    private void validateCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            throw new RuntimeException("Moeda é obrigatória");
        }
        
        List<String> supportedCurrencies = Arrays.asList("BRL", "USD", "EUR", "GBP");
        if (!supportedCurrencies.contains(currency.toUpperCase())) {
            throw new RuntimeException("Moeda não suportada: " + currency + ". Moedas suportadas: " + supportedCurrencies);
        }
    }
    
    private void validateProvider(String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            throw new RuntimeException("Provedor é obrigatório");
        }
        
        List<String> supportedProviders = Arrays.asList("stripe", "cielo", "paypal");
        if (!supportedProviders.contains(provider.toLowerCase())) {
            throw new RuntimeException("Provedor não suportado: " + provider + ". Provedores suportados: " + supportedProviders);
        }
    }
    
    private void validateProviderCurrencyCompatibility(String provider, String currency) {
        List<String> supportedCurrencies = getSupportedCurrencies(provider);
        
        if (!supportedCurrencies.contains(currency.toUpperCase())) {
            throw new RuntimeException("Moeda " + currency + " não é suportada pelo provedor " + provider);
        }
    }
    
    private void validateAmountCurrencyCompatibility(BigDecimal amount, String currency) {
        BigDecimal maxAmount = "BRL".equals(currency) ? MAX_AMOUNT_BRL : MAX_AMOUNT_OTHER;
        
        if (amount.compareTo(maxAmount) > 0) {
            throw new RuntimeException("Valor não pode exceder " + maxAmount + " para moeda " + currency);
        }
        
        // Validação de casas decimais
        int maxScale = 2;
        if (amount.scale() > maxScale) {
            throw new RuntimeException("Valor não pode ter mais de " + maxScale + " casas decimais");
        }
    }
    
    private void validatePaymentMethod(String paymentMethod, String provider) {
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new RuntimeException("Método de pagamento é obrigatório");
        }
        
        // Validações específicas por provedor
        switch (provider.toLowerCase()) {
            case "stripe":
                if (!paymentMethod.startsWith("tok_") && !paymentMethod.startsWith("card_")) {
                    throw new RuntimeException("Token do Stripe deve começar com 'tok_' ou 'card_'");
                }
                break;
            case "cielo":
                if (!paymentMethod.startsWith("card_")) {
                    throw new RuntimeException("Token da Cielo deve começar com 'card_'");
                }
                break;
            case "paypal":
                if (!paymentMethod.startsWith("paypal_")) {
                    throw new RuntimeException("Token do PayPal deve começar com 'paypal_'");
                }
                break;
        }
    }
    
    private void validateCardExpiration(Integer month, Integer year) {
        if (month == null || month < 1 || month > 12) {
            throw new RuntimeException("Mês de expiração deve estar entre 1 e 12");
        }
        
        if (year == null) {
            throw new RuntimeException("Ano de expiração é obrigatório");
        }
        
        // Validar se o ano não é muito distante no futuro
        int currentYear = LocalDate.now().getYear();
        if (year > currentYear + 20) {
            throw new RuntimeException("Ano de expiração deve ser válido");
        }
        
        // Verificar se o cartão não expirou
        LocalDate expirationDate = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);
        if (expirationDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Cartão expirado");
        }
    }
    
    private void validateCardBrand(String brand) {
        List<String> supportedBrands = Arrays.asList("VISA", "MASTERCARD", "AMEX", "ELO", "HIPERCARD");
        if (!supportedBrands.contains(brand.toUpperCase())) {
            throw new RuntimeException("Bandeira não suportada: " + brand + ". Bandeiras suportadas: " + supportedBrands);
        }
    }
    
    private void validateLastFourDigits(String lastFourDigits) {
        if (lastFourDigits == null || lastFourDigits.length() != 4 || !lastFourDigits.matches("\\d{4}")) {
            throw new RuntimeException("Últimos 4 dígitos devem ter exatamente 4 números");
        }
    }
    
    private List<String> getSupportedCurrencies(String provider) {
        switch (provider.toLowerCase()) {
            case "stripe":
                return STRIPE_CURRENCIES;
            case "cielo":
                return CIELO_CURRENCIES;
            case "paypal":
                return PAYPAL_CURRENCIES;
            default:
                throw new RuntimeException("Provedor não suportado: " + provider);
        }
    }
}
