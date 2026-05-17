# Task Board - CriptoPass MS Ticket Ticket

**Increment**: ms-ticket-ticket  
**Status**: implemented  
**Created**: 2026-05-17  
**Last Updated**: 2026-05-17

---

## Tareas de Implementación

### Fase 1: Configuración del Proyecto

#### Task 001
- **id**: `TASK-001`
- **title**: Configurar build.gradle con dependencias Spring Boot Kotlin
- **agent**: executor
- **spec_refs**: README.md (Stack Tecnológico), architecture.md (Paquetes Propuestos)
- **goal**: Configurar el archivo build.gradle con todas las dependencias necesarias para Spring Boot 4.0.6 con Kotlin 2.2.21
- **scope**: 
  - Spring Boot 4.0.6 starter web, validation, security, oauth2-resource-server, data-jpa
  - Flyway PostgreSQL
  - PostgreSQL driver
  - Kotlin stdlib, reflect, gradle plugin
  - Lombok (opcional, usar data classes en su lugar)
  - OpenAPI/Swagger UI
  - Actuator
  - Test: junit5, mockk, spring-boot-test, testcontainers
- **out_of_scope**: Configuración de application.yaml
- **inputs**: 
  - Stack definido en workspace README.md: Kotlin 2.2.21, Spring Boot 4.0.6, Gradle 8.14+
  - Paquetes en architecture.md
- **implementation_notes**: 
  - Usar Kotlin DSL (build.gradle.kts) o Groovy (build.gradle)
  - Incluir plugin de Spring Boot y Kotlin
  - Configurar Java 21 toolchain
  - Incluir dependencias para OpenAPI generator si se usa
  - Gradle 8.14+ requerido por Spring Boot 4.0.x
- **edge_cases**: 
  - Versiones compatibles entre Spring Boot 4.0.6 y Kotlin 2.2.21
  - Conflictos de dependencias de seguridad OAuth2
  - @MockBean reemplazado por @MockitoBean en Spring Boot 4.0
- **done_criteria**:
  - build.gradle tiene todas las dependencias declaradas
  - `./gradlew build` compila sin errores
  - `./gradlew test` ejecuta tests vacíos sin errores
- **verification**: 
  - `./gradlew dependencies --configuration runtimeClasspath` muestra todas las dependencias
  - `./gradlew build` exitoso
- **dependencies**: none
- **handoff_context**: Ninguno
- **source_of_truth**: README.md Stack Tecnológico
- **stale_terms_guard**: No usar aliases de versiones
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 002
- **id**: `TASK-002`
- **title**: Configurar application.yaml con propiedades Spring Boot
- **agent**: executor
- **spec_refs**: .env.example, README.md (Inicio Rápido)
- **goal**: Configurar application.yaml con todas las propiedades necesarias para Spring Boot, DB, Keycloak, servicios externos
- **scope**: 
  - Spring: nombre de aplicación, perfil activo, puerto
  - DataSource: URL, username, password, driver, HikariCP
  - Flyway: enabled, locations, baseline-on-migrate
  - OAuth2/OIDC: issuer-uri, client-id, client-secret
  - Servicios externos: payment, event, blockchain URLs y timeouts
  - Redis: host, port, password, TTL
  - Actuator: endpoints web exposure
  - Logging: nivel, formato
  - QR Code: TTL, secret
  - Compra: payment timeout, max tickets
- **out_of_scope**: Variables de entorno (se leen de .env)
- **inputs**: 
  - .env.example con todas las variables documentadas
  - README.md con prerrequisitos
- **implementation_notes**: 
  - Usar placeholders `${VAR:default}` para variables de entorno
  - Separar por perfiles si es necesario (dev, prod)
  - Incluir configuración de OAuth2 resource server
- **edge_cases**: 
  - Manejo de valores por defecto seguros para desarrollo
  - No hardcodear secrets en el archivo
- **done_criteria**:
  - application.yaml tiene todas las propiedades de .env.example
  - La aplicación inicia correctamente con `./gradlew bootRun`
  - Health checks responden correctamente
- **verification**: 
  - `curl http://localhost:8082/actuator/health` retorna 200
  - Logs muestran conexión exitosa a PostgreSQL
- **dependencies**: TASK-001
- **handoff_context**: Ninguno
- **source_of_truth**: .env.example
- **stale_terms_guard**: Usar nombres exactos de propiedades de Spring Boot 3.x
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

### Fase 2: Modelos de Dominio

#### Task 003
- **id**: `TASK-003`
- **title**: Crear enum TicketStatus
- **agent**: executor
- **spec_refs**: README.md (TicketStatus enum), OpenAPI (TicketStatus schema), architecture.md (Modelo de Estados)
- **goal**: Crear el enum TicketStatus con los 6 estados definidos
- **scope**: 
  - Archivo: `src/main/kotlin/com/criptopass/ms/ticket/ticket/domain/model/TicketStatus.kt`
  - Valores: PENDING_PAYMENT, ACTIVE, TRANSFERRED, VALIDATED, REVOKED, EXPIRED
- **out_of_scope**: Lógica de transiciones de estado (eso va en Domain Service)
- **inputs**: 
  - README.md línea 37: lista exacta de estados
  - OpenAPI.yaml línea 815-822: enum schema
- **implementation_notes**: 
  - Usar enum class de Kotlin
  - Considerar función para verificar si es estado terminal
- **edge_cases**: 
  - Manejo de casos en when expressions debe ser exhaustivo
- **done_criteria**:
  - Enum creado con los 6 valores exactos
  - Compilación exitosa
- **verification**: 
  - `./gradlew compileKotlin` sin errores
  - Test unitario verifica los 6 valores
- **dependencies**: TASK-001
- **handoff_context**: Usado por entidades y servicios
- **source_of_truth**: README.md línea 37
- **stale_terms_guard**: No usar aliases como "PENDING" o "USED"
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 004
- **id**: `TASK-004`
- **title**: Crear data class EventSummary
- **agent**: executor
- **spec_refs**: OpenAPI (EventSummary schema), README.md (Esquema de Boleta)
- **goal**: Crear data class EventSummary para representar resumen de evento
- **scope**: 
  - Archivo: `src/main/kotlin/com/criptopass/ms/ticket/ticket/domain/model/EventSummary.kt`
  - Campos: id (Long), name (String), start_date (Instant), venue_name (String)
- **out_of_scope**: Validaciones complejas
- **inputs**: 
  - OpenAPI.yaml línea 798-808: EventSummary schema
- **implementation_notes**: 
  - Usar data class de Kotlin
  - Usar Instant para fechas (Java 8 time API)
- **edge_cases**: 
  - venue_name puede ser null si no está asignado
- **done_criteria**:
  - Data class creada con campos exactos del schema
  - Compilación exitosa
- **verification**: 
  - `./gradlew compileKotlin` sin errores
- **dependencies**: TASK-001
- **handoff_context**: Usado en Ticket y TicketResponse
- **source_of_truth**: OpenAPI.yaml línea 798
- **stale_terms_guard**: No usar LocalDateTime, usar Instant
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 005
- **id**: `TASK-005`
- **title**: Crear data class TicketType
- **agent**: executor
- **spec_refs**: OpenAPI (TicketTypeResponse schema), README.md (Esquema de Boleta)
- **goal**: Crear data class TicketType para modelo de dominio
- **scope**: 
  - Archivo: `src/main/kotlin/com/criptopass/ms/ticket/ticket/domain/model/TicketType.kt`
  - Campos: id, event_id, name, description, price, quantity, available_quantity, max_per_user, created_at
- **out_of_scope**: Mapeo a entidad JPA
- **inputs**: 
  - OpenAPI.yaml línea 691-708: TicketTypeResponse schema
- **implementation_notes**: 
  - Usar data class de Kotlin
  - Separar modelo de dominio de DTOs de respuesta
- **edge_cases**: 
  - description puede ser null
  - available_quantity puede ser 0
- **done_criteria**:
  - Data class creada con campos del schema
  - Compilación exitosa
- **verification**: 
  - `./gradlew compileKotlin` sin errores
