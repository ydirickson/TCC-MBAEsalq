# Resultado - Replicacao Batch - <CENARIO>

## Identificacao
- Cenario: `<CENARIO>`
- Script: `monitoramento/k6/scripts/replication-batch-latency.js`
- Data da execucao: `<YYYY-MM-DD HH:mm>`

## Configuracao Executada
- `BULK_PESSOA_CREATE_COUNT=<VALOR>`
- `BULK_PESSOA_UPDATE_COUNT=<VALOR>`
- `BULK_ENABLE_VINCULO=<true|false>`
- `BULK_ITEM_TIMEOUT_MS=<VALOR>`
- `BULK_POLL_INTERVAL_MS=<VALOR>`

## Fase 1 - Carga de Escrita
- Pessoas novas solicitadas: `<VALOR>`
- Atualizacoes de pessoa solicitadas: `<VALOR>`
- Criar vinculo para cada pessoa nova: `<true|false>`
- Turma selecionada para vinculo: `<VALOR|N/A>`

### Tabela de Escrita
| OPERACAO | SOLICITADO | SUCESSO | FALHA | PULADO |
| --- | --- | --- | --- | --- |
| PESSOA_CREATE | <...> | <...> | <...> | <...> |
| VINCULO_CREATE | <...> | <...> | <...> | <...> |
| PESSOA_UPDATE | <...> | <...> | <...> | <...> |

## Fase 2 - Validacao de Replicacao
- Checks pendentes para validar: `<VALOR>`
- Resultado da validacao: `<VALOR>/<VALOR>` resolvidos no ciclo `<N>`

### Tabela de Replicacao e Latencia
| ENTIDADE | ACAO | ORIGEM | DESTINO | QUANTIDADE | SUCESSO | FALHA | P50_MS | P95_MS | P99_MS | MAX_MS |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PESSOA | CREATE | GRADUACAO | ASSINATURA | <...> | <...> | <...> | <...> | <...> | <...> | <...> |
| PESSOA | CREATE | GRADUACAO | DIPLOMAS | <...> | <...> | <...> | <...> | <...> | <...> | <...> |
| PESSOA | CREATE | GRADUACAO | POSGRADUACAO | <...> | <...> | <...> | <...> | <...> | <...> | <...> |
| PESSOA | UPDATE | GRADUACAO | DIPLOMAS | <...> | <...> | <...> | <...> | <...> | <...> | <...> |
| VINCULO | CREATE | GRADUACAO | DIPLOMAS | <...> | <...> | <...> | <...> | <...> | <...> | <...> |

## Metricas Agregadas
- Replicacoes bem-sucedidas: `<VALOR>`
- Replicacoes falhadas: `<VALOR>`
- Latencia media: `<VALOR>ms`
- Latencia P95: `<VALOR>ms`
- Latencia P99: `<VALOR>ms`

## Nota Metodologica Sobre Latencia (usar em C2A1/C2A2)
- A latencia deste teste representa **tempo ate confirmacao pelo script**, nao apenas tempo de replicacao de trigger/evento.
- O valor e calculado a partir de `originAt` na escrita ate o instante em que a validacao passa na fase 2.
- Como a fase 2 varre a fila de checks em lote, parte relevante da latencia inclui espera de fila e custo de varredura.
- Portanto, para comparar cenarios, usar a mesma configuracao e priorizar **comparacao relativa** entre os resultados.

## Resumo Comparavel (<CENARIO>)
| INDICADOR | VALOR |
| --- | --- |
| Escritas totais solicitadas | <...> |
| Escritas totais com sucesso | <...> |
| Checks de replicacao | <...> |
| Checks de replicacao com sucesso | <...> |
| Checks de replicacao com falha | <...> |
| Faixa de MAX_MS observada | <...> |
