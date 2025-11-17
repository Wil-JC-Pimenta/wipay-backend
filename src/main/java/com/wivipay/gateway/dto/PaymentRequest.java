package com.wivipay.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.Arrays;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição de pagamento")
public class PaymentRequest {
    
    @NotNull(message = "O valor é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    @DecimalMax(value = "999999.99", message = "O valor não pode exceder 999.999,99")
    @Schema(description = "Valor do pagamento", example = "100.00")
    private BigDecimal amount;
    
    @NotBlank(message = "A moeda é obrigatória")
    @Pattern(regexp = "^(BRL|USD|EUR|GBP)$", message = "Moeda deve ser BRL, USD, EUR ou GBP")
    @Schema(description = "Moeda do pagamento", example = "BRL")
    private String currency;
    
    @NotBlank(message = "O método de pagamento é obrigatório")
    @Schema(description = "Método de pagamento (token do cartão, etc)", example = "tok_1Nq9Xr2eZvKYlo2C0QvJ2eZv")
    private String paymentMethod;
    
    @NotBlank(message = "O provedor é obrigatório")
    @Pattern(regexp = "^(stripe|cielo|paypal)$", message = "Provedor deve ser stripe, cielo ou paypal")
    @Schema(description = "Provedor de pagamento", example = "cielo")
    private String provider;
    
    @Size(max = 255, message = "A descrição não pode exceder 255 caracteres")
    @Schema(description = "Descrição do pagamento", example = "Pagamento de teste")
    private String description;
    
    @Size(max = 100, message = "O ID do cliente não pode exceder 100 caracteres")
    @Schema(description = "ID do cliente", example = "12345")
    private String customerId;
    
    @Size(max = 1000, message = "Os metadados não podem exceder 1000 caracteres")
    @Schema(description = "Metadados adicionais", example = "{\"orderId\": \"123\"}")
    private String metadata;
    
    // Validações de negócio
    @AssertTrue(message = "Valor e moeda devem ser compatíveis")
    public boolean isAmountAndCurrencyCompatible() {
        if (amount == null || currency == null) {
            return true; // Validação básica já tratada
        }
        
        // BRL: máximo 2 casas decimais
        if ("BRL".equals(currency)) {
            return amount.scale() <= 2;
        }
        
        // USD, EUR, GBP: máximo 2 casas decimais
        if (Arrays.asList("USD", "EUR", "GBP").contains(currency)) {
            return amount.scale() <= 2;
        }
        
        return true;
    }
    
    @AssertTrue(message = "Provedor e moeda devem ser compatíveis")
    public boolean isProviderAndCurrencyCompatible() {
        if (provider == null || currency == null) {
            return true; // Validação básica já tratada
        }
        
        // Cielo: apenas BRL
        if ("cielo".equals(provider) && !"BRL".equals(currency)) {
            return false;
        }
        
        // Stripe: todas as moedas suportadas
        if ("stripe".equals(provider)) {
            return Arrays.asList("BRL", "USD", "EUR", "GBP").contains(currency);
        }
        
        // PayPal: todas as moedas suportadas
        if ("paypal".equals(provider)) {
            return Arrays.asList("BRL", "USD", "EUR", "GBP").contains(currency);
        }
        
        return true;
    }
} 