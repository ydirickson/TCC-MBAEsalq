# Resultado - Replicacao Batch - C2A2

## Identificacao
- Cenario: `C2A2`
- Script: `monitoramento/k6/scripts/replication-batch-latency.js`
- Data da execucao: `nao informada no log`
- Duracao total observada: `01m05.6s`

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
- Evolucao relevante:
  - ciclo 1: `400/500` resolvidos
  - ciclos 10/20/30: mantido em `400/500`
  - ciclo 35: `500/500` resolvidos

### Tabela de Replicacao e Latencia
| ENTIDADE | ACAO | ORIGEM | DESTINO | QUANTIDADE | SUCESSO | FALHA | P50_MS | P95_MS | P99_MS | MAX_MS |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PESSOA | CREATE | GRADUACAO | ASSINATURA | 100 | 100 | 0 | 7402 | 8832 | 8897 | 8913 |
| PESSOA | CREATE | GRADUACAO | DIPLOMAS | 100 | 100 | 0 | 7393 | 8826 | 8891 | 8907 |
| PESSOA | CREATE | GRADUACAO | POSGRADUACAO | 100 | 100 | 0 | 7383 | 8819 | 8886 | 8900 |
| PESSOA | UPDATE | GRADUACAO | DIPLOMAS | 100 | 100 | 0 | 8674 | 8877 | 8897 | 8901 |
| VINCULO | CREATE | GRADUACAO | DIPLOMAS | 100 | 0 | 100 | - | - | - | - |

## Metricas Agregadas
- Replicacoes bem-sucedidas: `400`
- Replicacoes falhadas: `100`
- Latencia media: `18292.15ms`
- Latencia P95: `60771.20ms`
- Latencia P99: `0.00ms`

## Evento de Falha Registrado
- Erro final do k6: `thresholds on metrics 'checks' have been crossed`
- Check que falhou:
  - `::batch: vinculo create replicado fails=100 passes=0`
- Interpretacao:
  - Os 100 checks de `VINCULO CREATE` nao confirmaram dentro do timeout por item (`60000ms`).
  - Como o threshold global de checks exige `rate>0.95`, o teste foi marcado como falho.

## Leitura Isolada de Pessoa
- Se desconsiderar `vinculo`, o comportamento de `pessoa` foi consistente:
  - `PESSOA CREATE`: `300/300` sucesso (3 destinos x 100)
  - `PESSOA UPDATE`: `100/100` sucesso
- Faixa observada para pessoa:
  - `P50`: `7383-8674ms`
  - `P95`: `8819-8877ms`
  - `MAX`: `8900-8913ms`

## Nota Metodologica Sobre Latencia
- A latencia deste teste representa **tempo ate confirmacao pelo script**, nao apenas tempo de replicacao puro.
- O valor e calculado da escrita (`originAt`) ate a confirmacao na fase 2.
- Como houve `100` timeouts de `vinculo`, as metricas agregadas ficaram contaminadas para comparacao global.

## Resumo Comparavel (C2A2)
| INDICADOR | VALOR |
| --- | --- |
| Escritas totais solicitadas | 300 |
| Escritas totais com sucesso | 300 |
| Checks de replicacao | 500 |
| Checks de replicacao com sucesso | 400 |
| Checks de replicacao com falha | 100 |
| Resultado global de threshold | FALHOU |
