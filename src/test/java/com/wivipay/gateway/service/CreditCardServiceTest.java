package com.wivipay.gateway.service;

import com.wivipay.gateway.dto.CreditCardRequest;
import com.wivipay.gateway.dto.CreditCardResponse;
import com.wivipay.gateway.model.CreditCard;
import com.wivipay.gateway.model.Customer;
import com.wivipay.gateway.repository.CreditCardRepository;
import com.wivipay.gateway.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class CreditCardServiceTest {

    @Mock
    private CreditCardRepository repository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BusinessValidationService businessValidationService;

    @InjectMocks
    private CreditCardService creditCardService;

    private CreditCardRequest request;
    private CreditCard creditCard;
    private Customer customer;
    private CreditCardResponse expectedResponse;
    private UUID creditCardId;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        creditCardId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        customer = new Customer();
        customer.setId(customerId);
        customer.setName("João Silva");

        request = new CreditCardRequest();
        request.setCustomerId(customerId);
        request.setProviderCardId("card_123456789");
        request.setLastFourDigits("1234");
        request.setBrand("VISA");
        request.setExpirationMonth(12);
        request.setExpirationYear(2025);
        request.setIsDefault(false);

        creditCard = new CreditCard();
        creditCard.setId(creditCardId);
        creditCard.setCustomer(customer);
        creditCard.setProviderCardId("card_123456789");
        creditCard.setLastFourDigits("1234");
        creditCard.setBrand("VISA");
        creditCard.setExpirationMonth(12);
        creditCard.setExpirationYear(2025);
        creditCard.setIsDefault(false);
        creditCard.setCreatedAt(now);
        creditCard.setUpdatedAt(now);

        expectedResponse = new CreditCardResponse();
        expectedResponse.setId(creditCardId);
        expectedResponse.setCustomerId(customerId);
        expectedResponse.setProviderCardId("card_123456789");
        expectedResponse.setLastFourDigits("1234");
        expectedResponse.setBrand("VISA");
        expectedResponse.setExpirationMonth(12);
        expectedResponse.setExpirationYear(2025);
        expectedResponse.setIsDefault(false);
        expectedResponse.setCreatedAt(now);
        expectedResponse.setUpdatedAt(now);
    }

    @Test
    void shouldCreateCreditCardSuccessfully() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(repository.existsByProviderCardId("card_123456789")).thenReturn(false);
        when(repository.save(any(CreditCard.class))).thenReturn(creditCard);

        CreditCardResponse result = creditCardService.createCreditCard(request);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getProviderCardId(), result.getProviderCardId());
        verify(repository).save(any(CreditCard.class));
    }

    @Test
    void shouldUpdateCreditCardSuccessfully() {
        when(repository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(repository.existsByProviderCardId("card_123456789")).thenReturn(false);
        when(repository.save(any(CreditCard.class))).thenReturn(creditCard);

        CreditCardResponse result = creditCardService.updateCreditCard(creditCardId, request);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        verify(repository).save(any(CreditCard.class));
    }

    @Test
    void shouldGetCreditCardSuccessfully() {
        when(repository.findById(creditCardId)).thenReturn(Optional.of(creditCard));

        CreditCardResponse result = creditCardService.getCreditCard(creditCardId);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getBrand(), result.getBrand());
    }

    @Test
    void shouldGetCustomerCreditCardsSuccessfully() {
        when(repository.findByCustomerId(customerId)).thenReturn(List.of(creditCard));

        List<CreditCardResponse> result = creditCardService.getCustomerCreditCards(customerId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponse.getId(), result.get(0).getId());
    }

    @Test
    void shouldGetDefaultCreditCardSuccessfully() {
        when(repository.findByCustomerIdAndIsDefaultTrue(customerId)).thenReturn(Optional.of(creditCard));

        CreditCardResponse result = creditCardService.getDefaultCreditCard(customerId);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
    }

    @Test
    void shouldDeleteCreditCardSuccessfully() {
        when(repository.existsById(creditCardId)).thenReturn(true);
        doNothing().when(repository).deleteById(creditCardId);

        assertDoesNotThrow(() -> creditCardService.deleteCreditCard(creditCardId));
        verify(repository).deleteById(creditCardId);
    }

    @Test
    void shouldSetDefaultCreditCardSuccessfully() {
        when(repository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(repository.findByCustomerId(customerId)).thenReturn(List.of(creditCard));
        when(repository.save(any(CreditCard.class))).thenReturn(creditCard);

        CreditCardResponse result = creditCardService.setDefaultCreditCard(creditCardId);

        assertNotNull(result);
        assertTrue(result.getIsDefault());
        verify(repository, times(1)).save(any(CreditCard.class));
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> creditCardService.createCreditCard(request));

        assertEquals("Cliente não encontrado", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreditCardNotFound() {
        when(repository.findById(creditCardId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> creditCardService.getCreditCard(creditCardId));

        assertEquals("Cartão de crédito não encontrado", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenProviderCardIdAlreadyExists() {
        lenient().when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        lenient().when(repository.existsByProviderCardId("card_123456789")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> creditCardService.createCreditCard(request));

        assertEquals("Cartão já cadastrado", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDefaultCreditCardNotFound() {
        when(repository.findByCustomerIdAndIsDefaultTrue(customerId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> creditCardService.getDefaultCreditCard(customerId));

        assertEquals("Cliente não possui cartão padrão", exception.getMessage());
    }

    @Test
    void shouldUnsetOtherDefaultCardsWhenSettingNewDefault() {
        CreditCard existingDefaultCard = new CreditCard();
        existingDefaultCard.setId(UUID.randomUUID());
        existingDefaultCard.setIsDefault(true);
        existingDefaultCard.setCustomer(customer);

        when(repository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(repository.findByCustomerId(customerId)).thenReturn(List.of(existingDefaultCard, creditCard));
        when(repository.save(any(CreditCard.class))).thenReturn(creditCard);

        CreditCardResponse result = creditCardService.setDefaultCreditCard(creditCardId);

        assertNotNull(result);
        assertTrue(result.getIsDefault());
        verify(repository, times(2)).save(any(CreditCard.class));
    }
}