- **dependencies**: TASK-001, TASK-004
- **handoff_context**: Usado en Ticket y servicios
- **source_of_truth**: OpenAPI.yaml línea 691
- **stale_terms_guard**: No confundir con TicketTypeResponse DTO
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 006
- **id**: `TASK-006`
- **title**: Crear data class Ticket (modelo de dominio)
- **agent**: executor
- **spec_refs**: OpenAPI (TicketResponse schema), README.md (Esquema de Boleta), architecture.md (Modelo de Estados)
- **goal**: Crear data class Ticket como modelo de dominio principal
- **scope**: 
  - Archivo: `src/main/kotlin/com/criptopass/ms/ticket/ticket/domain/model/Ticket.kt`
  - Campos: id, event (EventSummary), ticket_type (TicketType), owner_id, owner_email, price, status (TicketStatus), qr_code, blockchain_token_id, blockchain_tx_hash, seat_number, purchased_at, validated_at, created_at, updated_at
  - Métodos de dominio para transiciones de estado
- **out_of_scope**: Persistencia JPA, validaciones de negocio complejas
- **inputs**: 
  - README.md línea 154-171: esquema completo
  - OpenAPI.yaml línea 649-689: TicketResponse schema
  - architecture.md línea 258-273: diagrama de estados
- **implementation_notes**: 
  - Usar data class de Kotlin
  - Incluir métodos para transiciones: activate(), validate(), transfer(), revoke(), expire()
  - Cada método debe verificar estado actual antes de transicionar
  - No permitir transiciones inválidas (lanzar excepción de dominio)
- **edge_cases**: 
  - No permitir validar ticket ya validado
  - No permitir transferir ticket revocado
  - QR code expirado no invalida el ticket, solo el QR
- **done_criteria**:
  - Data class creada con todos los campos
  - Métodos de transición implementados con validación de estado
  - Tests unitarios para cada transición válida e inválida
- **verification**: 
  - `./gradlew test` con cobertura > 80% en Ticket.kt
  - Tests verifican todas las transiciones del diagrama de estados
- **dependencies**: TASK-003, TASK-004, TASK-005
- **handoff_context**: Entidad central del dominio
- **source_of_truth**: README.md línea 154
- **stale_terms_guard**: No usar aliases de estados
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

### Fase 3: Excepciones de Dominio

#### Task 007
- **id**: `TASK-007`
- **title**: Crear excepciones de dominio
- **agent**: executor
- **spec_refs**: architecture.md (Paquetes Propuestos - domain/exception)
- **goal**: Crear todas las excepciones de dominio necesarias
- **scope**: 
  - TicketNotFoundException.kt
  - TicketNotOwnedByUserException.kt
  - TicketAlreadyValidatedException.kt
  - TicketNotTransferableException.kt
  - InsufficientTicketsException.kt
  - InvalidTicketStatusTransitionException.kt
- **out_of_scope**: Manejo de excepciones en controllers
- **inputs**: 
  - architecture.md línea 157-162: lista de excepciones
- **implementation_notes**: 
  - Extender RuntimeException
  - Incluir campos relevantes (ticketId, userId, currentState, etc.)
  - Usar mensajes claros en español o inglés (consistente)
- **edge_cases**: 
  - Cada excepción debe tener constructor con mensaje y causa
- **done_criteria**:
  - Todas las excepciones creadas
  - Compilación exitosa
- **verification**: 
  - `./gradlew compileKotlin` sin errores
- **dependencies**: TASK-003, TASK-006
- **handoff_context**: Usadas por servicios de dominio y aplicación
- **source_of_truth**: architecture.md línea 157
- **stale_terms_guard**: Nombres exactos como en architecture.md
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

### Fase 4: Puertos (Interfaces)

#### Task 008
- **id**: `TASK-008`
- **title**: Crear puertos de salida (Outbound Ports)
- **agent**: executor
- **spec_refs**: architecture.md (Paquetes Propuestos - port/out)
- **goal**: Crear interfaces para puertos de salida
- **scope**: 
  - TicketRepositoryPort.kt: métodos CRUD y queries especializadas
  - TicketTypeRepositoryPort.kt: métodos para TicketType
  - BlockchainPort.kt: registerToken(), transferToken()
  - PaymentPort.kt: createOrder(), getOrderStatus()
  - EventPort.kt: getEventById(), getEventTicketTypes()
  - UserPort.kt: getUserByEmail()
- **out_of_scope**: Implementaciones de los puertos
- **inputs**: 
  - architecture.md línea 143-148: lista de puertos de salida
  - OpenAPI.yaml: operaciones que requieren cada puerto
- **implementation_notes**: 
  - Usar interfaces de Kotlin
  - TicketRepositoryPort debe incluir métodos como: findById(), findByOwnerId(), findByEventId(), save(), findByQrCode()
  - Los puertos externos deben usar modelos de dominio, no DTOs
- **edge_cases**: 
  - Métodos que pueden retornar null vs lanzar excepción
  - Paginación en métodos de lista
- **done_criteria**:
  - Todas las interfaces creadas
  - Métodos definidos con firmas correctas
  - Compilación exitosa
- **verification**: 
  - `./gradlew compileKotlin` sin errores
- **dependencies**: TASK-003, TASK-004, TASK-005, TASK-006
- **handoff_context**: Implementados por adaptadores de salida
- **source_of_truth**: architecture.md línea 143
- **stale_terms_guard**: Nombres exactos como en architecture.md
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 009
- **id**: `TASK-009`
- **title**: Crear puertos de entrada (Inbound Ports / Use Cases)
- **agent**: executor
- **spec_refs**: architecture.md (Paquetes Propuestos - port/in)
- **goal**: Crear interfaces para casos de uso
- **scope**: 
  - ListTicketsUseCase.kt: execute(userId, filters, pagination)
  - GetTicketUseCase.kt: execute(ticketId, userId)
  - PurchaseTicketUseCase.kt: execute(ticketTypeId, quantity, userId)
  - TransferTicketUseCase.kt: execute(ticketId, recipientEmail, userId)
  - ValidateTicketUseCase.kt: execute(ticketId or qrCode, validatorUserId)
  - RevokeTicketUseCase.kt: execute(ticketId, adminUserId)
  - ListEventTicketsUseCase.kt: execute(eventId, filters, pagination)
- **out_of_scope**: Implementaciones de los casos de uso
- **inputs**: 
  - architecture.md línea 135-140: lista de puertos de entrada
  - OpenAPI.yaml: endpoints que mapean a cada use case
- **implementation_notes**: 
  - Usar interfaces de Kotlin
  - Cada use case debe tener un método execute() claro
  - Usar data classes para parámetros complejos
- **edge_cases**: 
  - Algunos use cases pueden tener múltiples métodos (scan vs validate by ID)
- **done_criteria**:
  - Todas las interfaces creadas
  - Compilación exitosa
- **verification**: 
  - `./gradlew compileKotlin` sin errores
- **dependencies**: TASK-006, TASK-007, TASK-008
- **handoff_context**: Implementados por servicios de aplicación
- **source_of_truth**: architecture.md línea 135
- **stale_terms_guard**: Nombres exactos como en architecture.md
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

### Fase 5: Adaptadores de Salida

#### Task 010
- **id**: `TASK-010`
- **title**: Crear entidades JPA y repositories Spring Data
- **agent**: executor
- **spec_refs**: architecture.md (Paquetes Propuestos - adapter/out/persistence), README.md (Modelo de Datos)
- **goal**: Crear entidades JPA para Ticket y TicketType con sus repositories
- **scope**: 
  - TicketEntity.kt: entidad JPA con @Entity, @Table, columnas, relaciones
  - TicketTypeEntity.kt: entidad JPA para tipo de boleta
  - TicketRepository.kt: interface Spring Data JPA
  - TicketTypeRepository.kt: interface Spring Data JPA
  - Mappers: TicketEntity.toDomain(), Ticket.toEntity()
- **out_of_scope**: Migraciones Flyway (otra tarea)
- **inputs**: 
  - README.md línea 154-171: esquema de boleta
  - architecture.md línea 117-119: repositories
