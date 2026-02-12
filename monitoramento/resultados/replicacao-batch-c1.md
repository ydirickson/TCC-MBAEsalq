# Resultado - Replicacao Batch - C1

## Identificacao
- Cenario: `C1`
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
| PESSOA | CREATE | GRADUACAO | ASSINATURA | 100 | 100 | 0 | 8411 | 10301 | 10443 | 10475 |
| PESSOA | CREATE | GRADUACAO | DIPLOMAS | 100 | 100 | 0 | 8405 | 10294 | 10436 | 10468 |
| PESSOA | CREATE | GRADUACAO | POSGRADUACAO | 100 | 100 | 0 | 8399 | 10289 | 10430 | 10461 |
| PESSOA | UPDATE | GRADUACAO | DIPLOMAS | 100 | 100 | 0 | 10251 | 10451 | 10469 | 10473 |
| VINCULO | CREATE | GRADUACAO | DIPLOMAS | 100 | 100 | 0 | 8441 | 10326 | 10463 | 10492 |

## Metricas Agregadas
- Replicacoes bem-sucedidas: `500`
- Replicacoes falhadas: `0`
- Latencia media: `8602.76ms`
- Latencia P95: `10407.05ms`
- Latencia P99: `0.00ms`

## Nota Metodologica Sobre Latencia (usar nos proximos cenarios)
- A latencia deste teste representa **tempo ate confirmacao pelo script**, nao apenas tempo de replicacao de trigger/evento.
- O valor e calculado a partir de `originAt` na escrita ate o instante em que a validacao passa na fase 2.
- Como a fase 2 varre a fila de checks em lote, parte relevante da latencia inclui espera de fila e custo de varredura.
- Neste C1, o padrao `8-10s` indica principalmente overhead da metodologia de medicao (fila + varredura), e nao atraso puro de trigger no banco.
- Para `C2A1` e `C2A2`, interpretar com a mesma regra e comparar principalmente **diferenca relativa entre cenarios**.

## Resumo Comparavel (C1)
| INDICADOR | VALOR |
| --- | --- |
| Escritas totais solicitadas | 300 |
| Escritas totais com sucesso | 300 |
| Checks de replicacao | 500 |
| Checks de replicacao com sucesso | 500 |
| Checks de replicacao com falha | 0 |
| Faixa de MAX_MS observada | 10461-10492 |
