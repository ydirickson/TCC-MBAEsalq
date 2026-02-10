# TCC-MBA Esalq — Simulação de Replicação vs Kafka

Repositório do meu Trabalho de Conclusão de Curso (TCC) em Engenharia da Computação em 2025 e 2026. O tema deste TCC é propor uma solução de replicação de dados corporativos baseados em uma arquitetura distribuída e _event driven_, utilizando o Apache Kafka como peça central, possibilitando uma flexibilidade maior na comunicação e replicação destes dados com os diversos serviços que os consomem.

## Introdução e Contexto Resumidos

Vindo de um ambiente altamente acoplado de diversos sistemas/serviços corporativos, uma das estratégias mais clássicas para replicação de dados é utilizando algum mecanismo próprios dos Sistemas Gerenciadores de Banco de Dados (SGBDs) que permita que os dados presentes nesses bancos sejam replicados para outros locais de mesmo banco, seja por motivo de redundância, seja por motivos de compartilhamento desses dados.

A partir desse contexto, a ideia é propor uma solução que retire a dependência nas soluções proprietárias e/ou centralizadas desses SGBDs para uma arquitetura distribuída, garantindo assim que os dados sejam replicados da mesma forma e com a mesma segurança e desempenho, ganhando também maior flexibilidade em como os diversos sistemas irão consumir ou utilizar esses dados.

## Visão geral
- Objetivo: medir latência, throughput e impacto arquitetural ao migrar de replicação baseada em banco para replicação por eventos (Kafka).
- Domínio: serviços acadêmicos com entidades compartilhadas (Pessoa, VínculoAcadêmico) e fluxos de diploma/assinatura (incluindo documentos oficiais).
- Fases: 1) acoplado por replicação de banco; 2) desacoplado por eventos Kafka.

## Estrutura do repositório
- `planejamento/` — documentos de contexto, premissas e regras (ver índice em `planejamento/README.md`).
- `planejamento/modulos/` — visão por serviço e conceitos compartilhados.
- `compose/` — composes separados por stack (base, serviços, DB C1/C2/C3, overlay CDC, replicação A2/A3, monitoramento).
- `docker-compose-c1.yml` — compose único do cenário 1 (sem variação de arquitetura).
- `docker-compose-c2a1.yml` — compose único do cenário C2+A1 (schemas + DB Based).
- `docker-compose-c2a2.yml` — compose único do cenário C2+A2 (schemas + CDC/Kafka com Debezium Connect).
- `docker-compose*.yml` — legado (mantido para referência/compatibilidade).
- `.env.example` — variáveis para parametrizar as imagens e portas do compose.
- `bd/pgadmin/servers.json` — cadastro do servidor Postgres no pgAdmin (carregado no start).
- `docs/` — reservado para diagramas/artefatos gerados (a criar).
- `servicos/` — código da simulação (serviços grad/pós/diplomas/assinatura).

## Planejamento e regras
- Contexto e decisões estão documentados em `planejamento/README.md`.
- Regras de replicação e eventos canônicos: `planejamento/regras-replicacao.md`.
- Entidades e intersecções: `planejamento/entidades-interseccoes.md`.
- Módulos por serviço: `planejamento/modulos/`.

## Como executar — stacks por cenário
1. Pré-requisitos: Docker/Docker Compose instalados.
2. Escolha o `.env` do cenário (ou use `.env.example` como base). Opções principais: `C1` -> `.env.c1` (`BD_CENARIO=c1`), `C2+A1` -> `.env.c2` (`BD_CENARIO=c2a1`) e `C2+A2` -> `.env.c2a2` (`BD_CENARIO=c2a2`).
3. Execução simplificada do C1 (arquivo único, sem variação de arquitetura):
```bash
docker compose --env-file .env.c1 -f docker-compose-c1.yml up -d
```
4. Para derrubar o C1 simplificado:
```bash
docker compose --env-file .env.c1 -f docker-compose-c1.yml down -v
```
5. Execução simplificada do C2+A1 (arquivo único, schemas + DB Based):
```bash
docker compose --env-file .env.c2 -f docker-compose-c2a1.yml up -d
```
6. Para derrubar o C2+A1 simplificado:
```bash
docker compose --env-file .env.c2 -f docker-compose-c2a1.yml down -v
```
7. Execução simplificada do C2+A2 (arquivo único, schemas + CDC/Kafka):
```bash
docker compose --env-file .env.c2a2 -f docker-compose-c2a2.yml up -d
```
8. Para derrubar o C2+A2 simplificado:
```bash
docker compose --env-file .env.c2a2 -f docker-compose-c2a2.yml down -v
```
9. Suba a base + DB + serviços via stack modular conforme o cenário:
```bash
# C1
docker compose --env-file .env.c1 -f compose/base.yml -f compose/db.c1.yml -f compose/services.yml up -d

# C2 (modular)
docker compose --env-file .env.c2 -f compose/base.yml -f compose/db.c2.yml -f compose/services.yml up -d

# C3
docker compose --env-file .env.c3 -f compose/base.yml -f compose/db.c3.yml -f compose/services.yml up -d
```
Obs: no C3, o bootstrap aplica `bd/${BD_CENARIO}` em cada DB (default `schemas`) para manter as estruturas até a divisão fina dos scripts por serviço.
10. (Opcional) Monitoramento: adicione `-f compose/monitoring.yml` ao comando acima.
11. (Opcional) Replicação:
   - A2 (CDC+Kafka + Connect): adicione `-f compose/db.cdc.yml -f compose/replication.a2.yml`
   - A3 (EDA+Kafka): adicione `-f compose/replication.a3.yml`
