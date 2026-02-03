# 6.1 Tipos de arquitetura de replicação (DB Based, CDC+Kafka, EDA+Kafka)
[← Voltar ao índice](./README.md)

Este documento define o eixo arquitetural que será comparado no TCC e conecta esse eixo aos **4 cenários de infraestrutura** e às **métricas M1-M8**.

## 6.1.1 Eixos de comparação

### A) DB Based (integração via banco)
- Integração pelo próprio PostgreSQL (triggers, procedures, jobs, views materializadas, logical replication).
- Contrato principal de integração: tabela/schema (modelo relacional compartilhado ou espelhado).
- Ponto forte: implementação direta e baixa latência local (especialmente no cenário 1).
- Risco principal: alto acoplamento estrutural entre serviços e evolução mais custosa.

### B) CDC+Kafka (captura de mudanças de dados)
- Mudanças no banco são capturadas via CDC (WAL/logical decoding) e publicadas no Kafka.
- Contrato principal: evento técnico de mudança de registro (insert/update/delete), próximo ao schema relacional.
- Ponto forte: desacopla transporte/consumo sem exigir domínio totalmente orientado a eventos.
- Risco principal: semântica ainda tabela-cêntrica; menor expressividade de negócio.

### C) EDA com Kafka (eventos de domínio)
- Serviços publicam eventos de negócio explícitos (ex.: `PessoaCriada`, `ConclusaoPublicada`, `DiplomaEmitido`) com outbox/inbox.
- Contrato principal: evento de domínio versionado e idempotente.
- Ponto forte: maior autonomia entre serviços e maior flexibilidade evolutiva.
- Risco principal: maior disciplina operacional (governança de contratos, ordering, retries, DLQ e reprocessamento).

## 6.1.2 Relação com os cenários (infraestrutura x arquitetura)

| Cenário de infraestrutura | DB Based | CDC+Kafka | EDA com Kafka | Observação prática |
| --- | --- | --- | --- | --- |
| 1) Simples (mesmo BD/schema) | Referência natural (baseline) | Viável, porém pouco natural no cenário | Viável para comparação de desacoplamento | Útil para medir custo de introduzir barramento em ambiente simples |
| 2) Schema (mesmo BD, schemas distintos) | Viável com trigger/procedure cross-schema | Muito aderente | Muito aderente | Cenário ideal para comparar transição gradual |
| 3) Databases (DBs distintos no mesmo servidor) | Viável via logical replication/jobs | Muito aderente | Muito aderente | Evidencia consistência eventual e custos operacionais |
| 4) Servers (DBs em servidores distintos) | Viável via logical replication | Muito aderente | Muito aderente | Cenário de maior estresse para resiliência e observabilidade |

## 6.1.3 Relação com as métricas M1-M8

| Arquitetura | Métricas mais sensíveis | Como interpretar no TCC |
| --- | --- | --- |
| DB Based | M1, M4, M6, M8 | Espera-se baixa latência local (M1), alta consistência imediata em cenários simples (M4), mas maior acoplamento e complexidade de evolução (M8). |
| CDC+Kafka | M1, M2, M3, M5, M7 | Espera-se aumento moderado de latência (M1/M5), ganho de throughput em escala (M2) e necessidade de observar retries/perdas (M3) e disponibilidade da cadeia CDC+broker (M7). |
| EDA com Kafka | M2, M3, M5, M7, M8 | Espera-se melhor desacoplamento e evolução (M8), alta escalabilidade (M2), com foco em idempotência e governança para manter confiabilidade (M3/M5). |

## 6.1.4 Hipóteses de comparação (para conclusão)

1. **Confiabilidade (M3 + M4):** CDC+Kafka e EDA só são aceitos se mantiverem taxa de erro/perda próxima do DB Based e consistência funcional dos dados críticos.
2. **Desempenho (M1 + M2 + M5):** aumento de latência é aceitável se houver ganho de throughput, estabilidade de cauda (P95/P99) e lag dentro do SLA definido.
3. **Operação (M6 + M7 + M8):** a adoção de Kafka deve justificar o custo operacional adicional com maior resiliência e flexibilidade arquitetural.

## 6.1.5 Regra de leitura dos resultados

- A comparação é sempre feita no formato: **(mesmo cenário) + (arquiteturas diferentes)**.
- Exemplo: Cenário 2 comparado em DB Based vs CDC+Kafka vs EDA.
- Só depois é feita leitura transversal entre cenários (1→4) para avaliar efeito de distribuição física da infraestrutura.
