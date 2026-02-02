# TCC-MBA Esalq - AI Coding Agent Instructions

## Project Overview
Academic simulation comparing database replication vs. Kafka-based event-driven architecture for data replication. Four microservices (Graduação, Pós-graduação, Diplomas, Assinatura) share entities (Pessoa, VínculoAcademico) through multiple scenarios:
- **Scenario 1 (baseline)**: Same DB, same schemas - triggers/procedures for validation only
- **Scenarios 2+**: Kafka + CDC for event-driven replication with varying isolation levels (schemas, databases, servers)

## Architecture & Service Boundaries

### Service Structure
- **Multi-module Maven project** (parent POM: `servicos/pom.xml`)
- **Spring Boot 4.0.2 + Java 21** - All services use identical tech stack
- **Shared database approach** (Phase 1): PostgreSQL with replication via triggers/views
- **Service ownership**:
  - `graduacao` (8081): Owns Pessoa/VínculoAcademico creation for undergraduate
  - `pos-graduacao` (8082): Owns Pessoa/VínculoAcademico creation for graduate programs
  - `diplomas` (8083): Consumes VínculoAcademico, creates RequerimentoDiploma/DocumentoDiploma
  - `assinatura` (8084): Consumes DocumentoDiploma, handles SolicitacaoAssinatura workflow

### Data Flow & Replication Rules
Read [planejamento/regras-replicacao.md](../planejamento/regras-replicacao.md) for full details:
- **Pessoa**: Created/updated by Graduação or Pós-graduação; replicated to Diplomas/Assinatura
- **VínculoAcademico**: Multi-valued (1:N), includes denormalized `curso_codigo`, `curso_nome`, `curso_tipo`
- **Status transitions**: `VinculoAcademico.situacao='CONCLUIDO'` enables diploma issuance in Diplomas service
- **Check constraints**: e.g., `CONCLUIDO` requires `data_conclusao NOT NULL` (see [bd/simples/00_schema.sql](../bd/simples/00_schema.sql))

## Development Workflows

### Environment Setup
1. **Start infrastructure**: `docker compose up -d` (starts all services + monitoring stack)
2. **Verify health**: `docker compose ps` - wait for `postgres` to show `healthy`
3. **Database access**:
   - Direct CLI: `docker compose exec postgres psql -U tcc -d tccdb`
   - pgAdmin: http://localhost:8080 (credentials in `.env`)
4. **Service URLs**: http://localhost:808{1,2,3,4} (see [docker-compose.yml](../docker-compose.yml) for mappings)

### Build & Run Individual Services
```bash
# From servicos/<service-name>/
mvn spring-boot:run

# Or build multi-module:
cd servicos && mvn clean install
```

### Database Initialization
- **Schema/seed scripts**: `bd/simples/*.sql` - numbered execution order (00, 05, 06, etc.)
- **Sync scripts**: `*_sync.sql` files create triggers/functions for Scenario 1 (baseline validation)
- **Switch scenarios**: Change `BD_CENARIO` env var (default: `simples`) to load different schema folders
- **Scenario folders**: Future scenarios will have `bd/schema/`, `bd/databases/`, `bd/servers/` for isolation levels

## Code Conventions

### Layer Architecture (Standard MVC + Service)
```
api/
  controller/     # @RestController - endpoints use plural nouns (/cursos, /alunos)
  dto/           # *Request/*Response records (no setters)
  mapper/        # Entity <-> DTO conversion (@Component, stateless)
  exception/     # ApiExceptionHandler for global error handling
domain/
  model/         # JPA entities (@Entity, snake_case columns)
  repository/    # Spring Data JPA interfaces
  service/       # @Service - business logic, transaction boundaries
```

### Naming Standards
- **Controllers**: `{Entity}{Service}Controller` → `CursoGraduacaoController`
- **Services**: `{Entity}Service` → `CursoGraduacaoService`
- **DTOs**: `{Entity}Request` / `{Entity}Response` (use Java records)
- **Mappers**: `{Entity}Mapper` with methods like `toEntity()`, `toResponse()`
- **REST paths**: Plural, kebab-case → `/ofertas-disciplinas`, `/documentos-diploma`

### Configuration Patterns
- **Environment variables**: Prefixed by service (e.g., `PGR_POSTGRES_HOST` for Graduação)
- **Port mapping**: Defined in [docker-compose.yml](../docker-compose.yml) (8081-8084)
- **JPA config**: `ddl-auto: validate` - schema managed by SQL scripts, not Hibernate
- **Logging**: Includes MDC context (`traceId`, `spanId`, `requestId`) - see [application.yml](../servicos/graduacao/src/main/resources/application.yml)

## Monitoring & Performance Testing

### Observability Stack
- **Prometheus** (9090): Scrapes `/actuator/prometheus` from all services
- **Grafana** (3000): Dashboards for metrics (admin/admin123)
- **Postgres Exporter** (9187): Database-level metrics

### Load Testing with k6
```bash
# Simple health checks
k6 run monitoramento/k6/scripts/hello-world.js

# Business endpoints (GETs)
k6 run monitoramento/k6/scripts/business-endpoints.js

# CRUD workflows (creates data + reads)
K6_EXECUTION_MODE=dev k6 run monitoramento/k6/scripts/graduacao-crud.js
```
- **Config**: [monitoramento/k6/configs/endpoints.json](../monitoramento/k6/configs/endpoints.json) defines service endpoints
- **Remote write**: Send k6 metrics to Prometheus with `--out experimental-prometheus-rw`