- **implementation_notes**: 
  - Usar anotaciones JPA: @Entity, @Table, @Id, @GeneratedValue, @Column, @Enumerated, @ManyToOne, etc.
  - TicketEntity debe tener relación @ManyToOne con EventEntity (o @Embeddable para EventSummary)
  - Usar @Enumerated(EnumType.STRING) para TicketStatus
  - Incluir @Version para optimistic locking
  - Crear índices en owner_id, event_id, qr_code, status
  - Mappers separados para no mezclar JPA con dominio
- **edge_cases**: 
  - Manejo de nullable fields
  - Timestamps con @CreatedDate, @LastModifiedDate
- **done_criteria**:
  - Entidades creadas con anotaciones correctas
  - Repositories con métodos query derivados
  - Mappers implementados
  - Tests de repository exitosos
- **verification**: 
  - `./gradlew test` con tests de repository
  - Flyway migrate exitoso
- **dependencies**: TASK-003, TASK-004, TASK-005, TASK-006, TASK-008
- **handoff_context**: Implementa TicketRepositoryPort y TicketTypeRepositoryPort
- **source_of_truth**: README.md línea 154
- **stale_terms_guard**: No usar @Data de Lombok, usar data class con @Entity
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 011
- **id**: `TASK-011`
- **title**: Crear adaptador BlockchainClient
- **agent**: executor
- **spec_refs**: architecture.md (Paquetes Propuestos - adapter/out/blockchain)
- **goal**: Crear cliente REST para comunicación con Blockchain Service
- **scope**: 
  - BlockchainClient.kt: interface con anotaciones @HttpExchange o WebClient
  - Implementación de BlockchainPort
  - DTOs para requests/responses del servicio blockchain
- **out_of_scope**: Lógica de reintento (eso va en service)
- **inputs**: 
  - architecture.md línea 121-123: BlockchainClient
  - .env.example: BLOCKCHAIN_SERVICE_URL, timeout
- **implementation_notes**: 
  - Usar WebClient de Spring 5 o @HttpExchange de Spring 6
  - Incluir manejo de timeouts
  - Configurar circuit breaker si está disponible
- **edge_cases**: 
  - Servicio blockchain no disponible
  - Timeouts en registro de tokens
- **done_criteria**:
  - Cliente configurado con WebClient
  - Implementa BlockchainPort
  - Tests de integración mockeados
- **verification**: 
  - `./gradlew test` sin errores
- **dependencies**: TASK-008, TASK-002
- **handoff_context**: Usado por TicketPurchaseService y TicketTransferService
- **source_of_truth**: architecture.md línea 121
- **stale_terms_guard**: No usar RestTemplate (obsoleto)
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 012
- **id**: `TASK-012`
- **title**: Crear adaptador PaymentClient
- **agent**: executor
- **spec_refs**: architecture.md (Paquetes Propuestos - adapter/out/payment)
- **goal**: Crear cliente REST para comunicación con Payment Service
- **scope**: 
  - PaymentClient.kt: interface para crear orden de pago
  - Implementación de PaymentPort
  - DTOs para PaymentConfirmationWebhookRequest
- **out_of_scope**: Webhook (otra tarea)
- **inputs**: 
  - architecture.md línea 125-127: PaymentClient
  - .env.example: PAYMENT_SERVICE_URL, timeout
  - OpenAPI.yaml: PaymentConfirmationWebhookRequest schema
- **implementation_notes**: 
  - Usar WebClient
  - Método createOrder() retorna payment_preference_id
  - Configurar timeouts
- **edge_cases**: 
  - Servicio de pagos no disponible
  - Timeout en creación de orden
- **done_criteria**:
  - Cliente configurado
  - Implementa PaymentPort
  - Tests mockeados
- **verification**: 
  - `./gradlew test` sin errores
- **dependencies**: TASK-008, TASK-002
- **handoff_context**: Usado por TicketPurchaseService
- **source_of_truth**: architecture.md línea 125
- **stale_terms_guard**: No usar RestTemplate
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 013
- **id**: `TASK-013`
- **title**: Crear adaptador EventClient
- **agent**: executor
- **spec_refs**: architecture.md (Paquetes Propuestos - adapter/out/event)
- **goal**: Crear cliente REST para comunicación con Event Service
- **scope**: 
  - EventClient.kt: interface para obtener eventos y ticket types
  - Implementación de EventPort
  - Métodos: getEventById(eventId), getTicketTypes(eventId)
- **out_of_scope**: Cache (opcional, otra tarea)
- **inputs**: 
  - architecture.md línea 129-131: EventClient
  - .env.example: EVENT_SERVICE_URL, timeout
- **implementation_notes**: 
  - Usar WebClient
  - Incluir manejo de 404 (evento no encontrado)
  - Configurar timeouts
- **edge_cases**: 
  - Evento no encontrado (404)
  - Servicio no disponible
- **done_criteria**:
  - Cliente configurado
  - Implementa EventPort
  - Tests mockeados
- **verification**: 
  - `./gradlew test` sin errores
- **dependencies**: TASK-008, TASK-002, TASK-004, TASK-005
- **handoff_context**: Usado por TicketTypeController y TicketPurchaseService
- **source_of_truth**: architecture.md línea 129
- **stale_terms_guard**: No usar RestTemplate
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 014
- **id**: `TASK-014`
- **title**: Crear adaptador UserClient
- **agent**: executor
- **spec_refs**: architecture.md (Paquetes Propuestos - adapter/out/user)
- **goal**: Crear cliente REST para comunicación con User Service
- **scope**: 
  - UserClient.kt: interface para obtener usuario por email
  - Implementación de UserPort
  - Método: getUserByEmail(email) retorna userId
- **out_of_scope**: Cache de usuarios
- **inputs**: 
  - architecture.md línea 133-135: UserClient, línea 145: UserPort
  - .env.example: no hay variable específica, usar configuración general
- **implementation_notes**: 
  - Usar WebClient
  - Incluir manejo de 404 (usuario no encontrado)
  - Necesario para transferencia de tickets
- **edge_cases**: 
  - Usuario no encontrado (404)
  - Email inválido
- **done_criteria**:
  - Cliente configurado
  - Implementa UserPort
  - Tests mockeados
- **verification**: 
  - `./gradlew test` sin errores
- **dependencies**: TASK-008, TASK-002
- **handoff_context**: Usado por TicketTransferService
- **source_of_truth**: architecture.md línea 133
- **stale_terms_guard**: No usar RestTemplate
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

### Fase 6: Servicios de Aplicación

#### Task 015
- **id**: `TASK-015`
- **title**: Crear TicketDomainService
- **agent**: executor
- **spec_refs**: architecture.md (Paquetes Propuestos - domain/service)
- **goal**: Crear servicio de dominio con lógica pura de negocio
- **scope**: 
  - TicketDomainService.kt: lógica de validación de transiciones de estado
  - Métodos: canTransfer(ticket), canValidate(ticket), canRevoke(ticket)
  - Reglas de negocio puras sin dependencias externas
- **out_of_scope**: Persistencia, llamadas a servicios externos
- **inputs**: 
  - architecture.md línea 153-155: TicketDomainService
  - README.md línea 140-152: diagrama de estados
- **implementation_notes**: 
  - Clase pura sin anotaciones Spring (o @Service si es necesario)
  - Solo depende de modelos de dominio
  - No lanza excepciones de infraestructura
- **edge_cases**: 
  - Todas las combinaciones de estado deben estar cubiertas
- **done_criteria**:
  - Servicio creado con todas las reglas de negocio
  - Tests unitarios con 100% de cobertura
- **verification**: 
  - `./gradlew test` con cobertura > 90%
- **dependencies**: TASK-006, TASK-007
- **handoff_context**: Usado por servicios de aplicación
- **source_of_truth**: architecture.md línea 153
- **stale_terms_guard**: No mezclar con servicios de aplicación
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 016
- **id**: `TASK-016`
- **title**: Crear TicketService (List/Get tickets)
- **agent**: executor
- **spec_refs**: architecture.md (Paquetes Propuestos - application/service), OpenAPI (endpoints GET /tickets)
- **goal**: Implementar casos de uso para listar y obtener tickets
- **scope**: 
  - TicketService.kt: implementa ListTicketsUseCase y GetTicketUseCase
  - Métodos: listMyTickets(userId, filters, page, size), getTicketById(ticketId, userId)
  - Validación de propiedad (owner o ADMIN)
