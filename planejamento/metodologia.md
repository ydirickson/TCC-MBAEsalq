# 6.2 Metodologia comparativa (execucao e analise)
[← Voltar ao indice](./README.md)

Este documento define o metodo de comparacao entre arquiteturas de replicacao, mantendo alinhamento com:
- cenarios de infraestrutura em [`replicacao-cenarios.md`](./replicacao-cenarios.md);
- tipos de arquitetura em [`arquiteturas.md`](./arquiteturas.md);
- metricas M1-M8 em [`metricas.md`](./metricas.md).

## 6.2.1 Pergunta e hipotese

- **Pergunta principal:** em cada cenario (1-4), qual arquitetura (DB Based, CDC+Kafka, EDA+Kafka) entrega melhor equilibrio entre confiabilidade, desempenho e operacao?
- **Hipotese:** arquiteturas com Kafka (CDC+Kafka e EDA+Kafka) podem manter confiabilidade equivalente ao DB Based, com ganho de desacoplamento e escalabilidade, ao custo de maior complexidade operacional.

## 6.2.2 Delineamento experimental

### Fatores (variaveis independentes)
- **Arquitetura:** DB Based, CDC+Kafka, EDA+Kafka.
- **Cenario:** 1) Simples, 2) Schema, 3) Databases, 4) Servers.
- **Carga:** perfil leve, medio e pesado (k6).

### Respostas (variaveis dependentes)
- **M1-M8** conforme definicao em [`metricas.md`](./metricas.md).

### Controles (para comparabilidade)
- Mesmo fluxo funcional de negocio (Pessoa → Vinculo → Diploma → Assinatura).
- Mesmo dataset inicial e mesma massa gerada por rodada.
- Mesmo perfil de carga por comparacao.
- Mesmo tempo de teste e janela de observacao por rodada.
- Mesma versao de codigo por bloco de execucao.

## 6.2.3 Regra de comparacao (ordem obrigatoria)

1. **Comparacao primaria:** fixar um cenario e comparar as 3 arquiteturas.
2. **Comparacao secundaria:** repetir para todos os cenarios.
3. **Leitura transversal:** so depois comparar efeito de infraestrutura (cenario 1 → 4).

Isso evita misturar dois fatores ao mesmo tempo (arquitetura e infraestrutura).

## 6.2.4 Matriz minima de execucao

| Eixo | Valores |
| --- | --- |
| Arquiteturas | 3 (DB Based, CDC+Kafka, EDA+Kafka) |
| Cenarios | 4 (1-4) |
| Perfis de carga | 3 (leve, medio, pesado) |
| Repeticoes por combinacao | `R` (recomendado: 3) |

**Total de execucoes:** `3 * 4 * 3 * R` (com `R=3`, total = `108` rodadas).

## 6.2.5 Protocolo de execucao por rodada

1. Subir stack do cenario + arquitetura alvo (`docker compose up -d`).
2. Validar saude basica dos servicos (`/actuator/health`).
3. Rodar aquecimento curto (warm-up) sem coleta final.
4. Rodar teste oficial de carga com `RUN_ID` unico.
5. Coletar metricas no Prometheus/Grafana e reconciliacao SQL.
6. Registrar resultado da rodada em planilha/tabela padrao.
7. Limpar estado para proxima rodada (ou recriar ambiente).

## 6.2.6 Padrao de identificacao das rodadas

Use `RUN_ID` padronizado para rastrear cada execucao:

```text
run_<cenario>_<arquitetura>_<perfil>_r<rep>
```

Exemplo:

```text
run_c2_cdc_medio_r02
```

## 6.2.7 Coleta por metrica (M1-M8)

| Metrica | Fonte principal | Medida base da comparacao |
| --- | --- | --- |
| M1 Latencia ponta-a-ponta | k6 + Prometheus | media, p95, p99 |
| M2 Throughput | k6 + Prometheus | eventos/s processados |
| M3 Taxa de erro/perda | k6 + consumidor/Kafka | `falhas / (sucessos + falhas)` |
| M4 Consistencia | checks k6 + SQL reconciliacao | divergencia funcional por entidade critica |
| M5 Lag/staleness | Kafka/consumidor + k6 | atraso em ms e/ou backlog de eventos |
| M6 Recursos | Actuator + postgres_exporter (+ JMX) | CPU, memoria, IO, conexoes |
| M7 Disponibilidade | Prometheus (`up`) | `% uptime` por janela |
| M8 Complexidade operacional | inventario tecnico | componentes, passos manuais, pontos de falha |

Notas:
- Para cenario DB Based (sem Kafka), usar M1 como proxy principal de M5.
- Para CDC+Kafka e EDA+Kafka, incluir lag de consumidor, retries e DLQ na leitura de M3/M5/M7.

## 6.2.8 Comandos-base de referencia

Exemplo de rodada no cenario 1 (script integrado atual):

```bash
K6_EXECUTION_MODE=ramping-vus TEST_PROFILE=medio \
RUN_ID=run_c1_db_medio_r01 REPLICATION_MODE=sampled \
k6 run --out experimental-prometheus-rw=http://localhost:9090/api/v1/write \
  monitoramento/k6/scripts/cenario1-integrado.js
```

Para cenarios/arquiteturas seguintes, manter o mesmo padrao de carga e trocar apenas o stack alvo.

## 6.2.9 Template de registro por rodada

| RUN_ID | Cenario | Arquitetura | Perfil | M1 p95 (ms) | M2 (ev/s) | M3 (%) | M4 (%) | M5 (ms/eventos) | M6 (resumo) | M7 (%) | M8 (score) | Observacoes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [preencher] | [1-4] | [DB/CDC/EDA] | [leve/medio/pesado] | [x] | [x] | [x] | [x] | [x] | [x] | [x] | [x] | [texto curto] |

## 6.2.10 Template de comparacao por cenario

Preencher uma tabela por cenario, comparando lado a lado:

| Cenario X | DB Based | CDC+Kafka | EDA+Kafka | Melhor no cenario |
| --- | --- | --- | --- | --- |
| Confiabilidade (M3+M4) | [resultado] | [resultado] | [resultado] | [DB/CDC/EDA] |
| Desempenho (M1+M2+M5) | [resultado] | [resultado] | [resultado] | [DB/CDC/EDA] |
| Resiliencia (M7) | [resultado] | [resultado] | [resultado] | [DB/CDC/EDA] |
| Operacao (M6+M8) | [resultado] | [resultado] | [resultado] | [DB/CDC/EDA] |
| Decisao final do cenario | [texto] | [texto] | [texto] | [arquitetura escolhida] |

## 6.2.11 Regra de decisao consolidada

Usar prioridade fixa para conclusao:
1. Confiabilidade de dados (**M3 + M4**)
2. Desempenho (**M1 + M2 + M5**)
3. Resiliencia (**M7**)
4. Eficiencia e operacao (**M6 + M8**)

Se uma arquitetura falhar em confiabilidade (M3/M4), ela nao pode ser vencedora global, mesmo com melhor throughput.

## 6.2.12 Ameacas a validade (checklist rapido)

- Variacao de ambiente (maquina, recursos, concorrencia externa).
- Diferenca de massa de dados entre rodadas.
- Ordem de execucao influenciando cache/estado.
- Comparacao injusta por configuracoes diferentes de carga.
- Janela de observacao curta para eventos assincronos.

Registrar esses pontos em cada rodada para sustentar a analise final.
