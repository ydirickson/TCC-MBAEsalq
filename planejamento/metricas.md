# 6. Métricas previstas (alto nível)
[← Voltar ao índice](./README.md)

Este detalhamento conecta as métricas previstas aos **cenários definidos** e descreve **coleta e visualização** na stack atual do projeto (Prometheus + Grafana + k6 via remote write + postgres_exporter).

## 6.1 Métricas da pesquisa (significado, unidade e relevância)

Objetivo desta seção: definir as métricas que sustentam a hipótese de que um ambiente distribuído de replicação pode substituir um ambiente centralizado **sem perda significativa de desempenho e confiabilidade dos dados**.

| ID | Métrica | O que significa | Bom sinal | Sinal de alerta | Unidade | Relevância para a pesquisa | Referências base |
|---|---|---|---|---|---|---|---|
| M1 | Latência ponta-a-ponta | Tempo entre a mudança na origem e a persistência no destino. | P95/P99 estáveis e próximos ao baseline centralizado. | Crescimento de cauda (P95/P99), picos longos e imprevisíveis. | ms | Mede impacto direto no tempo de atualização dos dados replicados. | [R1], [R2], [R4] |
| M2 | Throughput | Taxa de eventos efetivamente processados na replicação. | Mantém ou supera o baseline sob carga equivalente. | Queda de taxa com aumento de carga ou filas crescentes. | eventos/s | Demonstra capacidade do modelo distribuído sem degradação relevante. | [R1], [R3], [R4] |
| M3 | Taxa de erro/perda | Percentual de eventos não processados corretamente (falha, descarte ou perda). | Próximo de zero e sem tendência de aumento. | Crescimento contínuo ou falhas intermitentes recorrentes. | % | É um dos principais indicadores de confiabilidade da replicação. | [R1], [R2], [R10], [R11] |
| M4 | Consistência dos dados replicados | Grau de integridade/correção entre origem e destino após replicação. | Registros críticos íntegros e sem divergências de negócio. | Diferenças de campos-chave, ausência de registros ou ordens incorretas. | % de validações corretas (ou sim/não por caso) | Prova que a troca arquitetural preserva a confiabilidade dos dados. | [R2], [R6], [R10], [R11] |
| M5 | Inconsistência temporária (lag/staleness) | Janela em que o destino fica desatualizado em relação à origem. | Janela curta e previsível dentro do SLA. | Janela longa, variável e com acúmulo de backlog. | ms e/ou eventos em atraso | Complementa latência ao mostrar desatualização temporária percebida. | [R3], [R4], [R5] |
| M6 | Recursos computacionais | Consumo de CPU, memória e I/O no banco e nos consumidores. | Uso estável e proporcional à carga aplicada. | Saturação frequente, gargalos de I/O ou crescimento sem controle. | % CPU, GB RAM, IOPS/MB/s | Mostra custo operacional e eficiência do modelo distribuído. | [R1], [R8], [R9], [R12] |
| M7 | Disponibilidade da plataforma de replicação | Tempo em que os componentes de replicação estão operacionais. | Uptime alto e recuperação rápida após falhas. | Quedas frequentes, indisponibilidade prolongada ou recuperação lenta. | % uptime | Valida resiliência da solução distribuída frente a falhas. | [R2], [R7], [R13] |
| M8 | Complexidade operacional | Esforço adicional para operar, monitorar e recuperar o ambiente. | Processos claros, baixo retrabalho manual e observabilidade adequada. | Muitos pontos de falha, tarefas manuais e alto custo de operação. | contagem de componentes/dependências e avaliação qualitativa | Apoia a decisão arquitetural além de performance e confiabilidade. | [R14] |

### 6.1.2 Referências confiáveis das medidas (fontes primárias)

