# 4. Metodologia comparativa (execucao e analise)
[← Voltar ao indice](./README.md)

Este documento define o metodo de comparacao entre arquiteturas de replicacao, mantendo alinhamento com:
- cenarios, regras e tipos de arquitetura em [`regras-replicacao.md`](./regras-replicacao.md);
- metricas M1-M8 em [`metricas.md`](./metricas.md).

## 4.1 Pergunta e hipotese

- **Pergunta principal:** nos cenarios comparativos (2-4), qual arquitetura (DB Based, CDC+Kafka, EDA+Kafka) entrega melhor equilibrio entre confiabilidade, desempenho e operacao?
- **Hipotese:** arquiteturas com Kafka (CDC+Kafka e EDA+Kafka) podem manter confiabilidade equivalente ao DB Based, com ganho de desacoplamento e escalabilidade, ao custo de maior complexidade operacional.

## 4.2 Delineamento experimental

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

## 4.3 Regra de comparacao (ordem obrigatoria)

1. **Baseline de referencia:** executar C1A1 (DB Based) para calibrar ambiente e coleta.
2. **Comparacao primaria:** fixar um cenario comparativo (C2, C3 ou C4) e comparar as 3 arquiteturas.
3. **Comparacao secundaria:** repetir para os demais cenarios comparativos (C2-C4).
4. **Leitura transversal:** so depois comparar efeito de infraestrutura (C2 → C4), usando C1A1 como referencia inicial.

Isso evita misturar dois fatores ao mesmo tempo (arquitetura e infraestrutura).

**Observacao do experimento:** o cenario 1 (C1) sera usado apenas como baseline de construcao (C1A1). As combinacoes C1A2 e C1A3 nao serao executadas; as comparacoes formais com 3 arquiteturas iniciam em C2–C4.

## 4.4 Matriz minima de execucao

| Eixo | Valores |
| --- | --- |
| Arquiteturas | 3 (DB Based, CDC+Kafka, EDA+Kafka) |
| Cenarios comparativos | 3 (2-4) |
| Baseline | C1A1 (sem replicacao fisica) |
| Perfis de carga | 3 (leve, medio, pesado) |
| Repeticoes por combinacao | `R` (recomendado: 3) |

**Total de execucoes (comparativas):** `3 * 3 * 3 * R` (com `R=3`, total = `81` rodadas) + baseline C1A1 (fora da comparacao).

## 4.5 Protocolo de execucao por rodada

1. Subir stack do cenario + arquitetura alvo (`docker compose up -d`).
2. Validar saude basica dos servicos (`/actuator/health`).
3. Rodar aquecimento curto (warm-up) sem coleta final.
4. Rodar teste oficial de carga com `RUN_ID` unico.
5. Coletar metricas no Prometheus/Grafana e reconciliacao SQL.
6. Registrar resultado da rodada em planilha/tabela padrao.
7. Executar reset deterministico do ambiente (dados + filas + offsets) antes da proxima rodada.

Reset minimo recomendado por rodada:
- Recriar o ambiente com compose (`down -v` / `up -d`) ou rotina equivalente documentada por cenario.
- Reaplicar seed padrao do cenario antes de iniciar nova coleta.
- Garantir que consumidor/connector inicie sem backlog residual da rodada anterior.

## 4.6 Padrao de identificacao das rodadas

Use `RUN_ID` padronizado para rastrear cada execucao:

```text
run_<cenario>_<arquitetura>_<perfil>_r<rep>
```

Exemplo:

```text
run_c2_cdc_medio_r02
```

## 4.7 Coleta por metrica (M1-M8)

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

## 4.8 Comandos-base de referencia

Exemplo de rodada no cenario 1 (baseline C1A1):

```bash
K6_EXECUTION_MODE=ramping-vus TEST_PROFILE=medio \
RUN_ID=run_c1_db_medio_r01 REPLICATION_MODE=sampled \
k6 run --out experimental-prometheus-rw=http://localhost:9090/api/v1/write \
  monitoramento/k6/scripts/replication-tests.js
```

Script oficial de comparacao: `monitoramento/k6/scripts/replication-tests.js`.
Para cenarios/arquiteturas seguintes, manter o mesmo padrao de carga e trocar apenas o stack alvo.

## 4.9 Template de registro por rodada

| RUN_ID | Cenario | Arquitetura | Perfil | M1 p95 (ms) | M2 (ev/s) | M3 (%) | M4 (%) | M5 (ms/eventos) | M6 (resumo) | M7 (%) | M8 (score) | Observacoes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [preencher] | [2-4] | [DB/CDC/EDA] | [leve/medio/pesado] | [x] | [x] | [x] | [x] | [x] | [x] | [x] | [x] | [texto curto] |

Observacao: para C1, registrar apenas DB Based (baseline) em linha separada (`run_c1_db_*`).

## 4.10 Template de comparacao por cenario

Preencher uma tabela por cenario, comparando lado a lado:

| Cenario X | DB Based | CDC+Kafka | EDA+Kafka | Melhor no cenario |
| --- | --- | --- | --- | --- |
| Confiabilidade (M3+M4) | [resultado] | [resultado] | [resultado] | [DB/CDC/EDA] |
| Desempenho (M1+M2+M5) | [resultado] | [resultado] | [resultado] | [DB/CDC/EDA] |
| Resiliencia (M7) | [resultado] | [resultado] | [resultado] | [DB/CDC/EDA] |
| Operacao (M6+M8) | [resultado] | [resultado] | [resultado] | [DB/CDC/EDA] |
| Decisao final do cenario | [texto] | [texto] | [texto] | [arquitetura escolhida] |

Observacao: esta comparacao se aplica aos cenarios 2–4. O cenario 1 e apenas baseline.

## 4.11 Regra de decisao consolidada

Usar prioridade fixa para conclusao:
1. Confiabilidade de dados (**M3 + M4**)
2. Desempenho (**M1 + M2 + M5**)
3. Resiliencia (**M7**)
4. Eficiencia e operacao (**M6 + M8**)

Se uma arquitetura falhar em confiabilidade (M3/M4), ela nao pode ser vencedora global, mesmo com melhor throughput.

Critérios minimos de aceite (default, na ausencia de SLA especifico):
- **M3 (erro/perda):** <= 1,0% por rodada.
- **M4 (consistencia):** >= 99,5% nas validacoes funcionais.
- **M7 (disponibilidade):** >= 99,0% na janela da rodada.
- **M1/M5:** P95 dentro do SLA definido para o perfil de carga.

Sem cumprir os gates de confiabilidade (M3/M4), a arquitetura fica reprovada no cenario.

## 4.12 Ameacas a validade (checklist rapido)

- Variacao de ambiente (maquina, recursos, concorrencia externa).
- Diferenca de massa de dados entre rodadas.
- Ordem de execucao influenciando cache/estado.
- Comparacao injusta por configuracoes diferentes de carga.
- Janela de observacao curta para eventos assincronos.

Registrar esses pontos em cada rodada para sustentar a analise final.

## 4.13 Tratamento estatistico e outliers

- Repeticoes por combinacao: minimo `R=3`, recomendado `R=5` quando houver variacao alta.
- Reportar por combinacao: mediana, P95 e desvio relativo entre repeticoes.
- Outlier: usar regra de IQR (`< Q1 - 1,5*IQR` ou `> Q3 + 1,5*IQR`).
- Se houver outlier: registrar causa provavel e executar 1 repeticao adicional para substituir a rodada descartada.
