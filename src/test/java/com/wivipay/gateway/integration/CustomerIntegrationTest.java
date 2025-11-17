package com.wivipay.gateway.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wivipay.gateway.dto.CustomerRequest;
import com.wivipay.gateway.model.Customer;
import com.wivipay.gateway.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@SuppressWarnings("null")
class CustomerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository repository;

    private CustomerRequest request;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        request = new CustomerRequest();
        request.setExternalId("CLI001");
        request.setName("João Silva");
        request.setEmail("joao.silva@email.com");
        request.setDocument("12345678901");
        request.setPhone("+5511999999999");
    }

    @Test
    @WithMockUser(authorities = "SCOPE_customers:write")
    void shouldCreateCustomerSuccessfully() throws Exception {
        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalId").value("CLI001"))
                .andExpect(jsonPath("$.name").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao.silva@email.com"))
                .andExpect(jsonPath("$.document").value("12345678901"))
                .andExpect(jsonPath("$.phone").value("+5511999999999"));
    }

    @Test
    @WithMockUser(authorities = "SCOPE_customers:write")
    void shouldUpdateCustomerSuccessfully() throws Exception {
        // Primeiro criar o cliente
        Customer customer = new Customer();
        customer.setExternalId("CLI001");
        customer.setName("João Silva");
        customer.setEmail("joao.silva@email.com");
        customer.setDocument("12345678901");
        customer.setPhone("+5511999999999");
        Customer savedCustomer = repository.save(customer);

        // Atualizar o cliente
        request.setName("João Silva Santos");
        request.setPhone("+5511888888888");

        mockMvc.perform(put("/customers/{id}", savedCustomer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("João Silva Santos"))
                .andExpect(jsonPath("$.phone").value("+5511888888888"));
    }

    @Test
    @WithMockUser(authorities = "SCOPE_customers:read")
    void shouldGetCustomerSuccessfully() throws Exception {
        Customer customer = repository.save(request.toCustomer());

        mockMvc.perform(get("/customers/{id}", customer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("CLI001"))
                .andExpect(jsonPath("$.name").value("João Silva"));
    }

    @Test
    @WithMockUser(authorities = "SCOPE_customers:read")
    void shouldGetCustomerByExternalIdSuccessfully() throws Exception {
        repository.save(request.toCustomer());

        mockMvc.perform(get("/customers/external/{externalId}", "CLI001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("CLI001"))
                .andExpect(jsonPath("$.name").value("João Silva"));
    }

    @Test
    @WithMockUser(authorities = "SCOPE_customers:read")
    void shouldGetAllCustomersSuccessfully() throws Exception {
        repository.save(request.toCustomer());

        CustomerRequest request2 = new CustomerRequest();
        request2.setExternalId("CLI002");
        request2.setName("Maria Santos");
        request2.setEmail("maria.santos@email.com");
        request2.setDocument("98765432100");
        request2.setPhone("+5511777777777");
        repository.save(request2.toCustomer());

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].externalId").value("CLI001"))
                .andExpect(jsonPath("$[1].externalId").value("CLI002"));
    }

    @Test
    @WithMockUser(authorities = "SCOPE_customers:write")
    void shouldDeleteCustomerSuccessfully() throws Exception {
        Customer customer = repository.save(request.toCustomer());

        mockMvc.perform(delete("/customers/{id}", customer.getId()))
                .andExpect(status().isNoContent());

        // Verificar se foi deletado
        assertFalse(repository.existsById(customer.getId()));
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_customers:write")
    void shouldReturnBadRequestWithInvalidRequest() throws Exception {
        request.setEmail("invalid-email");

        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_customers:write")
    void shouldReturnBadRequestWithMissingRequiredFields() throws Exception {
        request.setName(null);

        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_customers:read")
    void shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
        mockMvc.perform(get("/customers/{id}", "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_customers:read")
    void shouldReturnNotFoundWhenExternalIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/customers/external/{externalId}", "NON_EXISTENT"))
                .andExpect(status().isNotFound());
    }
}
