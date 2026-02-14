# 7. Pontos em aberto para decisão (para próxima iteração)
[← Voltar ao índice](./README.md)

- Definir **ownership final de Pessoa** (grad, pós, ou ambos com regras)
- Definir **campos mínimos replicados** de Pessoa (evitar excesso)
- Definir **estado de conclusão** (contrato: “elegível para diploma”)
- Definir **modelo de documento e assinatura** (hash, versões, reemissão)
- Definir **estratégia de resolução de conflito** (se Pessoa puder ser alterada em mais de um serviço)

## Métricas-alvo (simples de coletar)
- Estabilidade: uptime dos serviços, taxa de erro HTTP (4xx/5xx) e de consumo Kafka, dead-letter count.
- Desempenho: latência p99 ponta-a-ponta (evento origem → persistência destino), throughput (eventos/s), tempo médio de replicação por serviço.
- Consistência: divergência temporária (quantidade de registros desatualizados por janela), taxa de retrabalho/idempotência (reprocessamentos), atraso de replicação (lag em bytes/offsets).
- Recursos: CPU/memória dos serviços e bancos, IO do banco durante carga, tamanho de fila/tópico.

## Referências rápidas
- *Designing Data-Intensive Applications* (Kleppmann) — capítulos de replicação e filas para métricas de latência/consistência.
- *Database Reliability Engineering* (Laine Campbell, Charity Majors) — indicadores operacionais simples para bancos e replicação.
- Documentação Apache Kafka — seções de monitoring/lag (Kafka Consumer Lag, UnderReplicatedPartitions): https://kafka.apache.org/documentation/#monitoring
- Blog do Confluent — idempotência/exactly-once e métricas de consumo: https://www.confluent.io/blog/kafka-fastest-messaging-system/ e https://www.confluent.io/blog/enabling-exactly-once-kafka-streams/
