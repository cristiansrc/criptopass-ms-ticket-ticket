# Arquitectura - CriptoPass MS Ticket Ticket

## Visión General

Microservicio responsable de la gestión del ciclo de vida completo de boletas digitales dentro del ecosistema CriptoPass. Maneja desde la compra hasta la validación en entrada, incluyendo transferencia entre usuarios y revocación administrativa.

## Contexto del Sistema (Modelo C4 - Nivel 1)

```mermaid
graph TB
    subgraph "Usuarios"
        CLIENT[Cliente Web/Móvil]
        STAFF[Staff de Evento]
        ADMIN[Administrador]
    end

    subgraph "Ecosistema CriptoPass"
        GW[API Gateway<br/>Spring Cloud Gateway]
        KC[Keycloak<br/>Identity Provider]
    end

    subgraph "Microservicios"
        TICKET[MS Ticket Ticket<br/>⬅ Este servicio]
        EVENT[MS Event]
        PAYMENT[MS Payment]
        USER[MS User]
        BLOCKCHAIN[MS Blockchain]
    end

    subgraph "Infraestructura"
        DB[(PostgreSQL)]
        REDIS[(Redis Cache)]
    end

    CLIENT --> GW
    STAFF --> GW
    ADMIN --> GW
    GW --> KC
    GW --> TICKET
    TICKET --> EVENT
    TICKET --> PAYMENT
    TICKET --> USER
    TICKET --> BLOCKCHAIN
    TICKET --> DB
    TICKET --> REDIS
```

## Arquitectura Interna (Nivel 3 - Hexagonal)

```mermaid
graph TB
    subgraph "Capa de Adaptadores de Entrada"
        REST[REST Controllers<br/>Spring MVC]
    end

    subgraph "Capa de Aplicación"
        USE_CASES[Use Cases / Servicios de Aplicación]
        DTO_IN[Request DTOs]
        DTO_OUT[Response DTOs]
    end

    subgraph "Capa de Dominio"
        ENTITIES[Entidades de Dominio<br/>Ticket, TicketType, TicketStatus]
        DOMAIN_SVC[Servicios de Dominio<br/>TicketDomainService, ValidationDomainService]
        PORTS_IN[Inbound Ports<br/>interfaces]
        PORTS_OUT[Outbound Ports<br/>interfaces]
    end

    subgraph "Capa de Adaptadores de Salida"
        REPO_ADAPTER[Repository Adapter<br/>JPA/Spring Data]
        BC_ADAPTER[Blockchain Adapter<br/>REST Client]
        PAY_ADAPTER[Payment Adapter<br/>REST Client]
        EVT_ADAPTER[Event Adapter<br/>REST Client]
        USER_ADAPTER[User Adapter<br/>REST Client]
        ERROR_HANDLER[Global Error Handler<br/>@RestControllerAdvice]
    end

    subgraph "Infraestructura Externa"
        DB[(PostgreSQL)]
        BLOCKCHAIN[Blockchain Network]
        PAY_SVC[Payment Service]
        EVT_SVC[Event Service]
        USER_SVC[User Service]
    end

    REST --> USE_CASES
    USE_CASES --> PORTS_IN
    PORTS_IN --> ENTITIES
    PORTS_IN --> DOMAIN_SVC
    DOMAIN_SVC --> PORTS_OUT
    PORTS_OUT --> REPO_ADAPTER
    PORTS_OUT --> BC_ADAPTER
    PORTS_OUT --> PAY_ADAPTER
    PORTS_OUT --> EVT_ADAPTER
    PORTS_OUT --> USER_ADAPTER
    REPO_ADAPTER --> DB
    BC_ADAPTER --> BLOCKCHAIN
    PAY_ADAPTER --> PAY_SVC
    EVT_ADAPTER --> EVT_SVC
    USER_ADAPTER --> USER_SVC
    REST --> ERROR_HANDLER
```

## Paquetes Propuestos

