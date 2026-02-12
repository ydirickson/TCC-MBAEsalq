# Resultado - Replicacao Batch - C2A1

## Identificacao
- Cenario: `C2A1`
- Script: `monitoramento/k6/scripts/replication-batch-latency.js`
- Data da execucao: `nao informada no log`

## Configuracao Executada
- `BULK_PESSOA_CREATE_COUNT=100`
- `BULK_PESSOA_UPDATE_COUNT=100`
- `BULK_ENABLE_VINCULO=true`
- `BULK_ITEM_TIMEOUT_MS=60000`
- `BULK_POLL_INTERVAL_MS=1000`

## Fase 1 - Carga de Escrita
- Pessoas novas solicitadas: `100`
- Atualizacoes de pessoa solicitadas: `100`
- Criar vinculo para cada pessoa nova: `true`
- Turma selecionada para vinculo: `202401ADM`

### Tabela de Escrita
| OPERACAO | SOLICITADO | SUCESSO | FALHA | PULADO |
| --- | --- | --- | --- | --- |
| PESSOA_CREATE | 100 | 100 | 0 | 0 |
| VINCULO_CREATE | 100 | 100 | 0 | 0 |
| PESSOA_UPDATE | 100 | 100 | 0 | 0 |

## Fase 2 - Validacao de Replicacao
- Checks pendentes para validar: `500`
- Resultado da validacao: `500/500` resolvidos no ciclo 1

### Tabela de Replicacao e Latencia
| ENTIDADE | ACAO | ORIGEM | DESTINO | QUANTIDADE | SUCESSO | FALHA | P50_MS | P95_MS | P99_MS | MAX_MS |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PESSOA | CREATE | GRADUACAO | ASSINATURA | 100 | 100 | 0 | 9854 | 11748 | 11970 | 11998 |
| PESSOA | CREATE | GRADUACAO | DIPLOMAS | 100 | 100 | 0 | 9845 | 11737 | 11964 | 11991 |
| PESSOA | CREATE | GRADUACAO | POSGRADUACAO | 100 | 100 | 0 | 9838 | 11730 | 11959 | 11985 |
| PESSOA | UPDATE | GRADUACAO | DIPLOMAS | 100 | 100 | 0 | 11705 | 11969 | 12004 | 12012 |
| VINCULO | CREATE | GRADUACAO | DIPLOMAS | 100 | 100 | 0 | 9906 | 11794 | 11991 | 12040 |

## Metricas Agregadas
- Replicacoes bem-sucedidas: `500`
- Replicacoes falhadas: `0`
- Latencia media: `9854.48ms`
- Latencia P95: `11919.25ms`
- Latencia P99: `0.00ms`

## Nota Metodologica Sobre Latencia (usar em comparacao)
- A latencia deste teste representa **tempo ate confirmacao pelo script**, nao apenas tempo de replicacao de trigger/evento.
- O valor e calculado a partir de `originAt` na escrita ate o instante em que a validacao passa na fase 2.
- Como a fase 2 varre a fila de checks em lote, parte relevante da latencia inclui espera de fila e custo de varredura.
- Portanto, para comparar cenarios, usar a mesma configuracao e priorizar **comparacao relativa** entre os resultados.

## Comparacao Objetiva com C1
| INDICADOR | C1 | C2A1 | DELTA |
| --- | --- | --- | --- |
| Replicacoes com sucesso | 500 | 500 | 0 |
| Replicacoes com falha | 0 | 0 | 0 |
| Latencia media | 8602.76ms | 9854.48ms | +14.55% |
| Latencia P95 | 10407.05ms | 11919.25ms | +14.53% |
| Faixa de MAX_MS | 10461-10492 | 11985-12040 | +~1.5s |

## Resumo Comparavel (C2A1)
| INDICADOR | VALOR |
| --- | --- |
| Escritas totais solicitadas | 300 |
| Escritas totais com sucesso | 300 |
| Checks de replicacao | 500 |
| Checks de replicacao com sucesso | 500 |
| Checks de replicacao com falha | 0 |
| Faixa de MAX_MS observada | 11985-12040 |
