# k6 – Guia rápido de uso

Este guia descreve os scripts de carga e como executar cenários no k6.

## Estrutura
- Scripts: `monitoramento/k6/scripts/`
- Config de endpoints: `monitoramento/k6/configs/endpoints.json`
- Helpers: `monitoramento/k6/helpers/`

## Pré-requisitos
- Serviços e banco em execução (docker-compose)
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

## Configuração via .env
O `graduacao-crud.js` lê `.env` na raiz do projeto (ou `ENV_FILE`).

Exemplo:
```
ENV_FILE=.env.pesado
```

## Execution mode (obrigatório)
Defina `K6_EXECUTION_MODE` no `.env`.

Valores suportados:
- `constant-vus`
- `ramping-vus`
- `constant-arrival-rate`
- `ramping-arrival-rate`

### A) constant-vus
```env
K6_EXECUTION_MODE=constant-vus
VUS=5
DURATION=30s
```

### B) ramping-vus
Requer `TEST_PROFILE`:
```env
K6_EXECUTION_MODE=ramping-vus
TEST_PROFILE=leve
```

Profiles disponíveis:
- `leve`
- `medio`
- `pesado`

### C) ramping-arrival-rate
Requer `K6_ARRIVAL_PROFILE` (default `leve`):
```env
K6_EXECUTION_MODE=ramping-arrival-rate
K6_ARRIVAL_PROFILE=medio
```

### D) constant-arrival-rate
```env
K6_EXECUTION_MODE=constant-arrival-rate
K6_ARRIVAL_PROFILE=medio
K6_ARRIVAL_RATE=50
K6_ARRIVAL_DURATION=4m
```

## Observações
- `graduacao-crud.js` cria dados a cada iteração e não faz cleanup.
- Para cargas altas, acompanhe o crescimento do banco.
- Use `RUN_ID` para correlacionar requisições e logs.
