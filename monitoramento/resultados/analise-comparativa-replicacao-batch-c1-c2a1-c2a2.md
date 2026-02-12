# Analise Comparativa - Replicacao Batch (C1, C2A1, C2A2)

## Escopo
- Script: `monitoramento/k6/scripts/replication-batch-latency.js`
- Carga aplicada em todos: `100` criacoes de pessoa + `100` atualizacoes de pessoa + `100` criacoes de vinculo
- Timeout por item: `60000ms`
- Polling fase 2: `1000ms`

## Parametros k6 Utilizados
### Execucao
- Comando: `k6 run monitoramento/k6/scripts/replication-batch-latency.js`
- `executor`: `shared-iterations`
- `vus`: `1`
- `iterations`: `1`
- `maxDuration`: `45m` (default do script via `BULK_MAX_DURATION`)

### Thresholds
- `checks`: `rate>0.95`
- `http_req_duration`: `p(95)<8000`

### Variaveis de ambiente aplicadas no teste
- `BULK_PESSOA_CREATE_COUNT=100`
- `BULK_PESSOA_UPDATE_COUNT=100`
- `BULK_ENABLE_VINCULO=true`
- `BULK_ITEM_TIMEOUT_MS=60000`
- `BULK_POLL_INTERVAL_MS=1000`

## Lista 1 - Visao Geral por Cenario
- `C1`: escritas `300/300`; checks sucesso `500/500`; checks falha `0`; latencia media `8.6s`; latencia P95 `10.4s`; threshold checks `OK`.
- `C2A1`: escritas `300/300`; checks sucesso `500/500`; checks falha `0`; latencia media `9.9s`; latencia P95 `11.9s`; threshold checks `OK`.
- `C2A2`: escritas `300/300`; checks sucesso `400/500`; checks falha `100`; latencia media `18.3s`; latencia P95 `60.8s`; threshold checks `FALHOU`.

## Lista 2 - Latencia por Entidade e Destino (P50/P95/MAX em s)
- `PESSOA | CREATE | ASSINATURA`: `C1=8.4/10.3/10.5`, `C2A1=9.9/11.7/12.0`, `C2A2=7.4/8.8/8.9`.
- `PESSOA | CREATE | DIPLOMAS`: `C1=8.4/10.3/10.5`, `C2A1=9.8/11.7/12.0`, `C2A2=7.4/8.8/8.9`.
- `PESSOA | CREATE | POSGRADUACAO`: `C1=8.4/10.3/10.5`, `C2A1=9.8/11.7/12.0`, `C2A2=7.4/8.8/8.9`.
- `PESSOA | UPDATE | DIPLOMAS`: `C1=10.3/10.5/10.5`, `C2A1=11.7/12.0/12.0`, `C2A2=8.7/8.9/8.9`.
- `VINCULO | CREATE | DIPLOMAS`: `C1=8.4/10.3/10.5`, `C2A1=9.9/11.8/12.0`, `C2A2=-/-/-`.

## Leitura Comparativa
- `C1` e `C2A1` ficaram estaveis e consistentes (`500/500` checks).
- `C2A1` ficou mais lento que `C1` no agregado (media `+14.55%`, P95 `+14.53%`).
- Em `C2A2`, os fluxos de `pessoa` ficaram consistentes (`400/400` nos checks de pessoa), mas `vinculo` falhou totalmente (`0/100`), derrubando o threshold global.
- Em `C2A2`, a latencia agregada ficou inflada por timeouts de `vinculo` (P95 ~`60s`), portanto nao e comparavel diretamente com C1/C2A1 no agregado.

## Consideracao Especifica: Problema de Vinculo no C2A2
Evidencia observada no ambiente:
- `checks` com falha: `::batch: vinculo create replicado fails=100 passes=0`
- Connectors de vinculo da graduacao com `task FAILED` (sink para `diplomas` e `assinatura`)
- Erro no sink: violacao de FK (`vinculo_academico.pessoa_id_fkey`), indicando tentativa de gravar `vinculo` antes de `pessoa` existir no destino.

Interpretacao:
- Ha alta probabilidade de corrida/ordenacao entre topicos/consumidores (`pessoa` e `vinculo` em pipelines separados), sem garantia de ordem global entre entidades relacionadas.
- Quando o sink de `vinculo` falha por FK, a task para e os eventos subsequentes nao sao aplicados, gerando os 100 timeouts de validacao.

## Ideias de Contorno
Curto prazo (operacional):
1. Pre-check obrigatorio antes do teste: validar todos os connectors/tasks em `RUNNING`; abortar execucao se houver `FAILED`.
2. Auto-recuperacao: reiniciar connectors/tasks de `vinculo` falhados antes de iniciar a carga.
3. Isolacao de diagnostico: rodar um perfil "pessoa-only" quando o objetivo for medir apenas latencia de pessoa.

Medio prazo (robustez de pipeline):
1. Tornar sink de `vinculo` tolerante a erro transitivo de FK: retries/backoff maiores e tratamento de erro com DLQ em vez de parada imediata da task.
2. Implementar etapa de reprocessamento para eventos de `vinculo` que falharem por dependencia nao atendida.
3. Reduzir risco de corrida entre entidades relacionadas: garantir chegada/processamento de `pessoa` antes de `vinculo` (por desenho de fluxo, janela de atraso controlada ou etapa intermediaria de reconciliacao).

Longo prazo (arquitetural):
1. Usar modelo de ingestao com staging sem FK forte no primeiro passo e consolidacao posterior com validacao referencial.
2. Revisar contrato/evento de dominio para reduzir dependencia temporal estrita entre topicos diferentes.

## Nota Metodologica de Latencia
- A latencia deste teste mede **tempo ate confirmacao pelo script** (escrita -> confirmacao em polling), nao latencia "pura" de replicacao.
- Para comparacao entre cenarios, manter mesma configuracao e priorizar leitura **relativa**.