12. Ao trocar de cenário, recrie volumes (scripts em `/docker-entrypoint-initdb.d` só rodam na primeira inicialização): `docker compose down -v` usando o mesmo `--env-file` e `-f`.
13. Verifique se está saudável: `docker compose ps` deve mostrar `healthy` nos Postgres; em caso de dúvida, `docker compose logs -f <servico>`.
14. Acessos: Postgres (C1/C2) em `localhost:${POSTGRES_PORT:-5432}`. Prometheus `http://localhost:${PROMETHEUS_PORT:-9090}`. Grafana `http://localhost:${GRAFANA_PORT:-3000}`. Graduação `http://localhost:${GRADUACAO_PORT:-8081}`. Pós-graduação `http://localhost:${POS_GRADUACAO_PORT:-8082}`. Diplomas `http://localhost:${DIPLOMAS_PORT:-8083}`. Assinatura `http://localhost:${ASSINATURA_PORT:-8084}`.
15. Actuator/Prometheus: endpoint padrão `/actuator/prometheus` em cada serviço.

### Exemplo C2 + A2 (recomendado)
```bash
docker compose --env-file .env.c2a2 -f docker-compose-c2a2.yml up -d
```

### Bootstrap automatico no A2
- O service `connect-init` roda automaticamente no `up` e registra os conectores de `infra/kafka-connect/connectors/`.
- O source Debezium esta com `publication.autocreate.mode=filtered`, entao a publication e criada automaticamente.
- No `debezium/connect:3.4`, o sink JDBC (`io.debezium.connector.jdbc.JdbcSinkConnector`) ja vem no worker; `infra/kafka-connect/plugins/` fica opcional.
- Para verificar: `docker compose --env-file .env.c2a2 -f docker-compose-c2a2.yml logs connect-init`.

### Atalho automatizado
Comando unico para montar os arquivos corretos por cenario + arquitetura:

```bash
# cenario unico (compose dedicado)
./simulacao.sh up c1

# sobe C2 + A1
./simulacao.sh up c2a1

# sobe C2 + A2
./simulacao.sh up c2a2

# sobe C2 + A2 + monitoramento
./simulacao.sh up c2a2 --monitoring

# status
./simulacao.sh ps c2a2

# logs do bootstrap de conectores
./simulacao.sh logs c2a2 -- connect-init

# derruba (com volumes)
./simulacao.sh down c2a2
```

Opcao recomendada (entrada compacta por cenario de simulacao):

```bash
# status
./simulacao.sh ps c2a2

# logs do bootstrap de conectores
./simulacao.sh logs c2a2 -- connect-init

# derruba
./simulacao.sh down c2a2
```

## Monitoramento (Prometheus + Grafana)
Stack de observabilidade acoplada ao compose principal para as simulações.

### O que está incluído
- Prometheus (coleta e armazenamento de métricas)
- Grafana (dashboards)
- Postgres Exporter (métricas do banco)
- Spring Actuator + Micrometer Prometheus (métricas dos microserviços)

### Variáveis úteis
O Postgres Exporter usa:
```
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
POSTGRES_DB=tccdb
POSTGRES_USER=tcc
POSTGRES_PASSWORD=tcc123
```
Se precisar, exporte essas variáveis antes de subir o compose.

### Scrape dos microserviços
O Prometheus já está configurado para coletar:
- `graduacao:8081/actuator/prometheus`
- `pos-graduacao:8082/actuator/prometheus`
- `diplomas:8083/actuator/prometheus`
- `assinatura:8084/actuator/prometheus`

### k6 -> Prometheus (remote write)
Para enviar métricas do k6 direto ao Prometheus:
```bash
k6 run --out experimental-prometheus-rw=http://localhost:9090/api/v1/write seu_script.js
```

Sugestão: envie um header `X-Run-Id` nas chamadas para correlacionar com o log do banco.

