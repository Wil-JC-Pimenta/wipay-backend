package com.wivipay.gateway.service;

import com.wivipay.gateway.dto.CustomerRequest;
import com.wivipay.gateway.dto.CustomerResponse;
import com.wivipay.gateway.model.Customer;
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
public class CustomerService {
    
    private final CustomerRepository repository;
    @SuppressWarnings("unused")
    private final BusinessValidationService businessValidationService;
    
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        validateCustomerRequest(request);
        
        Customer customer = new Customer();
        customer.setExternalId(request.getExternalId());
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setDocument(request.getDocument());
        customer.setPhone(request.getPhone());
        
        Customer savedCustomer = repository.save(customer);
        log.info("Cliente criado com sucesso: {}", savedCustomer.getId());
        
        return mapToCustomerResponse(savedCustomer);
    }
    
    @Transactional
    public CustomerResponse updateCustomer(UUID id, CustomerRequest request) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
        validateCustomerRequest(request);
        
        customer.setExternalId(request.getExternalId());
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setDocument(request.getDocument());
        customer.setPhone(request.getPhone());
        
        Customer updatedCustomer = repository.save(customer);
        log.info("Cliente atualizado com sucesso: {}", updatedCustomer.getId());
        
        return mapToCustomerResponse(updatedCustomer);
    }
    
    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(UUID id) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
        return mapToCustomerResponse(customer);
    }
    
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByExternalId(String externalId) {
        Customer customer = repository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
        return mapToCustomerResponse(customer);
    }
    
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return repository.findAll().stream()
                .map(this::mapToCustomerResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteCustomer(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Cliente não encontrado");
        }
        
        repository.deleteById(id);
        log.info("Cliente deletado com sucesso: {}", id);
    }
    
    private void validateCustomerRequest(CustomerRequest request) {
        if (request.getEmail() != null && repository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }
        
        if (request.getDocument() != null && repository.existsByDocument(request.getDocument())) {
            throw new RuntimeException("Documento já cadastrado");
        }
        
        if (repository.existsByExternalId(request.getExternalId())) {
            throw new RuntimeException("ID externo já cadastrado");
        }
    }
    
    private CustomerResponse mapToCustomerResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setExternalId(customer.getExternalId());
        response.setName(customer.getName());
        response.setEmail(customer.getEmail());
        response.setDocument(customer.getDocument());
        response.setPhone(customer.getPhone());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        return response;
    }
}