- **out_of_scope**: Controladores REST
- **inputs**: 
  - architecture.md línea 127: TicketService
  - OpenAPI.yaml: GET /tickets, GET /tickets/{ticketId}
- **implementation_notes**: 
  - Usar @Service de Spring
  - Inyectar TicketRepositoryPort
  - Validar permisos (owner o ADMIN)
  - Paginación con Spring Data Page
- **edge_cases**: 
  - Ticket no encontrado (404)
  - Usuario no es propietario ni ADMIN (403)
  - Filtros por status y event_id
- **done_criteria**:
  - Servicio implementado
  - Tests unitarios y de integración
  - Cobertura > 80%
- **verification**: 
  - `./gradlew test` exitoso
- **dependencies**: TASK-008, TASK-009, TASK-010, TASK-015
- **handoff_context**: Usado por TicketController
- **source_of_truth**: OpenAPI.yaml GET /tickets
- **stale_terms_guard**: No acceder a repositories directamente desde controllers
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 017
- **id**: `TASK-017`
- **title**: Crear TicketPurchaseService
- **agent**: executor
- **spec_refs**: architecture.md (Paquetes Propuestos - application/service), OpenAPI (POST /ticket-types/{id}/purchase)
- **goal**: Implementar caso de uso de compra de boletas
- **scope**: 
  - TicketPurchaseService.kt: implementa PurchaseTicketUseCase
  - Método: purchaseTicket(ticketTypeId, quantity, userId)
  - Flujo: verificar disponibilidad → reservar → crear orden pago → pre-registrar blockchain
  - Retorna TicketPurchaseResponse con payment_preference_id
- **out_of_scope**: Webhook de confirmación (otra tarea)
- **inputs**: 
  - architecture.md línea 128: TicketPurchaseService
  - OpenAPI.yaml: POST /ticket-types/{ticketTypeId}/purchase
  - README.md línea 170-195: flujo de compra
- **implementation_notes**: 
  - Usar @Service
  - Transaccional (@Transactional)
  - Verificar stock en TicketTypeRepository
  - Crear tickets en estado PENDING_PAYMENT
  - Llamar a PaymentPort.createOrder()
  - Llamar a BlockchainPort.preRegisterTokens()
  - Manejar concurrencia con optimistic locking
- **edge_cases**: 
  - Stock insuficiente (409)
  - Timeout en payment service
  - Timeout en blockchain service
  - Rollback si falla alguna llamada externa
- **done_criteria**:
  - Servicio implementado con flujo completo
  - Tests unitarios y de integración
  - Manejo de transacciones correcto
- **verification**: 
  - `./gradlew test` exitoso
  - Test de integración verifica transaccionalidad
- **dependencies**: TASK-008, TASK-009, TASK-010, TASK-011, TASK-012, TASK-013, TASK-015
- **handoff_context**: Usado por TicketTypeController
- **source_of_truth**: OpenAPI.yaml POST /ticket-types/{id}/purchase
- **stale_terms_guard**: No crear tickets ACTIVE hasta confirmación del webhook
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 018
- **id**: `TASK-018`
- **title**: Crear TicketTransferService
- **agent**: executor
- **spec_refs**: architecture.md (Paquetes Propuestos - application/service), OpenAPI (POST /tickets/{id}/transfer), architecture.md (Flujo de Transferencia)
- **goal**: Implementar caso de uso de transferencia de boletas
- **scope**: 
  - TicketTransferService.kt: implementa TransferTicketUseCase
  - Método: transferTicket(ticketId, recipientEmail, currentUserId)
  - Flujo: verificar propiedad → buscar recipient → actualizar ticket original a TRANSFERRED → crear nuevo ticket ACTIVE para recipient → transferir blockchain
  - Retorna TicketResponse del NUEVO ticket (ACTIVE, del recipient)
- **out_of_scope**: Notificaciones al usuario
- **inputs**: 
  - architecture.md línea 129: TicketTransferService
  - OpenAPI.yaml: POST /tickets/{ticketId}/transfer
  - architecture.md línea 245-256: secuencia de transferencia
  - README.md línea 38: "ticket original → TRANSFERRED, nuevo ticket para recipient → ACTIVE"
- **implementation_notes**: 
  - Usar @Service
  - @Transactional
  - Verificar estado actual (solo ACTIVE o TRANSFERRED son transferibles)
  - Llamar a UserPort.getUserByEmail() para obtener recipient_id
  - Actualizar ticket original a TRANSFERRED
  - Crear NUEVO ticket con owner=recipient, status=ACTIVE
  - Llamar a BlockchainPort.transferToken()
  - Retornar el NUEVO ticket, no el original
- **edge_cases**: 
  - Ticket no transferible (VALIDATED, REVOKED, EXPIRED, PENDING_PAYMENT) → 409
  - Usuario no es propietario → 403
  - Recipient no encontrado → 404
  - Mismo owner que recipient → 400
  - Blockchain falla → rollback
- **done_criteria**:
  - Servicio implementado con flujo exacto
  - Tests unitarios y de integración
  - Verifica que retorna NUEVO ticket ACTIVE
- **verification**: 
  - `./gradlew test` exitoso
  - Test verifica que ticket original queda TRANSFERRED
  - Test verifica que nuevo ticket es ACTIVE con nuevo owner
- **dependencies**: TASK-008, TASK-009, TASK-010, TASK-011, TASK-014, TASK-015
- **handoff_context**: Usado por TicketController
- **source_of_truth**: OpenAPI.yaml POST /tickets/{id}/transfer, README.md línea 38
- **stale_terms_guard**: Retornar NUEVO ticket, no el original transferido
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 019
- **id**: `TASK-019`
- **title**: Crear TicketValidationService
- **agent**: executor
- **spec_refs**: architecture.md (Paquetes Propuestos - application/service), OpenAPI (POST /validation/scan, POST /validation/{id})
- **goal**: Implementar caso de uso de validación de boletas
- **scope**: 
  - TicketValidationService.kt: implementa ValidateTicketUseCase
  - Métodos: scanTicket(qrCode, validatorUserId), validateTicket(ticketId, validatorUserId)
  - Flujo: buscar ticket → verificar estado ACTIVE → actualizar a VALIDATED
  - Retorna TicketValidationResponse con valid=true y ticket actualizado
- **out_of_scope**: Generación de QR (otra tarea)
- **inputs**: 
  - architecture.md línea 130: TicketValidationService
  - OpenAPI.yaml: POST /validation/scan, POST /validation/{ticketId}
  - architecture.md línea 203-220: secuencia de validación
- **implementation_notes**: 
  - Usar @Service
  - @Transactional
  - scanTicket() busca por qr_code
  - validateTicket() busca por ticketId
  - Verificar estado = ACTIVE (si no, 409)
  - Actualizar a VALIDATED con validated_at y validated_by
  - Retornar TicketValidationResponse
- **edge_cases**: 
  - QR inválido → 400
  - Ticket no encontrado → 404
  - Ticket ya validado → 409
  - Ticket revocado → 409
  - Usuario sin rol ADMIN/ORGANIZER → 403 (se maneja en controller)
- **done_criteria**:
  - Servicio implementado
  - Tests unitarios y de integración
  - Cobertura > 80%
- **verification**: 
  - `./gradlew test` exitoso
- **dependencies**: TASK-008, TASK-009, TASK-010, TASK-015
- **handoff_context**: Usado por ValidationController
- **source_of_truth**: OpenAPI.yaml POST /validation/scan
- **stale_terms_guard**: No validar tickets que no estén ACTIVE
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 020
- **id**: `TASK-020`
- **title**: Crear TicketRevokeService
- **agent**: executor
- **spec_refs**: architecture.md (Paquetes Propuestos - application/service), OpenAPI (POST /tickets/{id}/revoke)
- **goal**: Implementar caso de uso de revocación de boletas
- **scope**: 
  - TicketRevokeService.kt: implementa RevokeTicketUseCase
  - Método: revokeTicket(ticketId, adminUserId)
  - Flujo: verificar ticket → actualizar a REVOKED
  - Retorna TicketResponse actualizado
