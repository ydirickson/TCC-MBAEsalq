# k6 – Guia rápido de uso

Este guia descreve os scripts de carga e como executar cenários no k6.

## Estrutura
- Scripts: `monitoramento/k6/scripts/`
- Config de endpoints: `monitoramento/k6/configs/endpoints.json`
- Helpers: `monitoramento/k6/helpers/`

## Pré-requisitos
- Serviços e banco em execução (compose do cenário)
- `k6` instalado

## Scripts disponíveis
### 1) hello-world.js
Healthcheck dos serviços (GET em `/actuator/health`).
```bash
k6 run monitoramento/k6/scripts/hello-world.js
```

### 2) business-endpoints.js
GETs simples nos endpoints de negócio definidos em `endpoints.json`.
```bash
k6 run monitoramento/k6/scripts/business-endpoints.js
```

### 3) graduacao-crud.js
Fluxo completo (CRUD) do serviço de graduação, cria dados e valida GETs.
```bash
k6 run monitoramento/k6/scripts/graduacao-crud.js
```

### 4) pos-graduacao-crud.js
Fluxo completo (CRUD) do serviço de pós-graduação, cria dados e valida GETs.
```bash
k6 run monitoramento/k6/scripts/pos-graduacao-crud.js
```

### 5) cenario1-integrado.js
Executa os dois fluxos de CRUD (graduação + pós-graduação) e valida replicação no Cenário 1 (sem Kafka), com modo configurável (`off`, `sampled`, `strict`).
```bash
K6_EXECUTION_MODE=ramping-vus TEST_PROFILE=medio \
REPLICATION_MODE=sampled REPLICATION_SAMPLE_RATE=0.2 \
k6 run --out experimental-prometheus-rw=http://localhost:9090/api/v1/write \
  monitoramento/k6/scripts/cenario1-integrado.js
```

## Configuração via .env
O `graduacao-crud.js` lê `.env` na raiz do projeto (ou caminho especificado em `ENV_FILE`).

Exemplo:
```env
ENV_FILE=.env.pesado
```

## Variáveis de Configuração

### Obrigatórias
- **K6_EXECUTION_MODE**: Modo de execução do k6 (padrão: `constant-vus`)
  - Valores: `constant-vus`, `ramping-vus`, `constant-arrival-rate`, `ramping-arrival-rate`

### Opcionais com Padrões
- **GRADUACAO_BASE_URL**: URL do serviço de graduação (padrão: `http://localhost:8081`)
- **POS_GRADUACAO_BASE_URL**: URL do serviço de pós-graduação (padrão: `http://localhost:8082`)
- **RUN_ID**: Identificador da execução (padrão: `run_<timestamp>`)
- **SCENARIO**: Nome do cenário (padrão: `graduacao-crud` ou `pos-graduacao-crud`)
- **SLEEP_S**: Pausa entre iterações em segundos (padrão: `1`)
- **ENV_FILE**: Caminho do arquivo .env (padrão: `.env`)
- **REPLICATION_MODE**: Controle da validação de replicação no script integrado (`off`, `sampled`, `strict`; padrão: `sampled`)
- **REPLICATION_SAMPLE_RATE**: Taxa de amostragem quando `REPLICATION_MODE=sampled` (0 a 1; padrão: `0.2`)
- **REPLICATION_TIMEOUT_MS**: Timeout máximo para confirmação de replicação (padrão: `30000`)
- **POLL_INTERVAL_MS**: Intervalo entre tentativas de polling da replicação (padrão: `500`)

### Thresholds (Limites de Qualidade)
- **K6_HTTP_REQ_FAILED**: Taxa de falhas HTTP (padrão: `rate<0.01` = menos de 1%)
- **K6_HTTP_REQ_DURATION**: Tempo de resposta P95 (padrão: `p(95)<1200` = 1200ms)
- **K6_CHECKS**: Taxa de sucesso das validações (padrão: `rate>0.95` = acima de 95%)

## Execution mode (obrigatório)
Defina `K6_EXECUTION_MODE` no `.env` ou via linha de comando.

Valores suportados:
- `constant-vus` - VUs fixos por duração
- `ramping-vus` - Rampa de VUs por estágios
- `constant-arrival-rate` - Taxa fixa de requisições/segundo
- `ramping-arrival-rate` - Rampa de taxa de requisições

### A) constant-vus (VUs Fixos)
Manutém número constante de usuários virtuais.

**Variáveis:**
- `VUS` (padrão: `1`) - Número de usuários virtuais
- `DURATION` (padrão: `30s`) - Duração do teste

```env
K6_EXECUTION_MODE=constant-vus
VUS=5
DURATION=30s
```

### B) ramping-vus (Rampa de VUs)
Aumenta/diminui VUs em estágios. Usa perfis predefinidos.

**Variáveis:**
- `TEST_PROFILE` (padrão: `leve`) - Perfil de carga

**Perfis disponíveis:**
- `leve` - 2 VUs por 20s, mantém por 40s, desce em 10s
- `medio` - Sobe de 5 para 10 VUs em estágios (total ~2min)
- `pesado` - Sobe de 10 para 50 VUs em estágios (total ~3min)

```env
K6_EXECUTION_MODE=ramping-vus
TEST_PROFILE=medio
```

### C) ramping-arrival-rate (Taxa de Requisições em Rampa)
Controla iterações/segundo em estágios. Usa perfis predefinidos.

**Variáveis:**
- `K6_ARRIVAL_PROFILE` (padrão: `leve`) - Perfil de taxa

**Perfis disponíveis:**
- `leve` - startRate: 5, rampeia até 20 req/s, 20 VUs pré-alocados, max 80 VUs
- `medio` - startRate: 10, rampeia até 80 req/s, 50 VUs pré-alocados, max 150 VUs  
- `pesado` - startRate: 20, rampeia até 200 req/s, 100 VUs pré-alocados, max 300 VUs

```env
K6_EXECUTION_MODE=ramping-arrival-rate
K6_ARRIVAL_PROFILE=medio
```

**Customização avançada** (sobrescreve perfil):
- `K6_ARRIVAL_START_RATE` - Taxa inicial de requisições/segundo
- `K6_ARRIVAL_STAGES` - Estágios customizados (JSON array)
- `K6_ARRIVAL_PREALLOCATED_VUS` - VUs pré-alocados
- `K6_ARRIVAL_MAX_VUS` - Máximo de VUs
- `K6_ARRIVAL_TIME_UNIT` (padrão: `1s`) - Unidade de tempo

### D) constant-arrival-rate (Taxa Fixa de Requisições)
Manutém taxa constante de iterações/segundo.

**Variáveis:**
- `K6_ARRIVAL_PROFILE` (padrão: `leve`) - Perfil base para VUs
- `K6_ARRIVAL_RATE` - Taxa de requisições/segundo (sobrescreve perfil)
- `K6_ARRIVAL_DURATION` - Duração do teste (sobrescreve perfil)

```env
K6_EXECUTION_MODE=constant-arrival-rate
K6_ARRIVAL_PROFILE=medio
K6_ARRIVAL_RATE=50
K6_ARRIVAL_DURATION=4m
```

## Observações
- `graduacao-crud.js` e `pos-graduacao-crud.js` criam dados a cada iteração e não fazem cleanup.
- `cenario1-integrado.js` aumenta bastante o volume de escrita; use `REPLICATION_MODE=off` para medir só carga e `sampled/strict` para medir carga + consistência.
- Para cargas altas, acompanhe o crescimento do banco.
- Use `RUN_ID` para correlacionar requisições e logs.
