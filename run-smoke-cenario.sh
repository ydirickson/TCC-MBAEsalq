#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

usage() {
  cat <<EOF
Uso:
  ./run-smoke-cenario.sh <c1|c2|c2a1|c2a2|c2a3>

O script:
  1) derruba TODOS os cenarios (containers, networks e volumes),
  2) sobe o cenario escolhido,
  3) aguarda healthcheck dos 4 servicos,
  4) executa simulacao/smoke-test.js com presets de timeout por cenario.

Exemplos:
  ./run-smoke-cenario.sh c1
  ./run-smoke-cenario.sh c2a2
EOF
}

normalize_scenario() {
  local s="${1,,}"
  case "$s" in
    c2|c2a1) echo "c2a1" ;;
    *) echo "$s" ;;
  esac
}

resolve_stack() {
  local scenario="$1"
  case "$scenario" in
    c1)
      ENV_FILE="$ROOT_DIR/.env.c1"
      COMPOSE_FILE="$ROOT_DIR/docker-compose-c1.yml"
      DEFAULT_TIMEOUT_MS=5000
      DEFAULT_POLL_MS=200
      DEFAULT_P95_MS=500
      DEFAULT_P99_MS=1000
      ;;
    c2a1)
      ENV_FILE="$ROOT_DIR/.env.c2"
      COMPOSE_FILE="$ROOT_DIR/docker-compose-c2a1.yml"
      DEFAULT_TIMEOUT_MS=10000
      DEFAULT_POLL_MS=500
      DEFAULT_P95_MS=2000
      DEFAULT_P99_MS=5000
      ;;
    c2a2)
      ENV_FILE="$ROOT_DIR/.env.c2a2"
      COMPOSE_FILE="$ROOT_DIR/docker-compose-c2a2.yml"
      DEFAULT_TIMEOUT_MS=30000
      DEFAULT_POLL_MS=1000
      DEFAULT_P95_MS=10000
      DEFAULT_P99_MS=30000
      ;;
    c2a3)
      ENV_FILE="$ROOT_DIR/.env.c2a3"
      COMPOSE_FILE="$ROOT_DIR/docker-compose-c2a3.yml"
      DEFAULT_TIMEOUT_MS=30000
      DEFAULT_POLL_MS=1000
      DEFAULT_P95_MS=10000
      DEFAULT_P99_MS=30000
      ;;
    *)
      echo "Cenario invalido: $scenario"
      echo ""
      usage
      exit 1
      ;;
  esac

  if [[ ! -f "$ENV_FILE" ]]; then
    echo "Env file nao encontrado: $ENV_FILE"
    exit 1
  fi

  if [[ ! -f "$COMPOSE_FILE" ]]; then
    echo "Compose file nao encontrado: $COMPOSE_FILE"
    exit 1
  fi
}

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

  echo "Aguardando servicos ficarem saudaveis (timeout ${timeout}s)..."

  while true; do
    local all_up=true
    for url in "${services[@]}"; do
      if ! curl -fsS "$url" >/dev/null 2>&1; then
        all_up=false
        break
      fi
    done

    if [[ "$all_up" == "true" ]]; then
      echo "Todos os servicos estao UP."
      return 0
    fi

    local now elapsed
    now="$(date +%s)"
    elapsed=$((now - start_time))

    if [[ "$elapsed" -ge "$timeout" ]]; then
      echo "Timeout aguardando healthcheck dos servicos."
      return 1
    fi

    echo "  servicos ainda nao disponiveis (${elapsed}s/${timeout}s)..."
    sleep 5
  done
}

compose_down_safely() {
  local env_file="$1"
  local compose_file="$2"
  local project_flag=()
  if [[ -n "${3:-}" ]]; then
    project_flag=(--project-name "$3")
  fi

  docker compose --env-file "$env_file" "${project_flag[@]}" -f "$compose_file" \
    down -v --remove-orphans >/dev/null 2>&1 || true
}

cleanup_all_scenarios() {
  echo "==> Limpando stacks de todos os cenarios (containers / networks / volumes)..."

  # Todos os pares env|compose conhecidos
  local -a stacks=(
    "$ROOT_DIR/.env.c1|$ROOT_DIR/docker-compose-c1.yml"
    "$ROOT_DIR/.env.c2|$ROOT_DIR/docker-compose-c2a1.yml"
    "$ROOT_DIR/.env.c2a2|$ROOT_DIR/docker-compose-c2a2.yml"
    "$ROOT_DIR/.env.c2a3|$ROOT_DIR/docker-compose-c2a3.yml"
  )

  # Nomes de projeto usados pelo simulacao.sh e por execucoes anteriores
  local -a known_projects=("tcc-c1" "tcc-c2a1" "tcc-c2a2" "tcc-c2a3")

  local pair
  for pair in "${stacks[@]}"; do
    local env_file="${pair%%|*}"
    local compose_file="${pair##*|}"

    [[ -f "$env_file" && -f "$compose_file" ]] || continue

    # Derrubar sem project-name (usa nome inferido pelo compose)
    compose_down_safely "$env_file" "$compose_file"

    # Derrubar para cada project-name conhecido (execucoes anteriores com --project-name)
    local pn
    for pn in "${known_projects[@]}"; do
      compose_down_safely "$env_file" "$compose_file" "$pn"
    done
  done

  # Limpeza residual de networks e volumes por label de projeto
  local project_default
  project_default="$(basename "$ROOT_DIR" | tr '[:upper:]' '[:lower:]' | tr -cd 'a-z0-9_-')"

  local -a all_projects=("$project_default" "${known_projects[@]}")
  local pr
  for pr in "${all_projects[@]}"; do
    docker network ls --filter "label=com.docker.compose.project=$pr" -q \
      | xargs -r docker network rm >/dev/null 2>&1 || true
    docker volume ls --filter "label=com.docker.compose.project=$pr" -q \
      | xargs -r docker volume rm >/dev/null 2>&1 || true
  done

  echo "    Limpeza concluida."
}

# ── Main ──────────────────────────────────────────────────────────────────────

if [[ $# -lt 1 ]]; then
  usage
  exit 1
fi

SCENARIO="$(normalize_scenario "$1")"
resolve_stack "$SCENARIO"

echo ""
echo "========================================"
echo " Smoke test — cenario: $SCENARIO"
echo "========================================"
echo ""

cleanup_all_scenarios

echo ""
echo "==> Subindo cenario $SCENARIO..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d

echo ""
wait_for_services

# Aplicar presets do cenario, permitindo override via variavel de ambiente
export REPLICATION_TIMEOUT_MS="${REPLICATION_TIMEOUT_MS:-$DEFAULT_TIMEOUT_MS}"
export POLL_INTERVAL_MS="${POLL_INTERVAL_MS:-$DEFAULT_POLL_MS}"
export REPLICACAO_P95_MS="${REPLICACAO_P95_MS:-$DEFAULT_P95_MS}"
export REPLICACAO_P99_MS="${REPLICACAO_P99_MS:-$DEFAULT_P99_MS}"

echo ""
echo "==> Executando smoke-test com presets do cenario $SCENARIO:"
echo "    REPLICATION_TIMEOUT_MS=$REPLICATION_TIMEOUT_MS"
echo "    POLL_INTERVAL_MS=$POLL_INTERVAL_MS"
echo "    REPLICACAO_P95_MS=$REPLICACAO_P95_MS"
echo "    REPLICACAO_P99_MS=$REPLICACAO_P99_MS"
echo ""

cd "$ROOT_DIR"
k6 run simulacao/smoke-test.js