- **out_of_scope**: Procesamiento de reembolsos (eso lo hace Payment Service)
- **inputs**: 
  - OpenAPI.yaml: POST /tickets/{ticketId}/revoke
- **implementation_notes**: 
  - Usar @Service
  - @Transactional
  - Verificar estado actual (no permitir si ya está REVOKED o VALIDATED)
  - Actualizar a REVOKED
  - No es necesario llamar a blockchain (el token queda revocado en el servicio)
- **edge_cases**: 
  - Ticket no encontrado → 404
  - Ticket ya revocado → 409
  - Ticket ya validado → 409
  - Usuario sin rol ADMIN/ORGANIZER → 403 (se maneja en controller)
- **done_criteria**:
  - Servicio implementado
  - Tests unitarios
- **verification**: 
  - `./gradlew test` exitoso
- **dependencies**: TASK-008, TASK-009, TASK-010, TASK-015
- **handoff_context**: Usado por TicketController
- **source_of_truth**: OpenAPI.yaml POST /tickets/{id}/revoke
- **stale_terms_guard**: No llamar a Payment Service para reembolso
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 021
- **id**: `TASK-021`
- **title**: Crear PaymentWebhookService
- **agent**: executor
- **spec_refs**: architecture.md (Flujo de Compra), OpenAPI (POST /webhooks/payment/confirmation)
- **goal**: Implementar servicio para webhook de confirmación de pago
- **scope**: 
  - PaymentWebhookService.kt: procesa confirmación de pago
  - Método: processPaymentConfirmation(webhookRequest)
  - Flujo: verificar firma → buscar orden → verificar idempotencia → transicionar tickets a ACTIVE
  - Retorna PaymentConfirmationResponse con accepted y tickets_activated
- **out_of_scope**: Verificación de firma HMAC (se maneja en filter/adapter)
- **inputs**: 
  - OpenAPI.yaml: POST /webhooks/payment/confirmation, PaymentConfirmationWebhookRequest schema
  - README.md línea 41: "Webhook payment: POST /webhooks/payment/confirmation con firma HMAC-SHA256, idempotente por payment_id"
- **implementation_notes**: 
  - Usar @Service
  - @Transactional
  - Verificar idempotencia por payment_id (guardar payment_id procesado)
  - Buscar tickets por order_id en estado PENDING_PAYMENT
  - Transicionar a ACTIVE
  - Retornar accepted=true si procesado, false si ya existía (idempotencia)
- **edge_cases**: 
  - payment_id ya procesado → accepted=false (idempotencia)
  - order_id no encontrado → 404
  - status=REJECTED → transicionar a EXPIRED
  - Firma inválida → 401 (se maneja antes)
- **done_criteria**:
  - Servicio implementado
  - Tests de idempotencia
  - Tests de transición de estado
- **verification**: 
  - `./gradlew test` exitoso
  - Test verifica idempotencia
- **dependencies**: TASK-008, TASK-009, TASK-010, TASK-015
- **handoff_context**: Usado por WebhookController
- **source_of_truth**: OpenAPI.yaml POST /webhooks/payment/confirmation
- **stale_terms_guard**: Idempotencia por payment_id, no por order_id
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

### Fase 7: Controladores REST

#### Task 022
- **id**: `TASK-022`
- **title**: Crear TicketController (endpoints de cliente)
- **agent**: executor
- **spec_refs**: OpenAPI (GET /tickets, GET /tickets/{id}, GET /tickets/{id}/qr, POST /tickets/{id}/transfer), architecture.md (Paquetes Propuestos - adapter/in/rest)
- **goal**: Implementar controlador REST para endpoints de cliente
- **scope**: 
  - TicketController.kt: @RestController
  - Endpoints: GET /tickets, GET /tickets/{ticketId}, GET /tickets/{ticketId}/qr, POST /tickets/{ticketId}/transfer
  - Mapeo de DTOs request/response
  - Manejo de autenticación (obtener userId del JWT)
- **out_of_scope**: Lógica de negocio (eso va en servicios)
- **inputs**: 
  - OpenAPI.yaml: endpoints de Customer Tickets
  - architecture.md línea 111-114: TicketController
- **implementation_notes**: 
  - Usar @RestController, @RequestMapping("/tickets")
  - Obtener userId de SecurityContextHolder (JWT)
  - Usar ResponseEntity para status codes y headers
  - Mapear request DTOs a parámetros de servicio
  - Mapear response de servicio a response DTOs
- **edge_cases**: 
  - 401 si no autenticado
  - 403 si no es propietario
  - 404 si no encontrado
  - 409 en transfer si no transferible
- **done_criteria**:
  - Controlador implementado
  - Tests de controller con MockMvc
  - OpenAPI generado coincide con spec
- **verification**: 
  - `./gradlew test` exitoso
  - `curl` a endpoints retorna respuestas correctas
- **dependencies**: TASK-009, TASK-016, TASK-018
- **handoff_context**: Expone casos de uso como endpoints REST
- **source_of_truth**: OpenAPI.yaml Customer Tickets
- **stale_terms_guard**: No incluir lógica de negocio en controller
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 023
- **id**: `TASK-023`
- **title**: Crear TicketTypeController (endpoints públicos y compra)
- **agent**: executor
- **spec_refs**: OpenAPI (GET /ticket-types, POST /ticket-types/{id}/purchase)
- **goal**: Implementar controlador REST para tipos de boleta
- **scope**: 
  - TicketTypeController.kt: @RestController
  - Endpoints: GET /ticket-types (público), POST /ticket-types/{ticketTypeId}/purchase (autenticado)
  - GET /ticket-types requiere event_id query param
  - POST purchase retorna 201 con Location header
- **out_of_scope**: Lógica de negocio
- **inputs**: 
  - OpenAPI.yaml: Public Tickets y Customer Tickets (purchase)
- **implementation_notes**: 
  - GET /ticket-types sin @PreAuthorize (público)
  - POST purchase con @PreAuthorize("isAuthenticated()")
  - Retornar 201 Created con header Location
  - Validar event_id presente (400 si falta)
- **edge_cases**: 
  - event_id faltante → 400
  - Evento no encontrado → 404
  - Stock insuficiente → 409
  - Cantidad inválida → 400
- **done_criteria**:
  - Controlador implementado
  - Tests con MockMvc
  - Location header correcto en 201
- **verification**: 
  - `./gradlew test` exitoso
  - Test verifica header Location en 201
- **dependencies**: TASK-009, TASK-013, TASK-017
- **handoff_context**: Expone endpoints públicos y de compra
- **source_of_truth**: OpenAPI.yaml GET /ticket-types, POST /ticket-types/{id}/purchase
- **stale_terms_guard**: GET /ticket-types es PÚBLICO, no requiere auth
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 024
- **id**: `TASK-024`
- **title**: Crear ValidationController (validación de boletas)
- **agent**: executor
- **spec_refs**: OpenAPI (POST /validation/scan, POST /validation/{id})
- **goal**: Implementar controlador REST para validación
- **scope**: 
  - ValidationController.kt: @RestController
  - Endpoints: POST /validation/scan, POST /validation/{ticketId}
  - Requiere rol ADMIN u ORGANIZER
- **out_of_scope**: Lógica de validación
- **inputs**: 
  - OpenAPI.yaml: Validation endpoints
  - architecture.md línea 113: ValidationController
- **implementation_notes**: 
  - Usar @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
  - @RequestMapping("/validation")
  - scanTicket() recibe TicketScanRequest
  - validateTicket() recibe ticketId de path
- **edge_cases**: 
  - Rol insuficiente → 403
  - QR inválido → 400
  - Ticket no encontrado → 404
  - Ticket ya validado → 409
- **done_criteria**:
  - Controlador implementado
  - Tests con MockMvc
  - Verificación de roles correcta
- **verification**: 
  - `./gradlew test` exitoso
