# Gateway de Pagamentos - WiviPay

## Status do Projeto

**Versão**: 1.0.0  
**Status**: Em Desenvolvimento  
**Última Atualização**: Novembro de 2025

### Ambiente de Desenvolvimento

- **Porta da API**: 8082
- **Context Path**: `/api`
- **Perfil Ativo**: `postgres` (PostgreSQL)
- **Banco de Dados**: PostgreSQL conectado
- **RabbitMQ**: Não configurado (inativo)

### Endpoints Disponíveis

- **API Base**: `http://localhost:8082/api`
- **Swagger UI**: `http://localhost:8082/api/swagger-ui.html`
- **OpenAPI Docs**: `http://localhost:8082/api/v3/api-docs`
- **Health Check**: `http://localhost:8082/api/actuator/health`

### Funcionalidades

- Sistema de Pagamentos: Autorização, captura, estorno e consulta
- Gestão de Clientes: CRUD completo com validações de negócio
- Gestão de Cartões: CRUD com suporte a cartão padrão
- Auditoria: Logs completos de transações
- Validações: Regras de negócio complexas
- Métricas: Sistema de monitoramento com Micrometer
- Segurança: OAuth2 + JWT + Keycloak
- Documentação: OpenAPI 3.0 completa
- Testes: 70 testes unitários implementados

---

## Configuração do Ambiente

### Pré-requisitos

- Docker Desktop
- Java 17+
- Maven 3.6+
- PostgreSQL 14+ (ou Docker)
- Insomnia ou Postman

### Configuração de Variáveis de Ambiente

O projeto utiliza variáveis de ambiente para configurações sensíveis. Siga os passos abaixo:

1. Copie o arquivo `.env.example` para `.env`:

   ```bash
   cp .env.example .env
   ```

2. Edite o arquivo `.env` com suas configurações:

   ```bash
   # Configurações do Banco de Dados
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=wivipay
   DB_USER=postgres
   DB_PASSWORD=sua_senha_segura

   # Configurações do RabbitMQ
   SPRING_RABBITMQ_HOST=localhost
   SPRING_RABBITMQ_PORT=5672
   SPRING_RABBITMQ_USERNAME=guest
   SPRING_RABBITMQ_PASSWORD=guest

   # Configurações do Keycloak
   KEYCLOAK_ADMIN=admin
   KEYCLOAK_ADMIN_PASSWORD=sua_senha_admin
   KC_DB_PASSWORD=sua_senha_db_keycloak
   POSTGRES_PASSWORD=sua_senha_postgres

   # Configurações do Gateway
   GATEWAY_CLIENT_ID=gateway
   GATEWAY_CLIENT_SECRET=seu_client_secret

   # Configurações dos Provedores de Pagamento
   PAYPAL_CLIENT_ID=seu_client_id_paypal
   PAYPAL_CLIENT_SECRET=seu_client_secret_paypal
   STRIPE_API_KEY=sua_chave_stripe
   CIELO_MERCHANT_ID=seu_merchant_id_cielo
   CIELO_MERCHANT_KEY=sua_chave_cielo
   ```


## Configuração do Keycloak

### Iniciar o Keycloak

```bash
cd keycloak
docker compose -f keycloak-compose.yml up -d
```

O Keycloak estará disponível em: `http://localhost:8180`

### Configurações Realizadas

- **Realm**: gateway
- **Cliente**: wivipay-gateway
  - Client authentication: ON
  - Authorization: ON
  - Standard flow: ON
  - Direct access grants: ON

#### URLs Configuradas

- Root URL: `http://localhost:8082`
- Home URL: `http://localhost:8082/api`
- Valid redirect URIs:
  ```
  http://localhost:8082/*
  http://localhost:8180/*
  http://localhost:3000/*
  ```
- Valid post logout redirect URIs:
  ```
  http://localhost:8082/*
  http://localhost:3000/*
  ```
- Web origins:
  ```
  http://localhost:8082
  http://localhost:3000
  ```

## Autenticação

### Obter Token

