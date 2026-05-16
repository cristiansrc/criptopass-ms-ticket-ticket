# Guía de Despliegue - CriptoPass MS Ticket Ticket

## Requisitos del Entorno

### Producción

| Componente | Versión Mínima | Notas |
|---|---|---|
| JDK | 21 (LTS) | Virtual Threads habilitados |
| PostgreSQL | 16+ | Con extensión `pgcrypto` si se requiere |
| Redis | 7+ | Cache y sesiones |
| Keycloak | 22+ | Identity Provider |
| Docker | 24+ | Para contenedorización |
| Kubernetes | 1.28+ | Orquestación (opcional) |

### Desarrollo

| Componente | Versión | Instalación |
|---|---|---|
| JDK | 21 | `sdk install java 21-tem` |
| Gradle | 8.x | Incluido via wrapper |
| Docker Compose | 2.x | Para dependencias locales |

## Variables de Entorno

Ver [.env.example](../.env.example) para el listado completo.

### Clasificación por Entorno

| Variable | Desarrollo | Staging | Producción |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | `staging` | `prod` |
| `DB_HOST` | `localhost` | `db-staging.internal` | `db-prod.internal` |
| `DB_PASSWORD` | `devpass` | Vault | Vault |
| `KEYCLOAK_ISSUER_URI` | `http://localhost:8080/realms/criptopass` | `https://auth-staging...` | `https://auth...` |
| `LOG_LEVEL` | `DEBUG` | `INFO` | `WARN` |

## Despliegue con Docker

### 1. Construir la Imagen

```bash
# Construir JAR
./gradlew bootJar

# Construir imagen Docker
docker build -t criptopass/ms-ticket-ticket:latest .
```

### 2. Docker Compose (Desarrollo)

```yaml
# docker-compose.yml
services:
  ms-ticket-ticket:
    image: criptopass/ms-ticket-ticket:latest
    ports:
      - "8082:8082"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      DB_HOST: postgres
      DB_PASSWORD: devpass
      KEYCLOAK_ISSUER_URI: http://keycloak:8080/realms/criptopass
    depends_on:
      postgres:
        condition: service_healthy
      flyway:
        condition: service_completed_successfully
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:8082/actuator/health || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
    networks:
      - criptopass-net

  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: ticket_db
      POSTGRES_USER: ticket_user
      POSTGRES_PASSWORD: devpass
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ticket_user -d ticket_db"]
      interval: 5s
      timeout: 3s
      retries: 5
    networks:
      - criptopass-net

  flyway:
    image: flyway/flyway:latest
    command: -url=jdbc:postgresql://postgres:5432/ticket_db -user=ticket_user -password=devpass -locations=filesystem:/flyway/sql migrate
    volumes:
      - ./src/main/resources/db/migration:/flyway/sql
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - criptopass-net

networks:
  criptopass-net:
    driver: bridge

volumes:
  pgdata:
```

### 3. Ejecutar

```bash
docker compose up -d
```

## Despliegue en Kubernetes

### Manifest Base

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ms-ticket-ticket
  namespace: criptopass
spec:
  replicas: 3
  selector:
    matchLabels:
      app: ms-ticket-ticket
  template:
    metadata:
      labels:
        app: ms-ticket-ticket
    spec:
      containers:
        - name: ms-ticket-ticket
          image: criptopass/ms-ticket-ticket:1.0.0
          ports:
            - containerPort: 8082
          envFrom:
            - secretRef:
                name: ms-ticket-ticket-secrets
            - configMapRef:
                name: ms-ticket-ticket-config
          resources:
            requests:
              cpu: 250m
              memory: 512Mi
            limits:
              cpu: "1"
              memory: 1Gi
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8082
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8082
            initialDelaySeconds: 10
            periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: ms-ticket-ticket
  namespace: criptopass
spec:
  selector:
    app: ms-ticket-ticket
  ports:
    - port: 80
      targetPort: 8082
  type: ClusterIP
