#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ── Uso ──────────────────────────────────────────────────────────────────────

usage() {
  cat <<EOF
Uso:
  ./executar-metricas.sh <c1|c2a1|c2a2|c2a3> [low|medium|high]

Executa o teste comparativo completo para um cenário:
  1) Derruba cenários anteriores
  2) Sobe cenário + monitoramento
  3) Aguarda healthcheck dos serviços
  4) (C2A2) Aguarda conectores Debezium
  5) Executa teste-comparativo.js com métricas exportadas ao Prometheus
  6) Salva output em monitoramento/resultados/

Exemplos:
  ./executar-metricas.sh c1 low
  ./executar-metricas.sh c2a2 medium
  ./executar-metricas.sh c2a3 high
EOF
}

# ── Configuração por cenário ─────────────────────────────────────────────────

resolve_scenario_config() {
  local scenario="$1"
  case "$scenario" in
    c1)
      REPLICATION_TIMEOUT_MS=15000
      POLL_INTERVAL_MS=500
      WARMUP_S=0
      DURATION_MULTIPLIER=1
      ;;
    c2a1)
      REPLICATION_TIMEOUT_MS=15000
      POLL_INTERVAL_MS=500
      WARMUP_S=0
      DURATION_MULTIPLIER=1
      ;;
    c2a2)
      REPLICATION_TIMEOUT_MS=30000
      POLL_INTERVAL_MS=500
      WARMUP_S=120
      DURATION_MULTIPLIER=3
      ;;
    c2a3)
      REPLICATION_TIMEOUT_MS=30000
      POLL_INTERVAL_MS=500
      WARMUP_S=60
      DURATION_MULTIPLIER=2
      ;;
    *)
      echo "Cenário inválido: $scenario"
      echo ""
      usage
      exit 1
      ;;
  esac
}

# Calcula duração com base na intensidade e multiplicador do cenário
compute_duration() {
  local intensity="$1"
  local multiplier="$2"
  local base_min
  case "$intensity" in
    low)    base_min=5 ;;
    medium) base_min=10 ;;
    high)   base_min=15 ;;
    *)      base_min=5 ;;
  esac
  echo "$((base_min * multiplier))m"
}

# ── Healthcheck ──────────────────────────────────────────────────────────────

wait_for_services() {
  local timeout=300
  local start_time
  start_time="$(date +%s)"
  local services=(
    "http://localhost:8081/actuator/health"
    "http://localhost:8082/actuator/health"
    "http://localhost:8083/actuator/health"
    "http://localhost:8084/actuator/health"
  )

  echo "Aguardando serviços ficarem saudáveis (timeout ${timeout}s)..."

  while true; do
    local all_up=true
    for url in "${services[@]}"; do
      if ! curl -sf "$url" > /dev/null 2>&1; then
        all_up=false
        break
      fi
    done

    if [[ "$all_up" == "true" ]]; then
      echo "Todos os serviços estão UP!"
      return 0
    fi

    local now elapsed
    now="$(date +%s)"
    elapsed=$((now - start_time))
    if [[ "$elapsed" -ge "$timeout" ]]; then
      echo "ERRO: Timeout aguardando serviços!"
      return 1
    fi

    echo "  aguardando... (${elapsed}s/${timeout}s)"
    sleep 5
  done
}

# ── Conectores Debezium (C2A2) ──────────────────────────────────────────────

wait_for_connectors() {
  local timeout=240
  local start_time
  start_time="$(date +%s)"
  local connect_url="http://localhost:${CONNECT_PORT:-18083}"

  echo "Aguardando Kafka Connect e conectores ficarem prontos (timeout ${timeout}s)..."

  while true; do
    local names_json
    names_json="$(curl -fsS "$connect_url/connectors" 2>/dev/null || true)"

    if [[ -n "$names_json" && "$names_json" != "[]" ]]; then
      local connectors
      connectors="$(echo "$names_json" | tr -d '[]"' | tr ',' '\n' | sed '/^\s*$/d')"

      local all_running=true
      while IFS= read -r c; do
        [[ -n "$c" ]] || continue
        local status
        status="$(curl -fsS "$connect_url/connectors/$c/status" 2>/dev/null || true)"
        if [[ -z "$status" || "$status" != *'"state":"RUNNING"'* ]]; then
          all_running=false
          break
        fi
      done <<< "$connectors"

      if [[ "$all_running" == "true" ]]; then
        echo "Kafka Connect pronto: conectores em execução."

        # Verificação canary: cria uma Pessoa teste e aguarda replicação CDC end-to-end
        echo "Verificando pipeline CDC end-to-end (canary)..."
        local canary_cpf="00000000001"
        local canary_payload="{\"nome\":\"CDC Canary\",\"cpf\":\"${canary_cpf}\",\"dataNascimento\":\"2000-01-01\"}"
        local canary_res
        canary_res="$(curl -s -o /dev/null -w '%{http_code}' -X POST \
          -H 'Content-Type: application/json' \
          -d "$canary_payload" \
          http://localhost:8081/pessoas 2>/dev/null || true)"

        if [[ "$canary_res" == "201" || "$canary_res" == "200" || "$canary_res" == "500" ]]; then
          # Aguarda a Pessoa aparecer no serviço Diplomas via CDC
          local canary_timeout=180
          local canary_start
          canary_start="$(date +%s)"
          local canary_ok=false

          while true; do
            local diplomas_res
            diplomas_res="$(curl -fsS http://localhost:8083/pessoas 2>/dev/null || true)"
            if [[ -n "$diplomas_res" && "$diplomas_res" == *"CDC Canary"* ]]; then
              canary_ok=true
              break
            fi

            local cnow celapsed
            cnow="$(date +%s)"
            celapsed=$((cnow - canary_start))
            if [[ "$celapsed" -ge "$canary_timeout" ]]; then
              break
            fi

            echo "  aguardando CDC replicar canary... (${celapsed}s/${canary_timeout}s)"
            sleep 5
          done

          if [[ "$canary_ok" == "true" ]]; then
            echo "Pipeline CDC verificado: replicação end-to-end OK!"
          else
            echo "AVISO: Canary não replicou em ${canary_timeout}s. CDC pode estar lento."
          fi
        else
          echo "AVISO: Não foi possível criar canary (HTTP $canary_res). Pulando verificação."
        fi

        return 0
      fi
    fi

    local now elapsed
    now="$(date +%s)"
    elapsed=$((now - start_time))
    if [[ "$elapsed" -ge "$timeout" ]]; then
      echo "ERRO: Timeout aguardando conectores do Kafka Connect."
      return 1
    fi

    echo "  conectores não prontos ainda (${elapsed}s/${timeout}s)..."
    sleep 5
  done
}

