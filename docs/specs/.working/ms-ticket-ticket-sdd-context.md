# SDD Context - CriptoPass MS Ticket Ticket

## Current status
validated-not-executed

## Canonical artifacts
- README: `/home/cristiansrc/Documentos/Proyectos/criptopass-workspace/projects/criptopass-ms-ticket-ticket/README.md`
- Architecture: `/home/cristiansrc/Documentos/Proyectos/criptopass-workspace/projects/criptopass-ms-ticket-ticket/docs/architecture.md`
- Deployment: `/home/cristiansrc/Documentos/Proyectos/criptopass-workspace/projects/criptopass-ms-ticket-ticket/docs/deployment.md`
- API Guide: `/home/cristiansrc/Documentos/Proyectos/criptopass-workspace/projects/criptopass-ms-ticket-ticket/docs/api-guide.md`
- OpenAPI: `/home/cristiansrc/Documentos/Proyectos/criptopass-workspace/projects/criptopass-ms-ticket-ticket/docs/api/openapi.yaml`
- Env Example: `/home/cristiansrc/Documentos/Proyectos/criptopass-workspace/projects/criptopass-ms-ticket-ticket/.env.example`

## Artifact evidence
- pass | /.../README.md | Status: validated-not-executed, PostgreSQL 16+, stack, endpoints, modelo, roles
- pass | /.../docs/architecture.md | C4, hexagonal con UserClient, 3 secuencias, estados, seguridad, métricas
- pass | /.../docs/deployment.md | Docker Compose con healthcheck, K8s manifests, Flyway, PostgreSQL 16+
- pass | /.../docs/api-guide.md | 11 endpoints con ejemplos, SCREAMING_SNAKE_CASE, QR TTL aclarado, rate limiting corregido
- pass | /.../docs/api/openapi.yaml | 11 endpoints (incluye webhook), 409 en transfer/revoke, 404 en events, Location header, schemas completos
- pass | /.../.env.example | Variables documentadas, QR_CODE_TTL_MINUTES=5 coherente

## Spec Validator Approval
verdict: ready
reviewed_at: 2026-05-15T17:15:00Z
validator_agent: spec-validator
artifact_set_reviewed: /home/cristiansrc/Documentos/Proyectos/criptopass-workspace/projects/criptopass-ms-ticket-ticket/README.md, /home/cristiansrc/Documentos/Proyectos/criptopass-workspace/projects/criptopass-ms-ticket-ticket/docs/architecture.md, /home/cristiansrc/Documentos/Proyectos/criptopass-workspace/projects/criptopass-ms-ticket-ticket/docs/deployment.md, /home/cristiansrc/Documentos/Proyectos/criptopass-workspace/projects/criptopass-ms-ticket-ticket/docs/api-guide.md, /home/cristiansrc/Documentos/Proyectos/criptopass-workspace/projects/criptopass-ms-ticket-ticket/docs/api/openapi.yaml, /home/cristiansrc/Documentos/Proyectos/criptopass-workspace/projects/criptopass-ms-ticket-ticket/.env.example
summary: Validación final completada. Los 16 hallazgos (14 originales + N-1 + N-2) están resueltos. El GET /ticket-types ahora incluye respuesta 400 para event_id faltante. La especificación está lista para Task Decomposer.
invalidated_by_changes_since: none

## Decisions locked
- Stack: Kotlin 2.2.21 + Spring Boot 4.0.6 + Gradle 8.14+
- Arquitectura: Hexagonal (Puertos y Adaptadores)
- Auth: Keycloak OAuth2/OIDC con JWT Bearer
- DB: PostgreSQL 16+ con Flyway para migraciones
- API base path: /ms-ticket-ticket/v1
- Roles: CUSTOMER, ADMIN, ORGANIZER
- TicketStatus enum: PENDING_PAYMENT, ACTIVE, TRANSFERRED, VALIDATED, REVOKED, EXPIRED
- Transferencia: ticket original → TRANSFERRED, nuevo ticket para recipient → ACTIVE (response retorna el nuevo ticket ACTIVE)
- QR Code: short-lived (TTL 5 min), expires_at indica expiración del QR, no del evento
- Error response: campo `error` en SCREAMING_SNAKE_CASE (BAD_REQUEST, FORBIDDEN, etc.)
- Webhook payment: POST /webhooks/payment/confirmation con firma HMAC-SHA256, idempotente por payment_id
- PostgreSQL version: 16+ (alineado en README, deployment.md y Docker Compose)

## Validator findings
- Todos los 16 hallazgos (14 originales + N-1 + N-2) resueltos

## Resolved findings
- B-1, H-1 a H-6, M-1 a M-4, L-1 a L-3, N-1, N-2: Todos resueltos

## Open questions
- ¿Se requiere integración con Redis para cache? (documentado como opcional)
- ¿Blockchain es un servicio interno o externo al ecosistema?
- ¿El Payment Service usa solo webhooks o también polling como fallback?

## Stale terms guard
- Ninguno identificado

## Next action
Task Decomposer