- **[R1] Google SRE Book - Monitoring Distributed Systems (Four Golden Signals: latency, traffic, errors, saturation):** https://sre.google/sre-book/monitoring-distributed-systems/
- **[R2] Google SRE Book - Service Level Objectives (SLI/SLO, disponibilidade, latência e percentis):** https://sre.google/sre-book/service-level-objectives/
- **[R3] Apache Kafka - Monitoring (consumer metrics, `records-consumed-rate`, `records-lag-max`):** https://kafka.apache.org/36/operations/monitoring/
- **[R4] Prometheus - Query functions (`rate`, `histogram_quantile`):** https://prometheus.io/docs/prometheus/latest/querying/functions/
- **[R5] PostgreSQL - Monitoring statistics (`pg_stat_replication`, lag de replicação):** https://www.postgresql.org/docs/current/monitoring-stats.html
- **[R6] PostgreSQL - Logical replication conflicts (integridade/consistência em conflitos):** https://www.postgresql.org/docs/current/logical-replication-conflicts.html
- **[R7] Google SRE Book - Availability Table (cálculo de disponibilidade por janela):** https://sre.google/sre-book/availability-table/
- **[R8] Prometheus Node Exporter (métricas de host: CPU, memória, disco, rede):** https://github.com/prometheus/node_exporter
- **[R9] cAdvisor (uso de recursos e performance de containers):** https://github.com/google/cadvisor
- **[R10] Grafana k6 - Built-in metrics (`checks`, counters, trends):** https://grafana.com/docs/k6/latest/using-k6/metrics/reference/
- **[R11] Grafana k6 - Checks e thresholds (taxa de sucesso/falha e critérios de aprovação):** https://grafana.com/docs/k6/latest/using-k6/checks/
- **[R12] Prometheus - Metric types (counter, gauge, histogram, summary e unidades):** https://prometheus.io/docs/concepts/metric_types/
- **[R13] Prometheus - Alerting rules (`up == 0`, monitoramento de disponibilidade de alvos):** https://prometheus.io/docs/prometheus/latest/configuration/alerting_rules/
- **[R14] Google SRE Book - Eliminating Toil (base para complexidade operacional):** https://sre.google/sre-book/eliminating-toil/

### 6.1.3 Prioridade para decisão (centralizado x distribuído)

Para responder ao objetivo da pesquisa, a ordem de importância para comparação é:

1. **Confiabilidade de dados:** M3 + M4  
2. **Desempenho:** M1 + M2 + M5  
3. **Resiliência:** M7  
4. **Eficiência e operação:** M6 + M8

Em termos de conclusão, o ambiente distribuído só é considerado equivalente/superior quando mantém confiabilidade (M3/M4) e desempenho (M1/M2/M5) em nível comparável ao baseline centralizado.

### 6.1.4 Corte analítico por arquitetura (dentro de cada cenário)

Para manter comparabilidade, a leitura recomendada é: **fixar o cenário (1-4)** e comparar **DB Based vs CDC+Kafka vs EDA+Kafka**.

| Arquitetura | Métricas de foco | Sinais esperados | Sinais de alerta |
|---|---|---|---|
| DB Based | M1, M4, M6, M8 | Menor latência local e consistência direta no banco. | Acoplamento alto, scripts complexos e dificuldade de evolução entre serviços. |
| CDC+Kafka | M1, M2, M3, M5, M7 | Boa escalabilidade de consumo e desacoplamento moderado com impacto controlado de latência. | Lag crescente, perda de eventos CDC, indisponibilidade de conectores/broker. |
| EDA+Kafka | M2, M3, M5, M7, M8 | Maior autonomia entre serviços e contratos de negócio claros com boa escalabilidade. | Eventos sem versionamento/idempotência, reprocessamento complexo e DLQ crescente. |

### Como medir esse corte arquitetural

- **Cenário 1:** usar DB Based como baseline e rodar CDC+Kafka/EDA+Kafka para medir custo de introdução de assíncrono.
- **Cenários 2-4:** executar as 3 arquiteturas com mesma carga k6 e comparar percentis (M1), taxa (M2), falha (M3), consistência (M4), lag (M5), recursos (M6), uptime (M7) e inventário operacional (M8).
- **Conclusão por cenário:** registrar vencedor técnico por prioridade (M3+M4 > M1+M2+M5 > M7 > M6+M8) e depois consolidar a visão global.

## 6.2 Aplicação por cenário (DB Based, CDC+Kafka e EDA+Kafka)