# ── Main ─────────────────────────────────────────────────────────────────────

if [[ $# -lt 1 ]]; then
  usage
  exit 1
fi

SCENARIO="${1,,}"
INTENSITY="${2:-low}"
RUN_ID="run_$(date +%Y%m%d_%H%M%S)"

# Normalizar cenário
case "$SCENARIO" in
  c2) SCENARIO="c2a1" ;;
esac

resolve_scenario_config "$SCENARIO"
COMPUTED_DURATION="$(compute_duration "$INTENSITY" "$DURATION_MULTIPLIER")"

echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║  Teste Comparativo — cenário: $SCENARIO"
echo "║  Intensidade: $INTENSITY | Run ID: $RUN_ID"
echo "║  Duração: $COMPUTED_DURATION | Warmup: ${WARMUP_S}s"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# 1. Derrubar cenários anteriores
echo "==> [1/6] Derrubando cenários anteriores..."
"$ROOT_DIR/simulacao.sh" down "$SCENARIO" 2>/dev/null || true
# Derrubar monitoramento anterior também
docker compose -f "$ROOT_DIR/docker-compose.monitoramento.yml" down -v 2>/dev/null || true
echo "    OK."

# 2. Subir cenário (sem monitoramento — ele é separado)
echo ""
echo "==> [2/6] Subindo cenário $SCENARIO..."
"$ROOT_DIR/simulacao.sh" up "$SCENARIO"

# 3. Subir stack de monitoramento (Prometheus + Grafana + postgres-exporter)
echo ""
echo "==> [3/6] Subindo stack de monitoramento..."
docker compose -f "$ROOT_DIR/docker-compose.monitoramento.yml" up -d
echo "    Monitoramento UP (Prometheus:9090, Grafana:3000)"

# 4. Healthcheck dos serviços
echo ""
echo "==> [4/6] Verificando healthcheck dos serviços..."
wait_for_services

# 5. Conectores Debezium (apenas c2a2 — CDC via Debezium; c2a3 usa eventos na aplicação)
if [[ "$SCENARIO" == "c2a2" ]]; then
  echo ""
  echo "==> [5/6] Verificando conectores Debezium..."
  wait_for_connectors
else
  echo ""
  echo "==> [5/6] Conectores Debezium: N/A para $SCENARIO"
fi

# 6. Executar k6
echo ""
echo "==> [6/6] Executando teste-comparativo.js..."
echo "    Cenário: $SCENARIO"
echo "    Intensidade: $INTENSITY"
echo "    Timeout replicação: ${REPLICATION_TIMEOUT_MS}ms"
echo "    Intervalo polling: ${POLL_INTERVAL_MS}ms"
echo "    Duração: ${COMPUTED_DURATION} | Warmup: ${WARMUP_S}s"
echo "    Run ID: $RUN_ID"
echo ""

# Garantir diretório de resultados
RESULTS_DIR="$ROOT_DIR/monitoramento/resultados"
mkdir -p "$RESULTS_DIR"
RESULT_FILE="$RESULTS_DIR/${SCENARIO}-${INTENSITY}-${RUN_ID}.txt"

REPLICATION_TIMEOUT_MS="$REPLICATION_TIMEOUT_MS" \
POLL_INTERVAL_MS="$POLL_INTERVAL_MS" \
INTENSITY="$INTENSITY" \
DURATION="$COMPUTED_DURATION" \
WARMUP_S="$WARMUP_S" \
k6 run \
  --tag scenario="$SCENARIO" \
  --tag run_id="$RUN_ID" \
  --out "experimental-prometheus-rw=http://localhost:9090/api/v1/write" \
  "$ROOT_DIR/monitoramento/k6/scripts/teste-comparativo.js" \
  2>&1 | tee "$RESULT_FILE"

K6_EXIT=${PIPESTATUS[0]}

echo ""
echo "════════════════════════════════════════════════════════════"
echo "  Resultado salvo em: $RESULT_FILE"
echo "  Dashboard Grafana:  http://localhost:3000/d/tcc-comparativo"
echo "  Prometheus:         http://localhost:9090"
echo "════════════════════════════════════════════════════════════"

exit "$K6_EXIT"