```
com.criptopass.ms.ticket.ticket
├── adapter/
│   ├── in/                          # Adaptadores de entrada
│   │   ├── rest/                    # Controllers REST
│   │   │   ├── TicketController.kt
│   │   │   ├── TicketTypeController.kt
│   │   │   └── ValidationController.kt
│   │   └── security/                # Filtros de seguridad
│   │       └── JwtAuthenticationFilter.kt
│   └── out/                         # Adaptadores de salida
│       ├── persistence/             # Repositorios JPA
│       │   ├── TicketRepository.kt
│       │   └── TicketTypeRepository.kt
│       ├── blockchain/              # Cliente blockchain
│       │   └── BlockchainClient.kt
│       ├── payment/                 # Cliente de pagos
│       │   └── PaymentClient.kt
│       ├── event/                   # Cliente de eventos
│       │   └── EventClient.kt
│       └── user/                    # Cliente de usuarios
│           └── UserClient.kt
├── application/
│   ├── service/                     # Servicios de aplicación
│   │   ├── TicketService.kt
│   │   ├── TicketPurchaseService.kt
│   │   ├── TicketTransferService.kt
│   │   └── TicketValidationService.kt
│   ├── port/
│   │   ├── in/                      # Puertos de entrada (interfaces)
│   │   │   ├── ListTicketsUseCase.kt
│   │   │   ├── PurchaseTicketUseCase.kt
│   │   │   ├── TransferTicketUseCase.kt
│   │   │   └── ValidateTicketUseCase.kt
│   │   └── out/                     # Puertos de salida (interfaces)
│   │       ├── TicketRepositoryPort.kt
│   │       ├── BlockchainPort.kt
│   │       ├── PaymentPort.kt
│   │       ├── EventPort.kt
│   │       └── UserPort.kt
│   └── dto/                         # DTOs de aplicación
│       ├── request/
│       └── response/
├── domain/
│   ├── model/                       # Entidades de dominio
│   │   ├── Ticket.kt
│   │   ├── TicketType.kt
│   │   ├── TicketStatus.kt
│   │   └── EventSummary.kt
│   ├── exception/                   # Excepciones de dominio
│   │   ├── TicketNotFoundException.kt
│   │   ├── TicketNotOwnedByUserException.kt
│   │   ├── TicketAlreadyValidatedException.kt
│   │   └── InsufficientTicketsException.kt
│   └── service/                     # Lógica de dominio pura
│       └── TicketDomainService.kt
└── config/                          # Configuración
    ├── SecurityConfig.kt
    ├── OpenApiConfig.kt
    └── ApplicationConfig.kt
```

## Flujos Principales

### 1. Compra de Boleta

```mermaid
sequenceDiagram
    participant Client as Cliente
    participant GW as API Gateway
    participant KC as Keycloak
    participant API as Ticket API
    participant Domain as Domain Service
    participant DB as PostgreSQL
    participant Pay as Payment Service
    participant BC as Blockchain

    Client->>GW: POST /ticket-types/{id}/purchase
    GW->>KC: Validar JWT
    KC-->>GW: Token válido
    GW->>API: Forward request
    API->>Domain: purchaseTicket(ticketTypeId, quantity)
    Domain->>DB: Verificar disponibilidad
    DB-->>Domain: Stock disponible
    Domain->>DB: Reservar boletas (PENDING_PAYMENT)
    Domain->>Pay: Crear orden de pago
    Pay-->>Domain: paymentPreferenceId
    Domain->>BC: Pre-registrar tokens
    BC-->>Domain: blockchain_token_id
    Domain-->>API: TicketPurchaseResponse
    API-->>GW: 201 Created
    GW-->>Client: Orden de compra creada
```

### 2. Validación de Boleta en Entrada

```mermaid
sequenceDiagram
    participant Staff as Staff Evento
    participant GW as API Gateway
    participant KC as Keycloak
    participant API as Validation API
    participant Domain as Validation Service
    participant DB as PostgreSQL

    Staff->>GW: POST /validation/scan {qr_code}
    GW->>KC: Validar JWT + rol ADMIN/ORGANIZER
    KC-->>GW: Token válido con rol
    GW->>API: Forward request
    API->>Domain: scanTicket(qr_code)
    Domain->>DB: Buscar boleta por QR
    DB-->>Domain: Ticket encontrado
    Domain->>Domain: Verificar estado = ACTIVE
    Domain->>DB: Actualizar estado = VALIDATED
    Domain-->>API: TicketValidationResponse(valid=true)
    API-->>GW: 200 OK
    GW-->>Staff: Boleta válida
```

### 3. Transferencia de Boleta