## Key Reference Files
- **Planning docs**: [planejamento/README.md](../planejamento/README.md) - index of all design decisions
- **Module details**: [planejamento/modulos/](../planejamento/modulos/) - per-service specifications
- **Schema**: [bd/simples/00_schema.sql](../bd/simples/00_schema.sql) - authoritative table definitions
- **Replication rules**: [planejamento/regras-replicacao.md](../planejamento/regras-replicacao.md)
- **Service READMEs**: Each service has `/servicos/{service}/README.md` with OpenAPI/Swagger docs

## Testing Context
- **Integration tests**: Use H2 in-memory database (see `test` scope in parent POM)
- **Test configs**: `application-test.yml` in `servicos/{graduacao,pos-graduacao}/src/test/resources/`
- **No security layer**: Intentionally omitted from scope (see service READMEs)
Kafka + CDC Implementation (Scenarios 2+)

### Event-Driven Architecture Patterns
- **Outbox Pattern**: Transactional outbox table (`outbox_eventos`) written atomically with business data
- **Inbox Pattern**: Consumer-side inbox for idempotent event processing (prevent duplicates)
- **CDC Options**: Debezium (preferred) or custom worker polling outbox table
- **Event Schema**: All events include `eventId`, `aggregateId`, `timestamp`, `version`, payload

### Canonical Events (see [planejamento/modulos/compartilhadas.md](../planejamento/modulos/compartilhadas.md))
- **Pessoa**: `PessoaCriada`, `PessoaAtualizada`
- **VínculoAcademico**: `VinculoAcademicoCriado`, `VinculoAcademicoAtualizado`, `ConclusaoPublicada`
- **Diploma**: `DiplomaEmitido`, `DocumentoDiplomaCriado`, `DocumentoDiplomaAtualizado`
- **Assinatura**: `SolicitacaoAssinaturaCriada`, `AssinaturaConcluida`, `AssinaturaRejeitada`
- **Documentos**: `DocumentoOficialCriado`, `DocumentoOficialAtualizado`

### Kafka Infrastructure Setup
```yaml
# docker-compose.kafka.yml structure (to be created)
services:
  zookeeper:    # Kafka coordination
  kafka:        # Message broker (3 brokers recommended for production-like scenario)
  schema-registry: # Avro/JSON schema management
  kafka-connect: # Debezium CDC connectors
  kafka-ui:     # Management interface (http://localhost:8090)
```

### Service Integration Points
- **Producers (Graduação/Pós)**: Write to outbox table in same transaction as domain entities
- **Consumers (All services)**: Subscribe to relevant topics, write to inbox before processing
- **Topic naming**: `{domain}.{entity}.{event}` → `graduacao.pessoa.criada`, `diplomas.diploma.emitido`
- **Consumer groups**: One per service to enable parallel consumption with offset management

### Metrics to Track (see [planejamento/metricas.md](../planejamento/metricas.md))
- **M1**: End-to-end latency (`timestamp_origem` → `timestamp_aplicacao`)
- **M2**: Throughput (events/sec via `records-consumed-rate`)
- **M3**: Database load (compare `blks_read/hit` across scenarios)
- **M4**: Application load (consumer CPU/memory via node_exporter)
- **M5**: Staleness window (`records-lag-max`, p95/p99 of event age)

### Scenario Comparison Matrix
| Scenario | Infrastructure | Replication Method | Key Trade-offs |
|----------|---------------|-------------------|----------------|
| 1-Simples | Same DB/schema | Triggers/procedures | Low latency, high coupling |
| 2-Schema | Same DB, separate schemas | Outbox+Kafka+Inbox | Schema isolation, same DB contention |
| 3-Databases | Multiple DBs, same server | Outbox+Kafka+Inbox | DB isolation, eventual consistency |
| 4-Servers | Multiple servers | Outbox+Kafka+Inbox | Full isolation, network latency |

### Implementation Checklist for New Scenarios
1. Create `bd/{scenario}/00_schema.sql` with outbox/inbox tables
2. Add `docker-compose.kafka.yml` with broker + Debezium
3. Implement outbox writer in service layer (Spring `@Transactional`)
4. Configure Debezium connector for each producer database
5. Implement Kafka consumers with inbox deduplication
6. Add Prometheus metrics for lag/throughput in Actuator
7. Update Grafana dashboards for Kafka JMX metrics
8. Test with k6 scripts modified for eventual consistency

## Critical Don'ts
- **Never** modify database schema via Hibernate (`ddl-auto: validate` is enforced)
- **Never** break check constraints (e.g., `CONCLUIDO` without `data_conclusao`)
- **Never** create cross-service foreign keys (each service reads replicated copies)
- **Never** publish events outside transactional outbox (risks data loss on rollback)
- **Never** process events without checking inbox for duplicates (idempotency required)
- **Avoid** mixing entity ownership (Pessoa creation belongs to Graduação/Pós only)
- **Avoid** synchronous Kafka calls in request path (degrades latency - use async consumerss)
- **Avoid** mixing entity ownership (Pessoa creation belongs to Graduação/Pós only)
