# 7. Métricas previstas (alto nível)
[← Voltar ao índice](./README.md)

Este detalhamento conecta as métricas previstas aos **cenários definidos** e descreve **coleta e visualização** na stack Grafana (Prometheus + Grafana + Loki).

## 7.1 Métricas da pesquisa (significado, unidade e relevância)

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

### 7.1.2 Referências confiáveis das medidas (fontes primárias)

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

### 7.1.3 Prioridade para decisão (centralizado x distribuído)

Para responder ao objetivo da pesquisa, a ordem de importância para comparação é:

1. **Confiabilidade de dados:** M3 + M4  
2. **Desempenho:** M1 + M2 + M5  
3. **Resiliência:** M7  
4. **Eficiência e operação:** M6 + M8

Em termos de conclusão, o ambiente distribuído só é considerado equivalente/superior quando mantém confiabilidade (M3/M4) e desempenho (M1/M2/M5) em nível comparável ao baseline centralizado.

## 7.2 Aplicação por cenário (tradicional x eventos)

Esta seção será detalhada na próxima etapa, separando as métricas por cenário (Simples, Schema, Databases e Servers), com critérios de comparação e metas de aceitação.

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
  - **M3/M4:** taxa de erro e taxa de validações de consistência aprovadas.  
  - **M6:** CPU/mem/IO por host e por serviço.  
  - **M7:** uptime e indisponibilidade por componente crítico.  
  - **M8:** tabela simples com componentes, pontos de falha e tarefas operacionais.