**Endpoint**: `POST http://localhost:8180/realms/gateway/protocol/openid-connect/token`

**Headers**:
```
Content-Type: application/x-www-form-urlencoded
```

**Body (x-www-form-urlencoded)**:

```
grant_type: password
client_id: wivipay-gateway
client_secret: [SEU_CLIENT_SECRET]
username: admin
password: admin
```

## Portas Utilizadas

- 8082: API Gateway
- 8180: Keycloak
- 5432: PostgreSQL Principal
- 5433: PostgreSQL Keycloak
- 5672: RabbitMQ
- 15672: RabbitMQ Management
- 9090: Prometheus
- 3000: Grafana

## Documentação Adicional

- [Swagger UI](http://localhost:8082/api/swagger-ui.html)
- [Keycloak Admin Console](http://localhost:8180)

## Ambiente de Desenvolvimento

### Docker Compose

O projeto utiliza múltiplos arquivos docker-compose:

- `docker-compose.yml`: Serviços principais (API, DB, RabbitMQ, etc.)
- `keycloak/keycloak-compose.yml`: Keycloak e seu banco de dados

### Variáveis de Ambiente

Configure as seguintes variáveis conforme necessário:

- `STRIPE_API_KEY`
- Outras variáveis conforme necessário

## Troubleshooting

### Keycloak

Se o token não estiver funcionando, verificar:

1. Client Secret
2. Configurações do cliente no Keycloak
3. Roles e permissões

### Docker

- Certifique-se que o Docker Desktop está rodando
- Use o terminal do Docker Desktop para comandos docker
- Verifique logs: `docker compose logs -f`

## Contribuição

1. Clone o repositório
2. Crie uma branch para sua feature
3. Faça commit das alterações
4. Abra um Pull Request

## Licença

Este projeto está licenciado sob a **MIT License** - veja o arquivo [LICENSE](LICENSE) para detalhes.

## Documentação Visual

### Configuração do Ambiente

#### Docker

![Configuração Docker](image-doc-wivipay/Docker.png)

*Configuração do ambiente Docker para desenvolvimento*

#### Keycloak

![Configuração Keycloak](image-doc-wivipay/keycloack.png)

*Interface do Keycloak para gerenciamento de autenticação*

### Configurações do Keycloak

#### Realm e Cliente

![Configuração Realm](image-doc-wivipay/Keycloack-configuration-gateway-realm.png)

*Configuração do Realm gateway*

![Configuração Cliente](image-doc-wivipay/keycloack-configuration-client.png)

*Configuração do cliente wivipay-gateway*

#### Roles e Permissões

![Configuração Roles](image-doc-wivipay/keycloack-configuration-client-Role.png)

*Configuração de roles para o cliente*

#### Credenciais

![Configuração Credenciais](image-doc-wivipay/keycloack-configuration-Client_Id_Secret_Key.png)

*Configuração de client_id e client_secret*

### Endpoints e Documentação

#### Swagger

![Documentação Swagger](image-doc-wivipay/Swagger-DOC-Endpoints.png)

*Documentação dos endpoints no Swagger*

#### Endpoints de Pagamento

##### Autorização

![Endpoint Autorização](image-doc-wivipay/POST-payments-authorize.png)

*Endpoint para autorização de pagamento*

![Resposta Autorização](image-doc-wivipay/POST-payments-authorize-Response-200-400.png)

*Respostas de sucesso para autorização*

![Erros Autorização](image-doc-wivipay/POST-payments-authorize-Response-401-500.png)

*Respostas de erro para autorização*

##### Captura

![Endpoint Captura](image-doc-wivipay/POST-payments-capture-transactionId.png)

*Endpoint para captura de pagamento*

![Resposta Captura](image-doc-wivipay/POST-payments-capture-transactionId-Response-200-400.png)

*Respostas de sucesso para captura*

![Erros Captura](image-doc-wivipay/POST-payments-capture-transactionId-Response-401-404-500.png)

*Respostas de erro para captura*

##### Reembolso

![Endpoint Reembolso](image-doc-wivipay/POST-payments-refund-transactionId.png)

*Endpoint para reembolso de pagamento*

![Resposta Reembolso](image-doc-wivipay/POST-payments-refund-transactionId-Response-200-400.png)

*Respostas de sucesso para reembolso*

![Erros Reembolso](image-doc-wivipay/POST-payments-refund-transactionId-Response-401-404-500.png)

*Respostas de erro para reembolso*

##### Consulta

![Endpoint Consulta](image-doc-wivipay/GET-Paymentes-id.png)

*Endpoint para consulta de pagamento*

![Resposta Consulta](image-doc-wivipay/GET-Paymentes-id-Response.png)

*Respostas para consulta de pagamento*

### Schemas

#### Request

![Schema Request](image-doc-wivipay/Schemas-paymentRequest.png)

*Schema para requisição de pagamento*

#### Response

![Schema Response](image-doc-wivipay/Schemas-paymentResponse.png)

*Schema para resposta de pagamento*

## Estrutura do Banco de Dados

O sistema utiliza um banco de dados PostgreSQL com as seguintes tabelas:

### 1. payment_transactions

Armazena todas as transações de pagamento.

**Campos principais**:
- `id` (UUID): Identificador único da transação
- `provider` (VARCHAR): Provedor de pagamento (ex: stripe, cielo)
- `provider_transaction_id` (VARCHAR): ID da transação no provedor
- `amount` (DECIMAL): Valor da transação
- `currency` (VARCHAR): Moeda (ex: BRL, USD)
- `status` (VARCHAR): Status do pagamento
- `payment_method` (VARCHAR): Método de pagamento
- `raw_response` (TEXT): Resposta completa do provedor
- `error_message` (TEXT): Mensagem de erro, se houver
- `created_at` (TIMESTAMP): Data de criação
- `updated_at` (TIMESTAMP): Data de atualização
- `description` (VARCHAR): Descrição do pagamento
- `customer_id` (VARCHAR): ID do cliente
- `metadata` (TEXT): Metadados adicionais

### 2. customers

Armazena informações dos clientes.

**Campos principais**:
- `id` (UUID): Identificador único do cliente
- `external_id` (VARCHAR): ID externo do cliente
- `name` (VARCHAR): Nome do cliente
- `email` (VARCHAR): Email do cliente
- `document` (VARCHAR): Documento (CPF/CNPJ)
- `phone` (VARCHAR): Telefone
- `created_at` (TIMESTAMP): Data de criação
- `updated_at` (TIMESTAMP): Data de atualização

### 3. credit_cards

Armazena cartões de crédito dos clientes.

**Campos principais**:
- `id` (UUID): Identificador único do cartão
- `customer_id` (UUID): ID do cliente (FK)
- `provider_card_id` (VARCHAR): ID do cartão no provedor
- `last_four_digits` (VARCHAR): Últimos 4 dígitos
- `brand` (VARCHAR): Bandeira do cartão
- `expiration_month` (INTEGER): Mês de expiração
- `expiration_year` (INTEGER): Ano de expiração
- `is_default` (BOOLEAN): Cartão padrão
- `created_at` (TIMESTAMP): Data de criação
- `updated_at` (TIMESTAMP): Data de atualização

### 4. transaction_logs

Armazena histórico de alterações nas transações.

**Campos principais**:
- `id` (UUID): Identificador único do log
- `transaction_id` (UUID): ID da transação (FK)
- `status` (VARCHAR): Status registrado
- `message` (TEXT): Mensagem do log
- `created_at` (TIMESTAMP): Data de criação

### Índices

Para otimizar as consultas, foram criados os seguintes índices:

- `idx_payment_transactions_provider`: Índice no campo provider da tabela payment_transactions
- `idx_payment_transactions_status`: Índice no campo status da tabela payment_transactions
- `idx_payment_transactions_customer_id`: Índice no campo customer_id da tabela payment_transactions
- `idx_customers_external_id`: Índice no campo external_id da tabela customers
- `idx_credit_cards_customer_id`: Índice no campo customer_id da tabela credit_cards
- `idx_transaction_logs_transaction_id`: Índice no campo transaction_id da tabela transaction_logs

### Relacionamentos

- `credit_cards.customer_id` → `customers.id`
- `transaction_logs.transaction_id` → `payment_transactions.id`

## Diagrama MER (Modelo Entidade-Relacionamento)

```mermaid
erDiagram
    PAYMENT_TRANSACTIONS ||--o{ TRANSACTION_LOGS : "logs"
    CUSTOMERS ||--o{ CREDIT_CARDS : "possui"
    CUSTOMERS ||--o{ PAYMENT_TRANSACTIONS : "realiza"
    
    PAYMENT_TRANSACTIONS {
        uuid id PK
        varchar provider "stripe, cielo, paypal"
        varchar provider_transaction_id "ID no provedor"
        decimal amount "Valor da transação"
        varchar currency "BRL, USD, etc."
        varchar status "PENDING, AUTHORIZED, CAPTURED, REFUNDED, FAILED"
        varchar payment_method "Método de pagamento"
        text raw_response "Resposta bruta do provedor"
        text error_message "Mensagem de erro"
        varchar description "Descrição da transação"
        varchar customer_id "ID externo do cliente"
        text metadata "Metadados JSON"
        timestamp created_at
        timestamp updated_at
    }
    
    CUSTOMERS {
        uuid id PK
        varchar external_id UK "ID externo único"
        varchar name "Nome completo"
        varchar email UK "Email único"
        varchar document UK "CPF/CNPJ único"
        varchar phone "Telefone"
        timestamp created_at
        timestamp updated_at
    }
    
    CREDIT_CARDS {
        uuid id PK
        uuid customer_id FK "Referência para customers"
        varchar provider_card_id UK "ID único no provedor"
        varchar last_four_digits "Últimos 4 dígitos"
        varchar brand "VISA, MASTERCARD, etc."
        integer expiration_month "1-12"
        integer expiration_year ">= 2024"
        boolean is_default "Cartão padrão"
        timestamp created_at
        timestamp updated_at
    }
    
    TRANSACTION_LOGS {
        uuid id PK
        uuid transaction_id FK "Referência para payment_transactions"
        varchar status "Status registrado"
        text message "Mensagem descritiva"
        timestamp created_at
    }
```

### Descrição do Diagrama MER

1. **Entidades**:
   - `payment_transactions`: Transações de pagamento
   - `customers`: Clientes
   - `credit_cards`: Cartões de crédito
   - `transaction_logs`: Logs de transações

2. **Relacionamentos**:
   - Um cliente pode ter vários cartões de crédito (1:N)
   - Um cliente pode realizar várias transações (1:N)
   - Uma transação pode ter vários logs (1:N)

3. **Atributos**:
   - PK: Chave primária
   - FK: Chave estrangeira
   - UK: Unique Key
   - Demais campos com seus respectivos tipos

4. **Cardinalidades**:
   - `||--o{`: Um para muitos (1:N)
   - `||--||`: Um para um (1:1)

## Diagrama UML - Classes

```mermaid
classDiagram
    class PaymentController {
        -PaymentService paymentService
        +authorize(PaymentRequest) PaymentResponse
        +capture(UUID) PaymentResponse
        +refund(UUID, BigDecimal) PaymentResponse
        +getPayment(UUID) PaymentResponse
    }
    
    class PaymentService {
        -List~PaymentProvider~ providers
        -PaymentTransactionRepository repository
        -TransactionLogService transactionLogService
        -BusinessValidationService businessValidationService
        +authorize(PaymentRequest) PaymentResponse
        +capture(UUID) PaymentResponse
        +refund(UUID, BigDecimal) PaymentResponse
        +getPayment(UUID) PaymentResponse
    }
    
    class PaymentProvider {
        <<interface>>
        +getName() String
        +authorize(PaymentRequest) PaymentResponse
        +capture(String) PaymentResponse
        +refund(String, BigDecimal) PaymentResponse
        +supports(String) boolean
    }
    
    class StripeProvider {
        -String stripeApiKey
        +getName() String
        +authorize(PaymentRequest) PaymentResponse
        +capture(String) PaymentResponse
        +refund(String, BigDecimal) PaymentResponse
    }
    
    class CieloProvider {
        -String apiUrl
        -String merchantId
        -String merchantKey
        +getName() String
        +authorize(PaymentRequest) PaymentResponse
        +capture(String) PaymentResponse
        +refund(String, BigDecimal) PaymentResponse
    }
    
    class PayPalProvider {
        -String apiUrl
        -String clientId
        -String clientSecret
        +getName() String
        +authorize(PaymentRequest) PaymentResponse
        +capture(String) PaymentResponse
        +refund(String, BigDecimal) PaymentResponse
    }
    
    class PaymentTransaction {
        -UUID id
        -String provider
        -String providerTransactionId
        -BigDecimal amount
        -String currency
        -PaymentStatus status
        -String paymentMethod
        -String rawResponse
        -String errorMessage
        -String description
        -String customerId
        -String metadata
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
    }
    
    class Customer {
        -UUID id
        -String externalId
        -String name
        -String email
        -String document
        -String phone
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
    }
    
    class CreditCard {
        -UUID id
        -Customer customer
        -String providerCardId
        -String lastFourDigits
        -String brand
        -Integer expirationMonth
        -Integer expirationYear
        -Boolean isDefault
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
    }
    
    class TransactionLog {
        -UUID id
        -PaymentTransaction transaction
        -String status
        -String message
        -LocalDateTime createdAt
    }
    
    class CustomerService {
        -CustomerRepository repository
        +create(CustomerRequest) CustomerResponse
        +getById(UUID) CustomerResponse
        +getByExternalId(String) CustomerResponse
        +update(UUID, CustomerRequest) CustomerResponse
        +delete(UUID) void
        +listAll() List~CustomerResponse~
    }
    
    class CreditCardService {
        -CreditCardRepository repository
        -CustomerRepository customerRepository
        +create(CreditCardRequest) CreditCardResponse
        +getById(UUID) CreditCardResponse
        +getByCustomerId(UUID) List~CreditCardResponse~
        +getDefaultCard(UUID) CreditCardResponse
        +setDefaultCard(UUID) CreditCardResponse
        +update(UUID, CreditCardRequest) CreditCardResponse
        +delete(UUID) void
    }
    
    class TransactionLogService {
        -TransactionLogRepository repository
        +logPaymentAuthorization(PaymentTransaction) void
        +logPaymentCapture(PaymentTransaction) void
        +logPaymentRefund(PaymentTransaction) void
        +getLogsByTransactionId(UUID) List~TransactionLog~
    }
    
    class BusinessValidationService {
        +validatePaymentRequest(PaymentRequest) void
        +validateCustomer(CustomerRequest) void
        +validateCreditCard(CreditCardRequest) void
    }
    
    PaymentController --> PaymentService
    PaymentService --> PaymentProvider
    PaymentService --> PaymentTransaction
    PaymentService --> TransactionLogService
    PaymentService --> BusinessValidationService
    PaymentProvider <|.. StripeProvider
    PaymentProvider <|.. CieloProvider
    PaymentProvider <|.. PayPalProvider
    CustomerService --> Customer
    CreditCardService --> CreditCard
    CreditCardService --> Customer
    TransactionLogService --> TransactionLog
    TransactionLog --> PaymentTransaction
    CreditCard --> Customer
```

## Diagrama UML - Sequência (Autorização de Pagamento)

```mermaid
sequenceDiagram
    participant Client
    participant PaymentController
    participant PaymentService
    participant BusinessValidationService
    participant PaymentProvider
    participant PaymentTransactionRepository
    participant TransactionLogService
    
    Client->>PaymentController: POST /payments/authorize
    PaymentController->>PaymentService: authorize(PaymentRequest)
    PaymentService->>BusinessValidationService: validatePaymentRequest(request)
    BusinessValidationService-->>PaymentService: validation result
    
    PaymentService->>PaymentService: findProvider(provider)
    PaymentService->>PaymentProvider: authorize(request)
    PaymentProvider-->>PaymentService: PaymentResponse
    
    PaymentService->>PaymentTransactionRepository: save(transaction)
    PaymentTransactionRepository-->>PaymentService: PaymentTransaction
    
    PaymentService->>TransactionLogService: logPaymentAuthorization(transaction)
    TransactionLogService-->>PaymentService: void
    
    PaymentService-->>PaymentController: PaymentResponse
    PaymentController-->>Client: HTTP 200 OK
```

## Arquitetura

### Padrões Utilizados

- **Clean Architecture**: Separação clara de responsabilidades
- **Strategy Pattern**: Para provedores de pagamento
- **Repository Pattern**: Para acesso a dados
- **Service Layer**: Para lógica de negócio
- **DTO Pattern**: Para transferência de dados
- **Builder Pattern**: Para construção de objetos complexos

### Tecnologias Core

- **Backend**: Java 17 + Spring Boot 3.2.2
- **Database**: PostgreSQL (produção) + H2 (desenvolvimento)
- **ORM**: Spring Data JPA + Hibernate 6.4.1
- **Segurança**: Spring Security + OAuth2 + JWT
- **Documentação**: OpenAPI 3.0 + Swagger UI
- **Mensageria**: RabbitMQ (AMQP) - configurado mas não rodando
- **Monitoramento**: Spring Boot Actuator + Micrometer

### Diagrama da Arquitetura

```mermaid
graph TD
    subgraph "Frontend"
        A[Cliente Web] --> B[Cliente Mobile]
    end

    subgraph "API Gateway"
        C[WiviPay API] --> D[Keycloak]
        C --> E[Banco de Dados]
        C --> F[RabbitMQ]
    end

    subgraph "Serviços de Pagamento"
        G[Cielo] --> C
        H[PayPal] --> C
        I[Stripe] --> C
    end

    subgraph "Monitoramento"
        J[Prometheus] --> C
        K[Grafana] --> J
    end

    A --> C
    B --> C
```

### Estrutura de Diretórios

```
src/main/java/com/wivipay/gateway/
├── config/           # Configurações (Security, Metrics, etc.)
├── controller/       # Controllers REST
├── dto/             # Data Transfer Objects
├── model/           # Entidades JPA
├── provider/        # Provedores de pagamento
├── repository/      # Repositórios JPA
├── service/         # Lógica de negócio
└── GatewayApplication.java

src/main/resources/
├── db/migration/    # Migrações Flyway
├── application.yml  # Configuração padrão
├── application-h2.yml
└── application-postgres.yml
```

### Componentes da Arquitetura

1. **Frontend**
   - Cliente Web (React/Angular)
   - Cliente Mobile (React Native/Flutter)

2. **API Gateway (WiviPay)**
   - API REST desenvolvida em Java 17+
   - Documentação via Swagger
   - Endpoints para autorização, captura, reembolso e consulta de pagamentos
   - Integração com múltiplos gateways de pagamento

3. **Autenticação e Autorização**
   - Keycloak para gerenciamento de identidade
   - OAuth2/OpenID Connect
   - JWT para tokens de acesso
   - Roles e permissões configuráveis

4. **Banco de Dados**
   - PostgreSQL para dados principais
   - PostgreSQL para Keycloak
   - Migrações automáticas com Flyway

5. **Message Broker**
   - RabbitMQ para comunicação assíncrona
   - Filas para processamento de pagamentos
   - Retry e dead letter queues

6. **Serviços de Pagamento**
   - Integração com Cielo
   - Integração com PayPal
   - Integração com Stripe
   - Padrão Strategy para diferentes providers

7. **Monitoramento**
   - Prometheus para métricas
   - Grafana para visualização
   - Logs estruturados
   - Alertas configuráveis

8. **Infraestrutura**
   - Docker para containerização
   - Docker Compose para orquestração
   - Ambiente de desenvolvimento isolado
   - CI/CD pipeline

A comunicação entre os componentes é feita via:

- HTTP/HTTPS para APIs REST
- AMQP para mensageria
- JWT para autenticação
- WebSockets para notificações em tempo real

Esta arquitetura permite:

- Alta disponibilidade
- Escalabilidade horizontal
- Segurança robusta
- Manutenibilidade
- Monitoramento efetivo
- Facilidade de deploy

## Configuração e Execução

### Execução Local

#### 1. Perfil PostgreSQL (Recomendado para Desenvolvimento)

```bash
# Configurar PostgreSQL primeiro
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

#### 2. Perfil H2 (Desenvolvimento Local)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

#### 3. Perfil Padrão

```bash
mvn spring-boot:run
```

### Configurações Disponíveis

- **`application.yml`**: Configuração padrão (PostgreSQL)
- **`application-h2.yml`**: Configuração H2 em memória
- **`application-postgres.yml`**: Configuração específica PostgreSQL

## Configuração do PostgreSQL

### Configuração Rápida

```bash
# Tornar o script executável
chmod +x setup-postgres.sh

# Executar configuração automática
./setup-postgres.sh
```

### Configuração Manual

```bash
# 1. Criar banco
sudo -u postgres psql -c "CREATE DATABASE wivipay;"

# 2. Configurar senha
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'postgres';"

# 3. Copiar configurações
cp env.example .env

# 4. Executar aplicação
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

### Variáveis de Ambiente

```bash
# Banco de Dados
DB_NAME=wivipay
DB_USER=postgres
DB_PASSWORD=postgres
DB_PORT=5432

# Aplicação
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/wivipay
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

## Testes e Qualidade

### Status dos Testes

- **Total de Testes**: 70
- **Testes de Serviço**: 70/70 Passando
- **Testes de Integração**: 2/2 Falhando (Docker não disponível)

### Cobertura de Testes

- **CustomerService**: 100% de cobertura
- **CreditCardService**: 100% de cobertura
- **TransactionLogService**: 100% de cobertura
- **BusinessValidationService**: 100% de cobertura
- **PaymentService**: 100% de cobertura

### Execução de Testes

```bash
# Executar todos os testes (exceto integração)
mvn test -DexcludedGroups="integration"

# Apenas testes de serviço
mvn test -Dtest="*ServiceTest"

# Testes de integração (requer Docker)
mvn test -Dgroups="integration"
```

## Documentação da API

### Swagger UI

Acesse `http://localhost:8082/api/swagger-ui.html` para:

- Visualizar todos os endpoints
- Testar as APIs interativamente
- Ver schemas e modelos
- Executar requisições de teste

### OpenAPI Specification

- **Endpoint**: `http://localhost:8082/api/v3/api-docs`
- **Formato**: JSON
- **Versão**: OpenAPI 3.0.1

### Endpoints Principais

#### Pagamentos (`/payments`)

- `POST /authorize` - Autorizar pagamento
- `POST /capture/{id}` - Capturar pagamento
- `POST /refund/{id}` - Estornar pagamento
- `GET /{id}` - Consultar pagamento

#### Clientes (`/customers`)

- `POST /` - Criar cliente
- `GET /{id}` - Consultar cliente
- `GET /external/{externalId}` - Por ID externo
- `PUT /{id}` - Atualizar cliente
- `DELETE /{id}` - Deletar cliente
- `GET /` - Listar todos

#### Cartões (`/credit-cards`)

- `POST /` - Criar cartão
- `GET /{id}` - Consultar cartão
- `GET /customer/{customerId}` - Por cliente
- `GET /customer/{customerId}/default` - Padrão
- `POST /{id}/set-default` - Definir padrão
- `PUT /{id}` - Atualizar cartão
- `DELETE /{id}` - Deletar cartão

#### Logs (`/transaction-logs`)

- `GET /transaction/{transactionId}` - Logs da transação
- `GET /transaction/{transactionId}/status/{status}` - Por status

## Monitoramento e Métricas

### Spring Boot Actuator

- **Health Check**: `/api/actuator/health`
- **Info**: `/api/actuator/info`
- **Métricas**: `/api/actuator/metrics`
- **Prometheus**: `/api/actuator/prometheus`

### Métricas Customizadas

- Contadores de pagamentos por status
- Timers de resposta por endpoint
- Gauges de volume de transações
- Métricas de negócio (sucesso, falha, etc.)

## Estrutura do Banco de Dados

### Tabelas Implementadas

- `payment_transactions` - Transações de pagamento
- `customers` - Clientes
- `credit_cards` - Cartões de crédito
- `transaction_logs` - Logs de auditoria

### Migrations Flyway

- `V1__create_tables.sql` - Tabela base de pagamentos
- `V2__create_customers_table.sql` - Tabela de clientes
- `V3__create_credit_cards_table.sql` - Tabela de cartões
- `V4__create_transaction_logs_table.sql` - Tabela de logs
- `V5__update_payment_transactions_table.sql` - Atualizações

### Relacionamentos e Constraints

- **Customers** → **CreditCards**: One-to-Many (um cliente pode ter vários cartões)
- **PaymentTransactions** → **TransactionLogs**: One-to-Many (uma transação pode ter vários logs)
- **Customers** → **PaymentTransactions**: One-to-Many (um cliente pode ter várias transações)
- **Foreign Keys**: Configuradas com CASCADE DELETE para logs
- **Unique Constraints**: external_id, email, document em customers; provider_card_id em credit_cards
- **Check Constraints**: Validações de mês (1-12) e ano (>= 2024) em cartões

## Segurança e Autenticação

### Configuração OAuth2

- **Provider**: Keycloak configurado
- **Realm**: `gateway`
- **Client**: `wivipay-gateway`
- **Grant Types**: Client Credentials

### Roles e Permissões

- `payments:read` - Leitura de pagamentos
- `payments:write` - Criação/modificação de pagamentos
- `customers:read` - Leitura de clientes
- `customers:write` - Criação/modificação de clientes
- `credit_cards:read` - Leitura de cartões
- `credit_cards:write` - Criação/modificação de cartões
- `transactions:read` - Leitura de logs de transação

## Próximos Passos

### Prioridade Alta (1-2 semanas)

1. Configurar RabbitMQ para mensageria
2. Configurar Keycloak para autenticação em produção
3. Implementar CI/CD com GitHub Actions
4. Configurar monitoramento com Prometheus + Grafana

### Prioridade Média (1-2 meses)

1. Implementar rate limiting e cache Redis
2. Adicionar testes de performance com Gatling
3. Implementar backup automático do banco
4. Configurar logs centralizados com ELK Stack

### Prioridade Baixa (3-6 meses)

1. Microserviços: Decomposição da aplicação
2. Kubernetes: Orquestração de containers
3. Service Mesh: Istio para comunicação
4. Machine Learning: Detecção de fraudes

## Contribuição

### Padrões de Commit (GitFlow)

```
feat: nova funcionalidade
fix: correção de bug
docs: documentação
style: formatação de código
refactor: refatoração
test: testes
chore: tarefas de manutenção
```

### Estrutura de Branches

- `main` - Código de produção
- `develop` - Código de desenvolvimento
- `feature/*` - Novas funcionalidades
- `hotfix/*` - Correções urgentes
- `release/*` - Preparação de releases

## Changelog

### v1.0.0 (Novembro 2025)

- Sistema de pagamentos completo implementado
- Gestão de clientes e cartões de crédito
- Sistema de auditoria e logs
- Validações de negócio implementadas
- Testes unitários (70 testes implementados)
- Documentação OpenAPI completa
- Configuração de métricas e monitoramento
- PostgreSQL configurado e funcionando
- Aplicação executando com sucesso
- Arquitetura limpa e escalável implementada
- MER corrigido e otimizado
- Migrações Flyway organizadas e sem conflitos

---

**Última atualização**: Novembro de 2025  
**Status**: Em Desenvolvimento

