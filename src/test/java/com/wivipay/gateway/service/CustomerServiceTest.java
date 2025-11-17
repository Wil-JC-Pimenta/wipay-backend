package com.wivipay.gateway.service;

import com.wivipay.gateway.dto.CustomerRequest;
import com.wivipay.gateway.dto.CustomerResponse;
import com.wivipay.gateway.model.Customer;
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
class CustomerServiceTest {

    @Mock
    private CustomerRepository repository;

    @Mock
    private BusinessValidationService businessValidationService;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRequest request;
    private Customer customer;
    private CustomerResponse expectedResponse;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        request = new CustomerRequest();
        request.setExternalId("CLI001");
        request.setName("João Silva");
        request.setEmail("joao.silva@email.com");
        request.setDocument("12345678901");
        request.setPhone("+5511999999999");

        customer = new Customer();
        customer.setId(customerId);
        customer.setExternalId("CLI001");
        customer.setName("João Silva");
        customer.setEmail("joao.silva@email.com");
        customer.setDocument("12345678901");
        customer.setPhone("+5511999999999");
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);

        expectedResponse = new CustomerResponse();
        expectedResponse.setId(customerId);
        expectedResponse.setExternalId("CLI001");
        expectedResponse.setName("João Silva");
        expectedResponse.setEmail("joao.silva@email.com");
        expectedResponse.setDocument("12345678901");
        expectedResponse.setPhone("+5511999999999");
        expectedResponse.setCreatedAt(now);
        expectedResponse.setUpdatedAt(now);
    }

    @Test
    void shouldCreateCustomerSuccessfully() {
        when(repository.existsByExternalId("CLI001")).thenReturn(false);
        when(repository.existsByEmail("joao.silva@email.com")).thenReturn(false);
        when(repository.existsByDocument("12345678901")).thenReturn(false);
        when(repository.save(any(Customer.class))).thenReturn(customer);

        CustomerResponse result = customerService.createCustomer(request);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getExternalId(), result.getExternalId());
        assertEquals(expectedResponse.getName(), result.getName());
        assertEquals(expectedResponse.getEmail(), result.getEmail());
        verify(repository).save(any(Customer.class));
    }

    @Test
    void shouldUpdateCustomerSuccessfully() {
        when(repository.findById(customerId)).thenReturn(Optional.of(customer));
        when(repository.existsByExternalId("CLI001")).thenReturn(false);
        when(repository.existsByEmail("joao.silva@email.com")).thenReturn(false);
        when(repository.existsByDocument("12345678901")).thenReturn(false);
        when(repository.save(any(Customer.class))).thenReturn(customer);

        CustomerResponse result = customerService.updateCustomer(customerId, request);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        verify(repository).save(any(Customer.class));
    }

    @Test
    void shouldGetCustomerSuccessfully() {
        when(repository.findById(customerId)).thenReturn(Optional.of(customer));

        CustomerResponse result = customerService.getCustomer(customerId);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getName(), result.getName());
    }

    @Test
    void shouldGetCustomerByExternalIdSuccessfully() {
        when(repository.findByExternalId("CLI001")).thenReturn(Optional.of(customer));

        CustomerResponse result = customerService.getCustomerByExternalId("CLI001");

        assertNotNull(result);
        assertEquals(expectedResponse.getExternalId(), result.getExternalId());
    }

    @Test
    void shouldGetAllCustomersSuccessfully() {
        when(repository.findAll()).thenReturn(List.of(customer));

        List<CustomerResponse> result = customerService.getAllCustomers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponse.getId(), result.get(0).getId());
    }

    @Test
    void shouldDeleteCustomerSuccessfully() {
        when(repository.existsById(customerId)).thenReturn(true);
        doNothing().when(repository).deleteById(customerId);

        assertDoesNotThrow(() -> customerService.deleteCustomer(customerId));
        verify(repository).deleteById(customerId);
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        when(repository.findById(customerId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.getCustomer(customerId));

        assertEquals("Cliente não encontrado", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenExternalIdAlreadyExists() {
        when(repository.existsByExternalId("CLI001")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.createCustomer(request));

        assertEquals("ID externo já cadastrado", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        lenient().when(repository.existsByExternalId("CLI001")).thenReturn(false);
        lenient().when(repository.existsByEmail("joao.silva@email.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.createCustomer(request));

        assertEquals("Email já cadastrado", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDocumentAlreadyExists() {
        lenient().when(repository.existsByExternalId("CLI001")).thenReturn(false);
        lenient().when(repository.existsByEmail("joao.silva@email.com")).thenReturn(false);
        lenient().when(repository.existsByDocument("12345678901")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.createCustomer(request));

        assertEquals("Documento já cadastrado", exception.getMessage());
    }
}