Nesta versão, o passo-a-passo detalhado abaixo está descrito para o **cenário 1 em DB Based**.  
Para **CDC+Kafka** e **EDA+Kafka** (cenários 1-4), aplicar o mesmo protocolo de carga e acrescentar métricas do broker/consumidores (ex.: lag, taxa de consumo, retries, DLQ) para leitura de M2, M3, M5 e M7.

### 6.2.1 Cenário 1 (Simples: mesmo BD e mesmo schema, com triggers/procedures)

No cenário 1, a replicação é síncrona no PostgreSQL (sem Kafka/CDC), implementada pelos scripts:

- `bd/simples/05_vinculo_academico_sync.sql`
- `bd/simples/07_requerimento_diploma_sync.sql`
- `bd/simples/08_assinatura_sync.sql`
- `bd/simples/09_documento_oficial_sync.sql`

Para obter as métricas da pesquisa neste cenário, a fonte de dados será:

- **k6** (`monitoramento/k6/scripts/replication-tests.js`) para latência de replicação, sucesso/falha e checks de consistência.
- **Prometheus** (`monitoramento/prometheus/prometheus.yml`) coletando `postgres-exporter` e `/actuator/prometheus` dos 4 serviços.
- **Grafana** para visualização dos percentis, séries temporais e comparação com baseline.
- **Consultas SQL de reconciliação** (via `psql`) para validação de consistência funcional entre tabelas origem e tabelas sincronizadas por trigger.

#### Como cada métrica (M1-M8) será obtida no cenário 1

| ID | Como obter | Fonte principal | Critério de leitura no cenário 1 |
|---|---|---|---|
| M1 (Latência ponta-a-ponta) | Rodar `replication-tests.js` e coletar a métrica customizada `replication_latency_ms` (Trend), calculada no tempo entre criação na origem e confirmação no destino via polling. | k6 + Grafana/Prometheus | Usar média, P95 e P99 por execução e comparar com baseline do cenário centralizado. |
| M2 (Throughput) | Calcular taxa por `replication_success_total` ao longo do tempo de teste; complementar com taxa de requisições HTTP por endpoint de escrita/leitura (`http_server_requests_seconds_count`). | k6 + métricas Micrometer (Actuator) | Medir eventos/s estáveis sob perfis leve/médio/pesado sem queda progressiva. |
| M3 (Taxa de erro/perda) | Usar `replication_failure_total`, `checks` (rate) e falhas HTTP (`http_req_failed` no k6). Fórmula principal: `falhas / (sucessos + falhas)`. | k6 | Taxa próxima de zero, sem tendência de crescimento ao aumentar carga. |
| M4 (Consistência dos dados replicados) | Combinar checks funcionais do `replication-tests.js` (comparação de campos essenciais entre serviços) com consultas SQL de reconciliação entre tabelas de origem e tabelas sincronizadas por trigger. | k6 + SQL no PostgreSQL | Divergência deve ser zero (ou residual justificada) para entidades críticas (`pessoa`, `vinculo_academico`, `requerimento_diploma`, `documento_assinavel`). |
| M5 (Inconsistência temporária / lag) | No cenário 1, usar `replication_latency_ms` como proxy de staleness (não há fila/consumer). Complementar com contagem de timeouts de replicação (`replication_failure_total` por timeout). | k6 | Janela curta e previsível (esperado baixo), sem acúmulo de atrasos. |
| M6 (Recursos computacionais) | Coletar CPU/memória/JVM/conexões dos serviços via Actuator (`process_*`, `jvm_*`, `hikaricp_*`) e carga de banco via `postgres-exporter` (`pg_stat_*`). | Prometheus (Spring + postgres-exporter) | Verificar estabilidade de uso por serviço e ausência de saturação contínua no banco. |
| M7 (Disponibilidade) | Calcular uptime com `up` dos jobs `spring_*`, `postgres_exporter` e `prometheus`. Ex.: `%uptime = avg_over_time(up[janela]) * 100`. | Prometheus | Uptime alto dos componentes críticos durante as execuções de teste. |
| M8 (Complexidade operacional) | Medir por inventário operacional do cenário: número de componentes ativos no `docker-compose`, número de scripts SQL de sincronização, número de scripts de teste/monitoramento e passos manuais de recuperação. | `docker-compose.yml` + docs/scripts | Usar comparação qualitativa e contagem objetiva para contrastar com cenários 2-4. |