- **dependencies**: TASK-009, TASK-019
- **handoff_context:** Expone endpoints de validación
- **source_of_truth**: OpenAPI.yaml Validation
- **stale_terms_guard**: Requiere ADMIN u ORGANIZER, no CUSTOMER
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 025
- **id**: `TASK-025`
- **title**: Crear AdminTicketController (endpoints administrativos)
- **agent**: executor
- **spec_refs**: OpenAPI (POST /tickets/{id}/revoke, GET /tickets/events/{eventId})
- **goal**: Implementar controlador REST para endpoints administrativos
- **scope**: 
  - AdminTicketController.kt: @RestController
  - Endpoints: POST /tickets/{ticketId}/revoke, GET /tickets/events/{eventId}
  - Requiere rol ADMIN u ORGANIZER
- **out_of_scope**: Lógica de revocación
- **inputs**: 
  - OpenAPI.yaml: Admin Tickets endpoints
- **implementation_notes**: 
  - Usar @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
  - revokeTicket() retorna 200 con TicketResponse
  - listEventTickets() retorna Page<TicketResponse>
- **edge_cases**: 
  - Rol insuficiente → 403
  - Ticket no encontrado → 404
  - Ticket ya revocado → 409
  - Evento no encontrado → 404
- **done_criteria**:
  - Controlador implementado
  - Tests con MockMvc
- **verification**: 
  - `./gradlew test` exitoso
- **dependencies**: TASK-009, TASK-016, TASK-020
- **handoff_context**: Expone endpoints administrativos
- **source_of_truth**: OpenAPI.yaml Admin Tickets
- **stale_terms_guard**: Requiere ADMIN u ORGANIZER
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 026
- **id**: `TASK-026`
- **title**: Crear WebhookController (payment confirmation)
- **agent**: executor
- **spec_refs**: OpenAPI (POST /webhooks/payment/confirmation)
- **goal**: Implementar controlador REST para webhook de pago
- **scope**: 
  - WebhookController.kt: @RestController
  - Endpoint: POST /webhooks/payment/confirmation
  - Verificación de firma HMAC-SHA256
  - Idempotencia por payment_id
- **out_of_scope**: Lógica de procesamiento (eso va en servicio)
- **inputs**: 
  - OpenAPI.yaml: POST /webhooks/payment/confirmation
  - README.md línea 41: firma HMAC-SHA256
- **implementation_notes**: 
  - @RequestMapping("/webhooks")
  - Verificar firma en header X-Webhook-Signature
  - Usar @PreAuthorize("permitAll()") (la seguridad es por firma, no JWT)
  - Retornar PaymentConfirmationResponse
- **edge_cases**: 
  - Firma inválida → 401
  - Payload inválido → 400
  - Orden no encontrada → 404
- **done_criteria**:
  - Controlador implementado
  - Verificación de firma funcional
  - Tests con MockMvc
- **verification**: 
  - `./gradlew test` exitoso
  - Test verifica firma HMAC
- **dependencies**: TASK-009, TASK-021
- **handoff_context**: Expone webhook para Payment Service
- **source_of_truth**: OpenAPI.yaml POST /webhooks/payment/confirmation
- **stale_terms_guard**: No usar JWT, usar firma HMAC
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

### Fase 8: Seguridad y Configuración

#### Task 027
- **id**: `TASK-027`
- **title**: Configurar SecurityConfig con OAuth2/OIDC
- **agent**: executor
- **spec_refs**: architecture.md (Paquetes Propuestos - config/SecurityConfig), README.md (Seguridad)
- **goal**: Configurar seguridad con Keycloak OAuth2/OIDC
- **scope**: 
  - SecurityConfig.kt: @Configuration con SecurityFilterChain
  - Configurar OAuth2 Resource Server con JWT
  - Configurar conversor de roles desde claim de Keycloak
  - Configurar CORS si es necesario
  - Excluir endpoints públicos y webhooks de autenticación JWT
- **out_of_scope**: Configuración de Keycloak realm (otra tarea)
- **inputs**: 
  - architecture.md línea 160: SecurityConfig
  - README.md línea 276-296: roles y permisos
  - .env.example: KEYCLOAK_ISSUER_URI, client-id, roles-claim