```mermaid
sequenceDiagram
    participant Owner as Propietario
    participant GW as API Gateway
    participant KC as Keycloak
    participant API as Ticket API
    participant Domain as Transfer Service
    participant DB as PostgreSQL
    participant BC as Blockchain
    participant User as User Service

    Owner->>GW: POST /tickets/{id}/transfer {recipient_email}
    GW->>KC: Validar JWT
    KC-->>GW: Token válido
    GW->>API: Forward request
    API->>Domain: transferTicket(ticketId, recipientEmail)
    Domain->>DB: Verificar propiedad y estado
    DB-->>Domain: Ticket ACTIVE, owner coincide
    Domain->>User: Buscar usuario por email
    User-->>Domain: recipient_id encontrado
    Domain->>DB: Actualizar owner (TRANSFERRED)
    Domain->>BC: Transferir token blockchain
    BC-->>Domain: tx_hash confirmado
    Domain->>DB: Crear nuevo ticket para recipient (ACTIVE)
    Domain-->>API: TicketResponse (nuevo ticket ACTIVE del recipient)
    API-->>GW: 200 OK
    GW-->>Owner: Boleta transferida (nuevo ticket ACTIVE para destinatario)
```

## Modelo de Estados de Boleta

```mermaid
stateDiagram-v2
    [*] --> PENDING_PAYMENT: Compra iniciada
    PENDING_PAYMENT --> ACTIVE: Pago confirmado (webhook)
    PENDING_PAYMENT --> EXPIRED: Timeout sin pago

    ACTIVE --> VALIDATED: Escaneo en entrada
    ACTIVE --> TRANSFERRED: Transferencia a otro usuario
    ACTIVE --> REVOKED: Revocación admin (reembolso)

    TRANSFERRED --> ACTIVE: Nuevo propietario recibe boleta
    TRANSFERRED --> VALIDATED: Escaneo en entrada
    TRANSFERRED --> REVOKED: Revocación admin

    VALIDATED --> [*]: Boleta usada (terminal)
    REVOKED --> [*]: Boleta revocada (terminal)
    EXPIRED --> [*]: Boleta expirada (terminal)
```

## Seguridad

### Autenticación
- **Proveedor**: Keycloak (OAuth2 / OIDC)
- **Mecanismo**: Bearer JWT en header `Authorization`
- **Validación**: Verificación de firma, expiración y claims en API Gateway

### Autorización (RBAC)

| Endpoint | Roles Requeridos |
|---|---|
| `GET /ticket-types` | Público |
| `GET /tickets` | Cualquier usuario autenticado |
| `GET /tickets/{id}` | Owner o ADMIN |
| `GET /tickets/{id}/qr` | Owner o ADMIN |
| `POST /tickets/{id}/transfer` | Owner |
| `POST /ticket-types/{id}/purchase` | Cualquier usuario autenticado |
| `POST /tickets/{id}/revoke` | ADMIN, ORGANIZER |
| `GET /tickets/events/{id}` | ADMIN, ORGANIZER |
| `POST /validation/scan` | ADMIN, ORGANIZER |
| `POST /validation/{id}` | ADMIN, ORGANIZER |

### Protección de Datos
- Los tokens blockchain y hashes de transacción son inmutables
- Los QR codes tienen expiración configurable
- Los logs no deben incluir datos sensibles (emails completos, tokens)

## Escalabilidad

### Consideraciones
- **Lectura intensiva**: Los endpoints de consulta de boletas son los más frecuentes
- **Picos de carga**: Compra masiva al abrir venta de eventos populares
- **Validación concurrente**: Múltiples scanners en entrada de eventos grandes

### Estrategias
- Cache de tipos de boleta y resúmenes de evento (Redis)
- Paginación en todos los listados
- Validación optimista de stock con retry
- Índices en `owner_id`, `event_id`, `qr_code`, `status`

## Observabilidad

### Métricas Clave
| Métrica | Tipo | Alerta |
|---|---|---|
| `tickets.purchase.count` | Counter | Caída > 50% vs baseline |
| `tickets.validation.count` | Counter | - |
| `tickets.validation.failure_rate` | Gauge | > 5% |
| `tickets.transfer.count` | Counter | - |
| `tickets.blockchain.tx_latency` | Histogram | p99 > 5s |
| `tickets.payment.order.count` | Counter | - |
| `http.requests.duration` | Histogram | p99 > 2s |

### Logs Estructurados
- Formato JSON con `trace_id`, `span_id`, `user_id`, `ticket_id`
- Niveles: INFO para operaciones normales, WARN para reintentos, ERROR para fallos

### Health Checks
- `/actuator/health` - Estado general
- `/actuator/health/db` - Conexión PostgreSQL
- `/actuator/health/blockchain` - Conectividad blockchain
- `/actuator/health/payment` - Conectividad payment service
