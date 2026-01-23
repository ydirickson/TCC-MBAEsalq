# TCC-MBA Esalq — Simulação de Replicação vs Kafka

Repositório do meu Trabalho de Conclusão de Curso (TCC) em Engenharia da Computação em 2025 e 2026. O tema deste TCC é propor uma solução de replicação de dados corporativos baseados em uma arquitetura distribuída e _event driven_, utilizando o Apache Kafka como peça central, possibilitando uma flexibilidade maior na comunicação e replicação destes dados com os diversos serviços que os consomem.

## Introdução e Contexto Resumidos

Vindo de um ambiente altamente acoplado de diversos sistemas/serviços corporativos, uma das estratégias mais clássicas para replicação de dados é utilizando algum mecanismo próprios dos Sistemas Gerenciadores de Banco de Dados (SGBDs) que permita que os dados presentes nesses bancos sejam replicados para outros locais de mesmo banco, seja por motivo de redundância, seja por motivos de compartilhamento desses dados.

A partir desse contexto, a ideia é propor uma solução que retire a dependência nas soluções proprietárias e/ou centralizadas desses SGBDs para uma arquitetura distribuída, garantindo assim que os dados sejam replicados da mesma forma e com a mesma segurança e desempenho, ganhando também maior flexibilidade em como os diversos sistemas irão consumir ou utilizar esses dados.

## Visão geral
- Objetivo: medir latência, throughput e impacto arquitetural ao migrar de replicação baseada em banco para replicação por eventos (Kafka).
- Domínio: serviços acadêmicos com entidades compartilhadas (Pessoa, VínculoAcadêmico) e fluxos de diploma/assinatura.
- Fases: 1) acoplado por replicação de banco; 2) desacoplado por eventos Kafka.

## Estrutura do repositório
- `planejamento/` — documentos de contexto, premissas e regras (ver índice em `planejamento/README.md`).
- `planejamento/modulos/` — visão por serviço e conceitos compartilhados.
- `docker-compose.yml` — infraestrutura atual + microserviços Spring (grad/pós/diplomas/assinatura).
- `.env.example` — variáveis para parametrizar as imagens e portas do compose.
- `bd/pgadmin/servers.json` — cadastro do servidor Postgres no pgAdmin (carregado no start).
- `docs/` — reservado para diagramas/artefatos gerados (a criar).
- `servicos/` — código da simulação (serviços grad/pós/diplomas/assinatura).

## Planejamento e regras
- Contexto e decisões estão documentados em `planejamento/README.md`.
- Regras de replicação e eventos canônicos: `planejamento/regras-replicacao.md`.
- Entidades e intersecções: `planejamento/entidades-interseccoes.md`.
- Módulos por serviço: `planejamento/modulos/`.

## Como executar — banco centralizado + microserviços (fase 1)
1. Pré-requisitos: Docker/Docker Compose instalados.
2. Copie o arquivo de variáveis: `cp .env.example .env` e ajuste senhas/portas se necessário.
3. Suba a stack completa: `docker compose up -d`.
4. Verifique se está saudável: `docker compose ps` deve mostrar `healthy` no Postgres; em caso de dúvida, `docker compose logs -f postgres` até ver `database system is ready to accept connections`.
5. Acessos:
   - Postgres: `localhost:${POSTGRES_PORT:-5432}` (usuário/senha definidos em `.env`). CLI rápida: `docker compose exec postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"`.
   - pgAdmin: `http://localhost:${PGADMIN_PORT:-8080}` com o login de `.env`. O servidor `TCC Postgres` já vem cadastrado via `bd/pgadmin/servers.json`. Se não aparecer, limpe o volume `pgadmin_data` (`docker volume ls | grep pgadmin_data` para conferir o nome e remover).
6. Monitoramento:
   - Prometheus: http://localhost:9090
   - Grafana: http://localhost:3000 (padrão: admin/admin123)
7. Microserviços:
   - Graduação: http://localhost:8081
   - Pós-graduação: http://localhost:8082
   - Diplomas: http://localhost:8083
   - Assinatura: http://localhost:8084
8. Actuator/Prometheus:
   - Endpoint padrão: `/actuator/prometheus` em cada serviço.

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
Os scripts ficam em `monitoramento/k6/scripts/` e as configs em `monitoramento/k6/configs/`.

Hello world (healthcheck dos serviços):
```bash
k6 run monitoramento/k6/scripts/hello-world.js
```

Endpoints de negócio (GETs simples):
```bash
k6 run monitoramento/k6/scripts/business-endpoints.js
```

Parâmetros úteis (ambos os scripts):
- `VUS` (padrão: 1)
- `DURATION` (padrão: 5s no hello-world, 10s no business-endpoints)
- `RUN_ID` (default automático)

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
- Modelagem: definir schema inicial de Pessoa e VínculoAcadêmico em SQL; tabelas de histórico e pedidos de diploma/assinatura.
- Contratos de eventos: descrever payload mínimo de cada evento em `planejamento/regras-replicacao.md` (id, versão/timestamp, origem).
- Serviços: scaffolds para Graduação, Pós, Diplomas e Assinatura com APIs REST básicas e produtores/consumidores Kafka.
- Replicação acoplada: implementar triggers/jobs para replicar Pessoa/Vínculo/PedidoDiploma/Assinatura na fase 1.
- Replicação desacoplada: implementar produtores/consumidores Kafka para os mesmos fluxos na fase 2.
- Métricas e testes: scripts de carga e medição (latência, throughput); testes de idempotência e de atraso/duplicidade de eventos.
- Topologia de bancos (sugestão): iniciar com **um PostgreSQL e schemas por serviço** para montar rápido triggers/ETL; numa segunda fase, evoluir para **múltiplos PostgreSQL (um por serviço)** para medir latência de rede, isolação e custos operacionais de replicação.