#### Consultas SQL de reconciliação sugeridas para M4

```sql
-- 1) Vinculos concluidos sem requerimento de diploma (deve ser 0)
SELECT COUNT(*) AS vinculos_sem_requerimento
FROM vinculo_academico v
LEFT JOIN requerimento_diploma r ON r.vinculo_id = v.id
WHERE v.tipo_vinculo = 'ALUNO'
  AND v.situacao = 'CONCLUIDO'
  AND r.id IS NULL;
```

```sql
-- 2) Documentos de diploma sem documento assinavel (deve ser 0)
SELECT COUNT(*) AS diplomas_sem_documento_assinavel
FROM documento_diploma dd
LEFT JOIN documento_assinavel da ON da.documento_diploma_id = dd.id
WHERE da.id IS NULL;
```

```sql
-- 3) Documentos oficiais sem documento assinavel (deve ser 0)
SELECT COUNT(*) AS oficiais_sem_documento_assinavel
FROM documento_oficial dof
LEFT JOIN documento_assinavel da ON da.documento_oficial_id = dof.id
WHERE da.id IS NULL;
```

#### Execução padrão para coleta do cenário 1

```bash
docker compose up -d
k6 run --out experimental-prometheus-rw=http://localhost:9090/api/v1/write \
  monitoramento/k6/scripts/replication-tests.js
```

## 6.3 Encaixe na stack Grafana (coleta e visualização)

### Coleta (Prometheus)
- **Prometheus** coleta métricas via `scrape_configs` definidos na configuração do servidor.  
  Referência: [Prometheus configuration](https://prometheus.io/docs/prometheus/latest/configuration/configuration/)
- **Aplicações** expõem `/actuator/prometheus` no formato Prometheus.  
  Referência: [Exposition formats](https://prometheus.io/docs/instrumenting/exposition_formats/)
- **Postgres:** usar `postgres_exporter` para expor `pg_stat_*` ao Prometheus.  
  Referência: [postgres_exporter](https://github.com/prometheus-community/postgres_exporter)
- **Kafka (cenários 2-4):** Kafka expõe métricas via JMX; usar `jmx_exporter` para publicar em `/metrics`.  
  Referências: [Kafka monitoring (JMX)](https://kafka.apache.org/38/documentation/#monitoring), [jmx_exporter](https://github.com/prometheus/jmx_exporter)
- **Hosts (opcional):** usar `node_exporter` para CPU/mem/disk quando necessário.  
  Referência: [node_exporter](https://prometheus.io/docs/guides/node-exporter/)
- **Logs (opcional):** enviar logs para Loki e correlacionar com métricas quando necessário.  
  Referências: [Loki data source](https://grafana.com/docs/grafana/latest/datasources/loki/), [Logs in Explore](https://grafana.com/docs/grafana/latest/explore/logs-integration/)

### Visualização (Grafana)
- **Datasource Prometheus** para métricas (dashboards de latência, throughput, carga e lag).  
  Referências: [Grafana Prometheus datasource](https://grafana.com/docs/grafana/latest/datasources/prometheus/), [Grafana for Prometheus](https://prometheus.io/docs/visualization/grafana/)
- **Datasource Loki (opcional)** para logs e correlação de incidentes.  
  Referência: [Grafana Loki datasource](https://grafana.com/docs/grafana/latest/datasources/loki/)
- **Painéis recomendados (por métrica):**  
  - **M1/M5:** séries temporais e percentis de latência/lag.  
  - **M2:** taxa de eventos (consumo e escrita no DB).  
  - **M3/M4:** taxa de erro e taxa de validações de consistência aprovadas.  
  - **M6:** CPU/mem/IO por host e por serviço.  
  - **M7:** uptime e indisponibilidade por componente crítico.  
  - **M8:** tabela simples com componentes, pontos de falha e tarefas operacionais.