```

### ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: ms-ticket-ticket-config
  namespace: criptopass
data:
  SPRING_PROFILES_ACTIVE: "prod"
  DB_HOST: "postgres-primary.criptopass.svc.cluster.local"
  DB_PORT: "5432"
  DB_NAME: "ticket_db"
  KEYCLOAK_ISSUER_URI: "https://auth.criptopass.com/realms/criptopass"
  LOG_LEVEL: "WARN"
  SERVER_PORT: "8082"
```

### Secret (ejemplo - usar Vault en producción)

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: ms-ticket-ticket-secrets
  namespace: criptopass
type: Opaque
stringData:
  DB_USERNAME: "ticket_user"
  DB_PASSWORD: "<password-segura>"
  KEYCLOAK_CLIENT_SECRET: "<client-secret>"
```

## Migraciones de Base de Datos

### Estructura de Migraciones

```
src/main/resources/db/migration/
├── V1__create_ticket_tables.sql
├── V2__create_ticket_type_tables.sql
├── V3__add_blockchain_columns.sql
└── V4__create_indexes.sql
```

### Ejecutar Migraciones

```bash
# Via Gradle
./gradlew flywayMigrate

# Via Docker
docker run --rm \
  -v $(pwd)/src/main/resources/db/migration:/flyway/sql \
  flyway/flyway:latest \
  -url=jdbc:postgresql://<host>:5432/ticket_db \
  -user=<user> \
  -password=<password> \
  -locations=filesystem:/flyway/sql \
  migrate
```

### Rollback

```bash
./gradlew flywayUndo
```

## Monitoreo

### Health Checks

| Endpoint | Descripción | Uso |
|---|---|---|
| `/actuator/health` | Estado general | Load balancer |
| `/actuator/health/liveness` | Liveness probe | Kubernetes |
| `/actuator/health/readiness` | Readiness probe | Kubernetes |
| `/actuator/health/db` | Estado de BD | Diagnóstico |
| `/actuator/metrics` | Métricas | Prometheus |

### Integración con Prometheus

```yaml
# Configuración de scrape
scrape_configs:
  - job_name: 'ms-ticket-ticket'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['ms-ticket-ticket:8082']
```

### Dashboards Recomendados (Grafana)

1. **Ticket Operations**: compras, validaciones, transferencias por minuto
2. **Performance**: latencia p50/p95/p99 por endpoint
3. **Error Rate**: tasa de errores 4xx/5xx
4. **Blockchain**: latencia de transacciones, tasa de éxito

## Runbook de Incidentes

### Boletas no se pueden validar

1. Verificar conectividad a PostgreSQL: `curl http://localhost:8082/actuator/health/db`
2. Revisar logs por errores de concurrencia: `grep "optimistic lock" logs/`
3. Verificar que el estado de la boleta sea `ACTIVE` o `TRANSFERRED`
4. Si hay deadlock, reiniciar pods uno a uno

### Pagos no confirman boletas

1. Verificar webhook del Payment Service
2. Revisar boletas en `PENDING_PAYMENT` con timeout > 15 min
3. Ejecutar job de compensación para boletas expiradas

### Alta latencia en validación

1. Verificar índices en `qr_code`: `EXPLAIN ANALYZE SELECT ...`
2. Revisar conexión a blockchain
3. Escalar horizontalmente si CPU > 80%

## Estrategia de Release

### Versionado

- **SemVer**: `MAJOR.MINOR.PATCH`
- **Imágenes Docker**: `criptopass/ms-ticket-ticket:<version>`
- **Tags Git**: `v<version>`

### Canary Deployment

```yaml
# 10% del tráfico a la nueva versión
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: ms-ticket-ticket
spec:
  http:
    - route:
        - destination:
            host: ms-ticket-ticket
            subset: v1-0-0
          weight: 90
        - destination:
            host: ms-ticket-ticket
            subset: v1-1-0
          weight: 10
```