### k6 (CLI) — scripts de carga
Os scripts ficam em `monitoramento/k6/scripts/` e as configs em `monitoramento/k6/configs/` (arquivo `endpoints.json`).

Hello world (healthcheck dos serviços):
```bash
k6 run monitoramento/k6/scripts/hello-world.js
```

Endpoints de negócio (GETs simples):
```bash
k6 run monitoramento/k6/scripts/business-endpoints.js
```

CRUD de graduação (cria dados + leituras):
```bash
# Requer K6_EXECUTION_MODE definido no .env ou via variável de ambiente
K6_EXECUTION_MODE=constant-vus VUS=5 DURATION=30s k6 run monitoramento/k6/scripts/graduacao-crud.js
```

Parâmetros úteis:
- `VUS` (padrão: 1) - Número de usuários virtuais
- `DURATION` (padrão: 5s no hello-world, 10s no business-endpoints) - Duração do teste
- `RUN_ID` (gerado automaticamente) - Identificador único da execução
- `K6_EXECUTION_MODE` (obrigatório para graduacao-crud.js) - Modo de execução: constant-vus, ramping-vus, constant-arrival-rate, ramping-arrival-rate
- `GRADUACAO_BASE_URL` (padrão: http://localhost:8081) - URL base do serviço
- `SLEEP_S` (padrão: 1) - Segundos de pausa entre iterações

Consulte o guia detalhado em `monitoramento/k6/README.md` para mais opções de configuração.

Exemplo com Prometheus remote write:
```bash
VUS=5 DURATION=30s RUN_ID=simulacao_01 k6 run --out experimental-prometheus-rw=http://localhost:9090/api/v1/write monitoramento/k6/scripts/business-endpoints.js
```

## Convenções de implementação
- Identificadores numéricos (long) para Pessoa e VínculoAcadêmico; evitar chaves compostas.
- Eventos em português, em snake_case ou CamelCase consistente com os mapeamentos das libs (definir por linguagem).
- Interfaces REST simples; contratos de evento priorizam id + versão/timestamp para idempotência.

## Guia de contribuição
- Issues/boards: registrar tarefas em Issues; cada PR deve referenciar pelo menos uma Issue.
- Branches: criar branches de feature/hotfix a partir da `main` usando prefixos claros (`feature/`, `fix/`, `doc/`) e nomes descritivos curtos.
- Pull requests:
  - Abrir PR somente após os testes locais passarem.
  - Descrever objetivo, impacto esperado e mudanças de contrato (REST/eventos).
  - Incluir checklist de verificação (testes executados, migrações necessárias).
  - Não fazer squash local; deixar o squash na fusão se aplicável.
- Revisão: pelo menos uma revisão cruzada antes de merge; para ajustes pequenos, comentário explicando por que não há testes.
- Código: comentários sucintos apenas para regras não óbvias; seguir lints/formatters padrão de cada stack.
- Testes: cobrir fluxos de replicação e idempotência; validar happy path e cenários de atraso/duplicidade.
- Commits: mensagens em português, no imperativo (“Adiciona…”, “Ajusta…”); commits pequenos e focados.

## Conduta e organização
- Evitar reverter mudanças de planejamento sem alinhamento prévio.
- Registrar decisões arquiteturais em `planejamento/pontos-abertos.md` ou ADRs futuros.
- Em dúvidas sobre ownership de dados ou contratos de evento, alinhar primeiro no planejamento antes de codificar.

## Plano inicial de tarefas
- Infraestrutura: montar `docker-compose` com bancos (por serviço) e Kafka/ZooKeeper; criar scripts de bootstrap de tópicos.
- Modelagem: definir schema inicial de Pessoa e VínculoAcadêmico em SQL; tabelas de histórico e pedidos de diploma/assinatura/documentos oficiais.
- Contratos de eventos: descrever payload mínimo de cada evento em `planejamento/regras-replicacao.md` (id, versão/timestamp, origem).
- Serviços: scaffolds para Graduação, Pós, Diplomas e Assinatura com APIs REST básicas e produtores/consumidores Kafka.
- Replicação acoplada: implementar triggers/jobs para replicar Pessoa/Vínculo/PedidoDiploma/Assinatura na fase 1.
- Replicação desacoplada: implementar produtores/consumidores Kafka para os mesmos fluxos na fase 2.
- Métricas e testes: scripts de carga e medição (latência, throughput); testes de idempotência e de atraso/duplicidade de eventos.
- Topologia de bancos (sugestão): iniciar com **um PostgreSQL e schemas por serviço** para montar rápido triggers/ETL; numa segunda fase, evoluir para **múltiplos PostgreSQL (um por serviço)** para medir latência de rede, isolação e custos operacionais de replicação.
