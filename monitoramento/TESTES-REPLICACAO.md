# Testes de Integração de Replicação

Este documento descreve a estratégia e os mecanismos de teste que garantem que a replicação de dados entre serviços está funcionando corretamente, independentemente do mecanismo utilizado (Triggers/Procedures ou Kafka+CDC).

## Visão Geral

O projeto TCC-MBAEsalq implementa diferentes cenários de replicação de dados:

1. **Cenário 1 (Simples)**: Mesmo BD, mesmo schema - Triggers/Procedures
2. **Cenário 2 (Schema)**: Mesmo BD, schemas distintos - Kafka + CDC
3. **Cenário 3 (Databases)**: Bancos distintos, mesmo servidor - Kafka + CDC
4. **Cenário 4 (Servers)**: Bancos em servidores diferentes - Kafka + CDC

Os testes de replicação garantem que, independentemente do cenário, os dados são corretamente replicados entre os serviços.

## Estratégia de Testes

Scripts k6 que testam o fluxo completo de replicação fazendo requisições HTTP reais aos serviços.

**Vantagens:**
- Testam a arquitetura completa (sem mocks)
- Fáceis de executar e integrar em CI/CD
- Fornecem métricas de latência e throughput
- Independentes da linguagem/framework
- Funcionam para qualquer mecanismo de replicação (Triggers ou Kafka+CDC)

**Arquivo:** [`monitoramento/k6/scripts/replication-tests.js`](../monitoramento/k6/scripts/replication-tests.js)

#### Como executar:

```bash
# Com todos os serviços rodando (exemplo C1 — ajuste conforme o cenário)
docker compose --env-file .env.c1 -f compose/base.yml -f compose/db.c1.yml -f compose/services.yml up -d

# Executar testes de replicação
k6 run monitoramento/k6/scripts/replication-tests.js

# Com configurações customizadas
REPLICATION_TIMEOUT_MS=60000 \
POLL_INTERVAL_MS=1000 \
k6 run monitoramento/k6/scripts/replication-tests.js

# Enviando métricas para Prometheus
k6 run --out experimental-prometheus-rw \
  monitoramento/k6/scripts/replication-tests.js
```

#### Variáveis de ambiente:

- `GRADUACAO_URL`: URL do serviço de Graduação (padrão: `http://localhost:8081`)
- `POS_GRADUACAO_URL`: URL do serviço de Pós-Graduação (padrão: `http://localhost:8082`)
- `DIPLOMAS_URL`: URL do serviço de Diplomas (padrão: `http://localhost:8083`)
- `ASSINATURA_URL`: URL do serviço de Assinatura (padrão: `http://localhost:8084`)
- `REPLICATION_TIMEOUT_MS`: Timeout máximo para aguardar replicação (padrão: `30000`)
- `POLL_INTERVAL_MS`: Intervalo entre tentativas de verificação (padrão: `500`)

#### Testes executados:

1. **Teste 1: Replicação de Pessoa**
   - Cria uma Pessoa em Graduação
   - Verifica se foi replicada para Pós-Graduação, Diplomas e Assinatura
   - Valida que os dados essenciais são consistentes

2. **Teste 2: Replicação de VínculoAcademico**
   - Cria um Aluno (que gera VínculoAcademico) em Graduação
   - Verifica se o vínculo foi replicado para Diplomas

3. **Teste 3: Replicação de Atualização**
   - Atualiza uma Pessoa em Graduação (nomeSocial)
   - Verifica se a atualização foi replicada para Diplomas

## Mecanismo de Polling

Ambas as abordagens utilizam um mecanismo de **polling com timeout** para aguardar a replicação:

1. **Criar recurso** no serviço de origem (ex: Pessoa em Graduação)
2. **Polling periódico** no serviço de destino:
   - Faz requisições GET para verificar se o recurso foi replicado
   - Valida se os dados são consistentes
   - Aguarda com intervalo configurável entre tentativas
3. **Timeout configurável** - se o recurso não for replicado dentro do prazo, o teste falha
4. **Métricas de latência** - registra quanto tempo levou para a replicação

## Adaptação para Diferentes Cenários

### Cenário 1 (Simples - Triggers/Procedures)

**Latência esperada:** < 100ms (mesma transação ou muito próximo)

```bash
# Executar com timeout menor
REPLICATION_TIMEOUT_MS=5000 k6 run monitoramento/k6/scripts/replication-tests.js
```

### Cenários 2-4 (Kafka + CDC)

**Latência esperada:** 100ms - 5s (dependendo do lag do consumer e CDC)

```bash
# Executar com timeout maior
REPLICATION_TIMEOUT_MS=30000 \
POLL_INTERVAL_MS=1000 \
k6 run monitoramento/k6/scripts/replication-tests.js
```

