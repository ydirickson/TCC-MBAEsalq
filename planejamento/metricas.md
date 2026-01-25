# 7. Métricas previstas (alto nível)
[← Voltar ao índice](./README.md)

Este detalhamento conecta as métricas previstas aos **cenários definidos** e descreve **coleta e visualização** na stack Grafana (Prometheus + Grafana + Loki).

## 7.1 Métricas detalhadas (definição + referências)

### M1) Latência ponta-a-ponta (origem → persistência no consumidor)
**O que mede:** tempo entre a mudança ser registrada no produtor e a persistência no destino.  
**Como medir:**  
- **Eventos (Kafka):** incluir `timestamp_origem` no evento (outbox) e registrar `timestamp_aplicacao` no consumer. Latência = `timestamp_aplicacao - timestamp_origem`.  
- **Replicação tradicional (Postgres):** usar `write_lag`, `flush_lag`, `replay_lag` de `pg_stat_replication` para o atraso de escrita/flush/replay (visibilidade no standby).  
**Por que avalia bem:** latência de replicação/consumo representa diretamente a janela de desatualização do dado no destino.  
**Referências:** [pg_stat_replication](https://www.postgresql.org/docs/12/monitoring-stats.html#PG-STAT-REPLICATION-VIEW), [Kafka consumer metrics (lag)](https://kafka.apache.org/36/documentation/#consumer_monitoring)

### M2) Throughput (eventos/segundo)
**O que mede:** volume de mudanças processadas por unidade de tempo.  
**Como medir:**  
- **Kafka:** `records-consumed-rate` e `bytes-consumed-rate` do consumidor.  
- **Postgres:** contadores `xact_commit`, `tup_inserted`, `tup_updated` em `pg_stat_database` (taxa por intervalo).  
**Por que avalia bem:** taxas de consumo e de transações traduzem a capacidade de replicação sob carga.  
**Referências:** [Kafka consumer metrics](https://kafka.apache.org/36/documentation/#consumer_monitoring), [pg_stat_database](https://www.postgresql.org/docs/12/monitoring-stats.html#PG-STAT-DATABASE-VIEW)

### M3) Carga no banco (CPU/IO) no acoplado vs no desacoplado
**O que mede:** pressão de leitura/escrita no banco e impacto do mecanismo de sincronização.  
**Como medir:**  
- **Postgres:** `blks_read`, `blks_hit`, `xact_commit` e contadores de tuplas em `pg_stat_database` (comparar cenários).  
- **Host do banco:** CPU/mem/IO via `node_exporter`.  
**Por que avalia bem:** `pg_stat_database` fornece estatísticas de I/O e transações, e métricas do host mostram custo real de CPU/memória.  
**Referências:** [pg_stat_database](https://www.postgresql.org/docs/12/monitoring-stats.html#PG-STAT-DATABASE-VIEW), [node_exporter](https://prometheus.io/docs/guides/node-exporter/)

### M4) Carga na aplicação (CPU/memória) no consumidor Kafka
**O que mede:** custo de processamento no consumer para aplicar eventos.  
**Como medir:**  
- **Host do consumer:** CPU/mem via `node_exporter`.  
- **Aplicação:** expor métricas próprias (ex.: tempo médio de processamento por evento) em `/metrics`.  
**Por que avalia bem:** recursos do host e métricas da aplicação refletem o custo real de consumo.  
**Referências:** [node_exporter](https://prometheus.io/docs/guides/node-exporter/), [Prometheus exposition formats](https://prometheus.io/docs/instrumenting/exposition_formats/)

### M5) Incidência de inconsistência temporária
**O que mede:** proporção/tempo em que o destino está “atrasado” em relação ao produtor.  
**Como medir:**  
- **Eventos:** staleness = `timestamp_aplicacao - timestamp_origem` (p95/p99) e `records-lag-max` do consumer.  
- **Replicação:** `replay_lag` em `pg_stat_replication`.  
**Por que avalia bem:** lag do consumidor/replicação traduz a janela de divergência entre produtor e consumidor.  
**Referências:** [Kafka consumer metrics (records-lag-max)](https://kafka.apache.org/36/documentation/#consumer_monitoring), [pg_stat_replication](https://www.postgresql.org/docs/12/monitoring-stats.html#PG-STAT-REPLICATION-VIEW)

### M6) Complexidade operacional (número de componentes, pontos de falha)
**O que mede:** esforço operacional (toil) e risco por dependências adicionais.  
**Como medir:** contagem de componentes (DBs, brokers, exporters, jobs), dependências críticas e tarefas manuais recorrentes.  
**Por que avalia bem:** o conceito de *toil* mapeia o custo operacional gerado por tarefas repetitivas e por aumento de complexidade.  
**Referências:** [SRE – Eliminating Toil](https://sre.google/sre-book/eliminating-toil/)

## 7.2 Aplicação por cenário (tradicional x eventos)

### 1) Simples (mesmo BD e mesmos schemas)
- **Tradicional (triggers/procedures):**
  - **M1:** medir tempo entre trigger/procedure e escrita na tabela alvo (timestamp interno).  
  - **M2:** taxa por `xact_commit`/`tup_*` no mesmo DB.  
  - **M3:** `blks_read/blks_hit` e CPU/IO do host.  
  - **M5:** staleness tende a ser baixo; medir delta entre timestamps.  
  - **M6:** menor número de componentes.  
- **Eventos (outbox + Kafka local):**
  - **M1/M5:** `timestamp_origem` → `timestamp_aplicacao` + `records-lag-max`.  
  - **M2:** `records-consumed-rate` + taxa de escrita no DB.  
  - **M3/M4:** CPU/IO do DB e CPU/mem do consumer.  
  - **M6:** adiciona broker + worker + consumer.

### 2) Schema (mesmo BD, schemas distintos)
- **Tradicional (triggers cross-schema):**  
  - **M1:** latência entre trigger e persistência no schema consumidor.  
  - **M2/M3:** `pg_stat_database` no mesmo DB (sem separar por schema).  
- **Eventos (outbox + Kafka):**  
  - **M1/M5:** `timestamp_origem` → `timestamp_aplicacao` + lag do consumer.  
  - **M2:** throughput no Kafka + taxa de escrita no schema consumidor.  
  - **M3/M4:** impacto no DB e no consumer.  

### 3) Databases (bancos distintos no mesmo servidor)
- **Tradicional (logical replication):**  
  - **M1/M5:** `write_lag/flush_lag/replay_lag` em `pg_stat_replication`.  
  - **M2/M3:** comparar taxas e I/O entre publisher e subscriber.  
- **Eventos (outbox + Kafka):**  
  - **M1/M5:** `timestamp_origem` → `timestamp_aplicacao` + `records-lag-max`.  
  - **M2:** `records-consumed-rate` vs taxa de escrita no DB consumidor.  
  - **M3/M4:** DBs no mesmo host + consumer.

### 4) Servers (bancos em servidores diferentes)
- **Tradicional (logical replication):**  
  - **M1/M5:** `replay_lag` + monitoramento de rede e disponibilidade.  
  - **M2/M3:** comparar publisher/subscriber e CPU/IO por servidor.  
- **Eventos (outbox + Kafka):**  
  - **M1/M5:** `timestamp_origem` → `timestamp_aplicacao` + lag do consumer.  
  - **M2/M4:** throughput do consumer + CPU/mem em host remoto.  
  - **M3:** I/O no DB destino e custos de rede.

## 7.3 Encaixe na stack Grafana (coleta e visualização)

### Coleta (Prometheus)
- **Prometheus** coleta métricas via `scrape_configs` definidos na configuração do servidor.  
  Referência: [Prometheus configuration](https://prometheus.io/docs/prometheus/latest/configuration/configuration/)
- **Aplicações** expõem `/metrics` no formato Prometheus.  
  Referência: [Exposition formats](https://prometheus.io/docs/instrumenting/exposition_formats/)
- **Postgres:** usar `postgres_exporter` para expor `pg_stat_*` ao Prometheus.  
  Referência: [postgres_exporter](https://github.com/prometheus-community/postgres_exporter)
- **Kafka:** Kafka expõe métricas via JMX; usar `jmx_exporter` para publicar em `/metrics`.  
  Referências: [Kafka monitoring (JMX)](https://kafka.apache.org/38/documentation/#monitoring), [jmx_exporter](https://github.com/prometheus/jmx_exporter)
- **Hosts (DB e consumer):** usar `node_exporter` para CPU/mem/disk.  
  Referência: [node_exporter](https://prometheus.io/docs/guides/node-exporter/)
- **Logs (correlação):** enviar logs para Loki e correlacionar com métricas quando necessário.  
  Referências: [Loki data source](https://grafana.com/docs/grafana/latest/datasources/loki/), [Logs in Explore](https://grafana.com/docs/grafana/latest/explore/logs-integration/)

### Visualização (Grafana)
- **Datasource Prometheus** para métricas (dashboards de latência, throughput, carga e lag).  
  Referências: [Grafana Prometheus datasource](https://grafana.com/docs/grafana/latest/datasources/prometheus/), [Grafana for Prometheus](https://prometheus.io/docs/visualization/grafana/)
- **Datasource Loki** para logs e correlação de incidentes.  
  Referência: [Grafana Loki datasource](https://grafana.com/docs/grafana/latest/datasources/loki/)
- **Painéis recomendados (por métrica):**  
  - **M1/M5:** séries temporais e percentis de latência/lag.  
  - **M2:** taxa de eventos (consumo e escrita no DB).  
  - **M3/M4:** CPU/mem/IO por host e por serviço.  
  - **M6:** tabela simples com componentes, pontos de falha e tarefas de operação.
