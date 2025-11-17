package com.wivipay.gateway.service;

import com.wivipay.gateway.dto.CreditCardRequest;
import com.wivipay.gateway.dto.CreditCardResponse;
import com.wivipay.gateway.model.CreditCard;
import com.wivipay.gateway.model.Customer;
import com.wivipay.gateway.repository.CreditCardRepository;
import com.wivipay.gateway.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CreditCardService {
    
    private final CreditCardRepository repository;
    private final CustomerRepository customerRepository;
    private final BusinessValidationService businessValidationService;
    
    @Transactional
    public CreditCardResponse createCreditCard(CreditCardRequest request) {
        validateCreditCardRequest(request);
        
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
        CreditCard creditCard = new CreditCard();
        creditCard.setCustomer(customer);
        creditCard.setProviderCardId(request.getProviderCardId());
        creditCard.setLastFourDigits(request.getLastFourDigits());
        creditCard.setBrand(request.getBrand());
        creditCard.setExpirationMonth(request.getExpirationMonth());
        creditCard.setExpirationYear(request.getExpirationYear());
        creditCard.setIsDefault(request.getIsDefault());
        
        // Validações de negócio
        businessValidationService.validateCreditCard(creditCard);
        
        // Se for o cartão padrão, desmarca os outros
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            unsetDefaultCards(customer.getId());
        }
        
        CreditCard savedCard = repository.save(creditCard);
        log.info("Cartão de crédito criado com sucesso: {}", savedCard.getId());
        
        return mapToCreditCardResponse(savedCard);
    }
    
    @Transactional
    public CreditCardResponse updateCreditCard(UUID id, CreditCardRequest request) {
        CreditCard creditCard = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cartão de crédito não encontrado"));
        
        validateCreditCardRequest(request);
        
        creditCard.setProviderCardId(request.getProviderCardId());
        creditCard.setLastFourDigits(request.getLastFourDigits());
        creditCard.setBrand(request.getBrand());
        creditCard.setExpirationMonth(request.getExpirationMonth());
        creditCard.setExpirationYear(request.getExpirationYear());
        
        // Se for o cartão padrão, desmarca os outros
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            unsetDefaultCards(creditCard.getCustomer().getId());
        }
        creditCard.setIsDefault(request.getIsDefault());
        
        CreditCard updatedCard = repository.save(creditCard);
        log.info("Cartão de crédito atualizado com sucesso: {}", updatedCard.getId());
        
        return mapToCreditCardResponse(updatedCard);
    }
    
    @Transactional(readOnly = true)
    public CreditCardResponse getCreditCard(UUID id) {
        CreditCard creditCard = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cartão de crédito não encontrado"));
        
        return mapToCreditCardResponse(creditCard);
    }
    
    @Transactional(readOnly = true)
    public List<CreditCardResponse> getCustomerCreditCards(UUID customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(this::mapToCreditCardResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CreditCardResponse getDefaultCreditCard(UUID customerId) {
        CreditCard defaultCard = repository.findByCustomerIdAndIsDefaultTrue(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente não possui cartão padrão"));
        
        return mapToCreditCardResponse(defaultCard);
    }
    
    @Transactional
    public void deleteCreditCard(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Cartão de crédito não encontrado");
        }
        
        repository.deleteById(id);
        log.info("Cartão de crédito deletado com sucesso: {}", id);
    }
    
    @Transactional
    public CreditCardResponse setDefaultCreditCard(UUID id) {
        CreditCard creditCard = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cartão de crédito não encontrado"));
        
        unsetDefaultCards(creditCard.getCustomer().getId());
        
        creditCard.setIsDefault(true);
        CreditCard updatedCard = repository.save(creditCard);
        
        log.info("Cartão de crédito definido como padrão: {}", updatedCard.getId());
        return mapToCreditCardResponse(updatedCard);
    }
    
    private void validateCreditCardRequest(CreditCardRequest request) {
        if (repository.existsByProviderCardId(request.getProviderCardId())) {
            throw new RuntimeException("Cartão já cadastrado");
        }
        
        if (request.getExpirationMonth() < 1 || request.getExpirationMonth() > 12) {
            throw new RuntimeException("Mês de expiração inválido");
        }
        
        if (request.getExpirationYear() < 2024) {
            throw new RuntimeException("Ano de expiração inválido");
        }
    }
    
    private void unsetDefaultCards(UUID customerId) {
        repository.findByCustomerId(customerId).stream()
                .filter(CreditCard::getIsDefault)
                .forEach(card -> {
                    card.setIsDefault(false);
                    repository.save(card);
                });
    }
    
    private CreditCardResponse mapToCreditCardResponse(CreditCard creditCard) {
        CreditCardResponse response = new CreditCardResponse();
        response.setId(creditCard.getId());
        response.setCustomerId(creditCard.getCustomer().getId());
        response.setProviderCardId(creditCard.getProviderCardId());
        response.setLastFourDigits(creditCard.getLastFourDigits());
        response.setBrand(creditCard.getBrand());
        response.setExpirationMonth(creditCard.getExpirationMonth());
        response.setExpirationYear(creditCard.getExpirationYear());
        response.setIsDefault(creditCard.getIsDefault());
        response.setCreatedAt(creditCard.getCreatedAt());
        response.setUpdatedAt(creditCard.getUpdatedAt());
        return response;
    }
}