## Métricas Coletadas

### k6 Metrics

- `replication_latency_ms` (Trend): Latência de replicação em millisegundos
- `replication_success_total` (Counter): Número de replicações bem-sucedidas
- `replication_failure_total` (Counter): Número de replicações falhadas
- `http_req_duration` (Trend): Duração das requisições HTTP
- `checks` (Rate): Taxa de sucesso das validações

### Visualização no Grafana

As métricas podem ser enviadas para Prometheus e visualizadas no Grafana:

```bash
# Executar k6 com output para Prometheus
k6 run --out experimental-prometheus-rw \
  monitoramento/k6/scripts/replication-tests.js
```

Criar dashboard no Grafana com:
- Latência P50, P95, P99 de replicação por cenário
- Taxa de sucesso/falha de replicação
- Número de tentativas necessárias até confirmação

## Integração com CI/CD

### GitHub Actions / GitLab CI

```yaml
name: Replication Integration Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Start services (exemplo C1)
        run: docker compose --env-file .env.c1 -f compose/base.yml -f compose/db.c1.yml -f compose/services.yml up -d
      
      - name: Wait for services to be healthy
        run: |
          timeout 60 bash -c 'until docker compose --env-file .env.c1 -f compose/base.yml -f compose/db.c1.yml -f compose/services.yml ps | grep healthy; do sleep 2; done'
      
      - name: Run k6 replication tests
        uses: grafana/k6-action@v0.3.0
        with:
          filename: monitoramento/k6/scripts/replication-tests.js
        env:
          REPLICATION_TIMEOUT_MS: 30000
      
      - name: Cleanup
        if: always()
        run: docker compose --env-file .env.c1 -f compose/base.yml -f compose/db.c1.yml -f compose/services.yml down -v
```

## Boas Práticas

1. **Sempre rodar com todos os serviços ativos** - Os testes precisam dos 4 serviços rodando
2. **Configurar timeouts adequados ao cenário** - Triggers são mais rápidos que Kafka
3. **Validar dados essenciais, não todos os campos** - Foco em consistência de negócio
4. **Usar IDs únicos nos testes** - Evitar conflitos em execuções paralelas
5. **Limpar dados após testes (opcional)** - Em ambientes de desenvolvimento
6. **Monitorar métricas de replicação** - Alertar se latência aumentar significativamente

## Extensão dos Testes

Para adicionar novos testes de replicação, adicione uma nova função de teste em [`replication-tests.js`](../monitoramento/k6/scripts/replication-tests.js):

```javascript
function testNovaEntidadeReplication() {
  console.log('\n=== Teste X: Replicação de NovaEntidade ===');
  
  // 1. Criar entidade no serviço de origem
  const createResponse = http.post(
    `${BASE_URLS.graduacao}/nova-entidade`,
    JSON.stringify({...}),
    { headers: { 'Content-Type': 'application/json' } }
  );
  
  const entidade = JSON.parse(createResponse.body);
  
  // 2. Aguardar replicação
  const result = waitForReplication(
    `${BASE_URLS.diplomas}/nova-entidade/${entidade.id}`,
    (data) => data.id === entidade.id && data.campo === entidade.campo
  );
  
  // 3. Registrar métricas
  replicationLatency.add(result.latency);
  if (result.success) {
    replicationSuccess.add(1);
  } else {
    replicationFailure.add(1);
  }
}
```

## Troubleshooting

### Teste falha com timeout

1. Verifique se todos os serviços estão rodando: `docker compose ps`
2. Verifique logs dos serviços: `docker compose logs graduacao diplomas`
3. Aumente o timeout: `REPLICATION_TIMEOUT_MS=60000`
4. Verifique se o mecanismo de replicação está ativo (triggers ou Kafka consumers)

### Replicação não acontece

1. **Cenário 1 (Triggers):** Verifique se os scripts `*_sync.sql` foram executados
2. **Cenários 2-4 (Kafka):** Verifique se o Kafka está rodando e os consumers estão ativos
3. Verifique logs de erro no serviço de origem ao criar o recurso
4. Teste manualmente com curl/httpie para isolar o problema

### Latência muito alta

1. Compare com métricas esperadas para o cenário (ver seção "Adaptação para Diferentes Cenários")
2. Verifique carga no banco de dados e no Kafka
3. Verifique se há lag nos consumers Kafka: `kafka-consumer-groups --describe`
4. Analise métricas no Prometheus/Grafana para identificar gargalos

## Referências

- [Documentação k6](https://k6.io/docs/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Planejamento: Métricas](../planejamento/metricas.md)
- [Planejamento: Regras de Replicação](../planejamento/regras-replicacao.md)