- **implementation_notes**: 
  - Usar oauth2ResourceServer { jwt { } }
  - Configurar JwtAuthenticationConverter para extraer roles
  - Claim de roles: resource_access.ms-ticket-ticket.roles
  - Excluir: GET /ticket-types, POST /webhooks/**, actuator/**
  - Configurar method security con @PreAuthorize
- **edge_cases**: 
  - Token sin roles
  - Token expirado
  - Issuer no coincide
- **done_criteria**:
  - SecurityConfig implementado
  - JWT validado correctamente
  - Roles extraídos del claim correcto
  - Endpoints públicos accesibles sin auth
- **verification**: 
  - `./gradlew test` con tests de seguridad
  - curl sin token a endpoint protegido → 401
  - curl con token válido → 200
- **dependencies**: TASK-001, TASK-002
- **handoff_context**: Habilita autenticación para todos los controllers
- **source_of_truth**: README.md línea 276
- **stale_terms_guard**: Usar Spring Security 6.x (SecurityFilterChain, no WebSecurityConfigurerAdapter)
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 028
- **id**: `TASK-028`
- **title**: Crear WebhookSignatureFilter para HMAC-SHA256
- **agent**: executor
- **spec_refs**: OpenAPI (WebhookSignature security scheme), README.md (Webhook payment)
- **goal**: Implementar filtro para verificar firma HMAC de webhooks
- **scope**: 
  - WebhookSignatureFilter.kt: OncePerRequestFilter
  - Verificar header X-Webhook-Signature
  - Calcular HMAC-SHA256 del payload con secret compartido
  - Comparar firmas en tiempo constante
- **out_of_scope**: Configuración del secret (viene de .env)
- **inputs**: 
  - OpenAPI.yaml línea 639-642: WebhookSignature security scheme
  - README.md línea 41: firma HMAC-SHA256
  - .env.example: QR_CODE_SECRET (usar similar para webhook)
- **implementation_notes**: 
  - Extender OncePerRequestFilter
  - Aplicar solo a POST /webhooks/**
  - Leer payload completo antes de verificar firma
  - Usar MessageDigest.isEqual() para comparación en tiempo constante
  - Si falla, lanzar AuthenticationException
- **edge_cases**: 
  - Header faltante → 401
  - Firma inválida → 401
  - Payload modificado → 401
- **done_criteria**:
  - Filtro implementado
  - Tests de firma válida e inválida
  - Integrado en SecurityConfig
- **verification**: 
  - `./gradlew test` exitoso
  - Test de integración verifica webhook con firma correcta
- **dependencies**: TASK-027
- **handoff_context**: Usado por WebhookController
- **source_of_truth**: OpenAPI.yaml línea 639
- **stale_terms_guard**: HMAC-SHA256, no SHA256 simple
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 029
- **id**: `TASK-029`
- **title**: Configurar OpenApiConfig para Swagger UI
- **agent**: executor
- **spec_refs**: architecture.md (Paquetes Propuestos - config/OpenApiConfig)
- **goal**: Configurar OpenAPI/Swagger UI para documentación
- **scope**: 
  - OpenApiConfig.kt: @Configuration con OpenAPI bean
  - Configurar info, servers, security schemes
  - Importar spec desde openapi.yaml o generar desde código
- **out_of_scope**: Generación de código desde OpenAPI
- **inputs**: 
  - architecture.md línea 161: OpenApiConfig
  - docs/api/openapi.yaml: spec completa
- **implementation_notes**: 
  - Usar springdoc-openapi para Spring Boot 3
  - Configurar BearerJWT security scheme
  - Configurar server URL: /ms-ticket-ticket/v1
- **edge_cases**: 
  - Swagger UI accesible en desarrollo, no en prod
- **done_criteria**:
  - Swagger UI accesible en /swagger-ui.html
  - API docs muestran todos los endpoints
- **verification**: 
  - curl http://localhost:8082/swagger-ui.html → 200
- **dependencies**: TASK-001, TASK-002
- **handoff_context**: Documentación de API
- **source_of_truth**: architecture.md línea 161
- **stale_terms_guard**: Usar springdoc-openapi, no springfox
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 030
- **id**: `TASK-030`
- **title**: Crear GlobalExceptionHandler con ApiErrorResponse
- **agent**: executor
- **spec_refs**: OpenAPI (ApiErrorResponse schema), README.md (Error response), architecture.md (adapter/out/ERROR_HANDLER)
- **goal**: Implementar manejador global de excepciones con formato estandarizado
- **scope**: 
  - GlobalExceptionHandler.kt: @RestControllerAdvice
  - ApiErrorResponse data class: timestamp, status, error, code, message, path, trace_id, details
  - Manejar: TicketNotFoundException, TicketNotOwnedByUserException, etc.
  - Campo `error` en SCREAMING_SNAKE_CASE
- **out_of_scope**: Excepciones de infraestructura (DB, red)
- **inputs**: 
  - OpenAPI.yaml línea 836-867: ApiErrorResponse schema
  - README.md línea 40: "Error response: campo `error` en SCREAMING_SNAKE_CASE"
  - architecture.md línea 77: ERROR_HANDLER
- **implementation_notes**: 
  - Usar @ExceptionHandler para cada excepción de dominio
  - Mapear a status HTTP correcto (404, 403, 409, 400)
  - Campo `error`: BAD_REQUEST, NOT_FOUND, FORBIDDEN, CONFLICT, etc.
  - Incluir trace_id para observabilidad (SLF4J MDC)
  - Details array para errores de validación
- **edge_cases**: 
  - Excepción desconocida → 500 INTERNAL_SERVER_ERROR
  - Validación de campos → 400 con details
- **done_criteria**:
  - Handler implementado
  - Todas las excepciones mapeadas
  - Formato exacto de ApiErrorResponse
  - Tests de cada tipo de error
- **verification**: 
  - `./gradlew test` exitoso
  - curl a endpoint con error retorna formato correcto
- **dependencies**: TASK-007, TASK-001
- **handoff_context**: Usado por todos los controllers
- **source_of_truth**: OpenAPI.yaml ApiErrorResponse, README.md línea 40
- **stale_terms_guard**: error en SCREAMING_SNAKE_CASE, no camelCase
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

### Fase 9: Migraciones Flyway

#### Task 031
- **id**: `TASK-031`
- **title**: Crear migraciones Flyway para esquema de base de datos
- **agent**: executor
- **spec_refs**: README.md (Modelo de Datos), architecture.md (Persistencia)
- **goal**: Crear scripts SQL de migración para tablas de tickets
- **scope**: 
  - V1__create_ticket_types_table.sql
  - V2__create_tickets_table.sql
  - V3__create_payment_orders_table.sql (para idempotencia de webhook)
  - Índices en owner_id, event_id, qr_code, status, order_id
- **out_of_scope**: Datos seed (otra tarea si se necesita)
- **inputs**: 
  - README.md línea 154-171: esquema de boleta
  - OpenAPI.yaml: schemas de Ticket y TicketType
- **implementation_notes**: 
  - Usar PostgreSQL 16+ syntax
  - TicketType: id, event_id, name, description, price, quantity, available_quantity, max_per_user, created_at
  - Ticket: id, event_id, event_name, event_start_date, venue_name, ticket_type_id, owner_id, owner_email, price, status, qr_code, blockchain_token_id, blockchain_tx_hash, seat_number, purchased_at, validated_at, created_at, updated_at
  - PaymentOrder: id (order_id), payment_id (para idempotencia), status, processed_at
  - Índices: idx_ticket_owner, idx_ticket_event, idx_ticket_qr, idx_ticket_status, idx_payment_order_payment_id
  - Usar @Version para optimistic locking (columna version)
- **edge_cases**: 
  - available_quantity con CHECK >= 0
  - status con CHECK en enum values
- **done_criteria**:
  - Migraciones creadas
  - Flyway migrate exitoso
  - Tablas creadas con índices
- **verification**: 
  - `./gradlew flywayMigrate` exitoso
  - psql \dt muestra tablas
  - psql \di muestra índices
- **dependencies**: TASK-001, TASK-002
- **handoff_context**: Requerido para repositories JPA
- **source_of_truth**: README.md línea 154
- **stale_terms_guard**: PostgreSQL 16+ syntax, no MySQL
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

### Fase 10: DTOs y Mappers

#### Task 032
- **id**: `TASK-032`
- **title**: Crear DTOs de request y response
- **agent**: executor
- **spec_refs**: OpenAPI (todos los schemas de request/response)
- **goal**: Crear data classes para DTOs de transporte
- **scope**: 
  - request/: TicketPurchaseRequest, TicketTransferRequest, TicketScanRequest, PaymentConfirmationWebhookRequest
  - response/: TicketResponse, TicketTypeResponse, TicketPurchaseResponse, TicketValidationResponse, TicketQRResponse, PaymentConfirmationResponse, PagedResponse
  - EventSummary DTO
- **out_of_scope**: Mapeo (otra tarea)
- **inputs**: 
  - OpenAPI.yaml: todos los schemas bajo components/schemas
- **implementation_notes**: 
  - Usar data classes de Kotlin
  - Validaciones con anotaciones javax.validation o jakarta.validation
  - @JsonProperty para nombres snake_case si es necesario
  - Separar DTOs de modelos de dominio
- **edge_cases**: 
  - Campos opcionales nullable
  - Fechas como Instant o LocalDateTime
- **done_criteria**:
  - Todos los DTOs creados
  - Validaciones anotadas
  - Compilación exitosa
- **verification**: 
  - `./gradlew compileKotlin` sin errores
- **dependencies**: TASK-001
- **handoff_context**: Usados por controllers y servicios
- **source_of_truth**: OpenAPI.yaml components/schemas
- **stale_terms_guard**: No usar modelos de dominio como DTOs
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 033
- **id**: `TASK-033`
- **title**: Crear mappers entre dominio y DTOs
- **agent**: executor
- **spec_refs**: architecture.md (Repository DTO Patterns)
- **goal**: Crear mappers para convertir entre entidades, dominio y DTOs
- **scope**: 
  - TicketMapper.kt: TicketEntity ↔ Ticket ↔ TicketResponse
  - TicketTypeMapper.kt: TicketTypeEntity ↔ TicketType ↔ TicketTypeResponse
  - Extension functions o mapper classes
- **out_of_scope**: Lógica de negocio
- **inputs**: 
  - OpenAPI.yaml: response schemas
  - TASK-006: Ticket domain model
  - TASK-010: TicketEntity JPA
- **implementation_notes**: 
  - Usar extension functions de Kotlin o mapper classes
  - No exponer entidades JPA fuera de adapters
  - No exponer modelos de dominio en controllers
- **edge_cases**: 
  - Campos nullables
  - Fechas en diferentes formatos
- **done_criteria**:
  - Mappers implementados
  - Tests de mapeo
- **verification**: 
  - `./gradlew test` exitoso
- **dependencies**: TASK-006, TASK-010, TASK-032
- **handoff_context**: Usado por servicios y controllers
- **source_of_truth**: OpenAPI.yaml schemas
- **stale_terms_guard**: Separar capas: Entity → Domain → DTO
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

### Fase 11: QR Code

#### Task 034
- **id**: `TASK-034`
- **title**: Implementar generador de QR codes
- **agent**: executor
- **spec_refs**: README.md (QR Code TTL), OpenAPI (GET /tickets/{id}/qr, TicketQRResponse)
- **goal**: Implementar servicio para generar QR codes con TTL
- **scope**: 
  - QrCodeService.kt: generar QR code string y URL de imagen
  - QR code contiene: ticketId, firma HMAC, expires_at
  - TTL de 5 minutos (configurable)
  - TicketQRResponse con qr_code, qr_image_url, expires_at
- **out_of_scope**: Generación de imagen QR (usar librería)
- **inputs**: 
  - README.md línea 39: "QR Code: short-lived (TTL 5 min)"
  - .env.example: QR_CODE_TTL_MINUTES=5, QR_CODE_SECRET
  - OpenAPI.yaml: GET /tickets/{ticketId}/qr, TicketQRResponse schema
- **implementation_notes**: 
  - Usar librería como ZXing o QRGen para generar imagen
  - Firmar QR con HMAC-SHA256 usando QR_CODE_SECRET
  - expires_at = now + TTL
  - QR code string formato: ticketId:signature:expiresAt
  - qr_image_url apunta a endpoint que genera imagen o servicio externo
- **edge_cases**: 
  - QR expirado no invalida el ticket, solo el QR
  - Regenerar QR si está expirado
- **done_criteria**:
  - Servicio implementado
  - QR generado con firma y TTL
  - Tests de generación y validación de QR
- **verification**: 
  - `./gradlew test` exitoso
  - QR code escaneable y verificable
- **dependencies**: TASK-006, TASK-010
- **handoff_context**: Usado por TicketController para GET /tickets/{id}/qr
- **source_of_truth**: README.md línea 39
- **stale_terms_guard**: expires_at es del QR, no del evento
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

### Fase 12: Tests

#### Task 035
- **id**: `TASK-035`
- **title**: Crear tests unitarios de dominio
- **agent**: executor
- **spec_refs**: testing-strategy skill, architecture.md (Modelo de Estados)
- **goal**: Crear tests unitarios para modelos y servicios de dominio
- **scope**: 
  - TicketTest.kt: tests de transiciones de estado
  - TicketDomainServiceTest.kt: tests de reglas de negocio
  - Cobertura > 90% en dominio
- **out_of_scope**: Tests de integración
- **inputs**: 
  - architecture.md línea 258-273: diagrama de estados
- **implementation_notes**: 
  - Usar JUnit 5 y MockK
  - Test cada transición válida
  - Test cada transición inválida (debe lanzar excepción)
  - No usar Spring context para tests de dominio puro
- **edge_cases**: 
  - Todas las combinaciones de estado
- **done_criteria**:
  - Tests creados
  - Cobertura > 90% en dominio
  - `./gradlew test` exitoso
- **verification**: 
  - `./gradlew test jacocoTestReport`
  - Reporte de cobertura muestra > 90% en dominio
- **dependencies**: TASK-006, TASK-007, TASK-015
- **handoff_context**: Evidencia de calidad
- **source_of_truth**: architecture.md diagrama de estados
- **stale_terms_guard**: No usar Spring en tests de dominio puro
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 036
- **id**: `TASK-036`
- **title**: Crear tests de integración de repositories
- **agent**: executor
- **spec_refs**: testing-strategy skill
- **goal**: Crear tests de integración para repositories JPA
- **scope**: 
  - TicketRepositoryIntegrationTest.kt
  - TicketTypeRepositoryIntegrationTest.kt
  - Usar Testcontainers con PostgreSQL
  - Verificar CRUD y queries especializadas
- **out_of_scope**: Tests de servicios
- **inputs**: 
  - TASK-010: Repositories JPA
- **implementation_notes**: 
  - Usar @SpringBootTest con Testcontainers
  - Flyway migrate automático en tests
  - Verificar índices y constraints
- **edge_cases**: 
  - Constraints de base de datos
  - Queries con filtros múltiples
- **done_criteria**:
  - Tests de integración creados
  - `./gradlew integrationTest` exitoso
- **verification**: 
  - `./gradlew integrationTest` exitoso
- **dependencies**: TASK-010, TASK-031
- **handoff_context**: Evidencia de persistencia correcta
- **source_of_truth**: TASK-010
- **stale_terms_guard**: Usar Testcontainers, no H2
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

#### Task 037
- **id**: `TASK-037`
- **title**: Crear tests de integración de controllers (MockMvc)
- **agent**: executor
- **spec_refs**: testing-strategy skill, OpenAPI (todos los endpoints)
- **goal**: Crear tests de integración para todos los endpoints REST
- **scope**: 
  - TicketControllerIntegrationTest.kt
  - TicketTypeControllerIntegrationTest.kt
  - ValidationControllerIntegrationTest.kt
  - AdminTicketControllerIntegrationTest.kt
  - WebhookControllerIntegrationTest.kt
  - Verificar status codes, response bodies, headers
- **out_of_scope**: Tests E2E con servicios reales
- **inputs**: 
  - OpenAPI.yaml: todos los endpoints
- **implementation_notes**: 
  - Usar @SpringBootTest con MockMvc
  - Mockear puertos de salida (BlockchainPort, PaymentPort, etc.)
  - Verificar OpenAPI generado coincide con spec
- **edge_cases**: 
  - Todos los status codes: 200, 201, 400, 401, 403, 404, 409, 500
  - Location header en 201
  - Error responses con formato ApiErrorResponse
- **done_criteria**:
  - Tests para todos los endpoints
  - `./gradlew test` exitoso
- **verification**: 
  - `./gradlew test` exitoso
  - Cobertura > 80% en controllers
- **dependencies**: TASK-022, TASK-023, TASK-024, TASK-025, TASK-026
- **handoff_context**: Evidencia de API correcta
- **source_of_truth**: OpenAPI.yaml
- **stale_terms_guard**: Mockear puertos de salida, no usar servicios reales
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

### Fase 13: Documentación

#### Task 038
- **id**: `TASK-038`
- **title**: Actualizar README con estado de implementación
- **agent**: executor
- **spec_refs**: documentation-standards skill, README.md
- **goal**: Actualizar README con estado post-implementación
- **scope**: 
  - Cambiar status de validated-not-executed a implemented
  - Agregar sección de estado de endpoints
  - Agregar badges de build, tests, coverage si aplica
- **out_of_scope**: Documentación de API (ya está en openapi.yaml)
- **inputs**: 
  - README.md actual
- **implementation_notes**: 
  - Mantener formato existente
  - Agregar sección de implementación completada
- **edge_cases**: 
  - Ninguno
- **done_criteria**:
  - README actualizado
  - Estado reflejado correctamente
- **verification**: 
  - README.md legible y actualizado
- **dependencies**: Todas las tareas de implementación completadas
- **handoff_context**: Documentación final
- **source_of_truth**: README.md
- **stale_terms_guard**: No cambiar estructura del README
- **status**: done
- **executor_notes**: 
- **verification_result**: 
- **blocker**: none

---

## Resumen de Tareas

| Fase | Tareas | Estado |
|------|--------|--------|
| 1. Configuración | 001, 002 | done |
| 2. Modelos de Dominio | 003, 004, 005, 006 | done |
| 3. Excepciones | 007 | done |
| 4. Puertos | 008, 009 | done |
| 5. Adaptadores de Salida | 010, 011, 012, 013, 014 | done |
| 6. Servicios de Aplicación | 015, 016, 017, 018, 019, 020, 021 | done |
| 7. Controladores REST | 022, 023, 024, 025, 026 | done |
| 8. Seguridad y Configuración | 027, 028, 029, 030 | done |
| 9. Migraciones Flyway | 031 | done |
| 10. DTOs y Mappers | 032, 033 | done |
| 11. QR Code | 034 | done |
| 12. Tests | 035, 036, 037 | done |
| 13. Documentación | 038 | done |

**Total**: 38 tareas

---

## Notas para Executor

1. **Orden de ejecución**: Seguir el orden numérico de tareas. Las dependencias están definidas en cada tarea.
2. **Verificación**: Cada tarea debe incluir verificación antes de marcar como done.
3. **Status updates**: Actualizar este archivo con `executor_notes`, `verification_result` y cambiar status a `in_progress`, `done`, o `blocked`.
4. **Blockers**: Si una tarea se bloquea, actualizar `blocker` con `blocked_reason`, `conflicting_artifacts`, `required_owner` y `next_required_decision`.
5. **No editar definiciones**: Solo Task Decomposer puede editar las definiciones de tareas. Executor solo actualiza status y notas.

---

## Decomposition Contract

Esta descomposición asume:
- Stack: Kotlin 2.2.21 + Spring Boot 4.0.6 + Gradle 8.14+
- Arquitectura: Hexagonal con separación clara de capas
- 11 endpoints OpenAPI implementados
- 6 estados de TicketStatus
- 3 roles: CUSTOMER, ADMIN, ORGANIZER
- Webhook idempotente por payment_id
- QR con TTL 5 minutos
- Error responses con campo `error` en SCREAMING_SNAKE_CASE
- PostgreSQL 16+ con Flyway
