#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_DIR="$ROOT_DIR/compose"

usage() {
  cat <<USAGE
Uso:
  scripts/compose-scenario.sh <up|down|ps|logs|config> <c1|c2|c3> [a1|a2|a3] [--monitoring] [--env-file <arquivo>] [--project-name <nome>] [--] [args...]

Exemplos:
  scripts/compose-scenario.sh up c2 a2
  scripts/compose-scenario.sh up c2 a2 --monitoring
  scripts/compose-scenario.sh down c2 a2 --project-name tcc-c2a2
  scripts/compose-scenario.sh logs c2 a2 -- connect-init
USAGE
}

if [ "$#" -lt 2 ]; then
  usage
  exit 1
fi

ACTION="$1"
SCENARIO="$2"
ARCH="${3:-a1}"

if [ "$#" -ge 3 ] && [[ "$3" =~ ^a[123]$ ]]; then
  shift 3
else
  shift 2
fi

MONITORING="false"
ENV_FILE=""
PROJECT_NAME=""
EXTRA_ARGS=()

while [ "$#" -gt 0 ]; do
  case "$1" in
    --monitoring)
      MONITORING="true"
      shift
      ;;
    --env-file)
      ENV_FILE="$2"
      shift 2
      ;;
    --project-name)
      PROJECT_NAME="$2"
      shift 2
      ;;
    --)
      shift
      EXTRA_ARGS=("$@")
      break
      ;;
    *)
      EXTRA_ARGS+=("$1")
      shift
      ;;
  esac
done

case "$SCENARIO" in
  c1|c2|c3) ;;
  *)
    echo "Cenario invalido: $SCENARIO"
    usage
    exit 1
    ;;
esac

case "$ARCH" in
  a1|a2|a3) ;;
  *)
    echo "Arquitetura invalida: $ARCH"
    usage
    exit 1
    ;;
esac

if [ -z "$ENV_FILE" ]; then
  case "$SCENARIO:$ARCH" in
    c1:a1) ENV_FILE="$ROOT_DIR/.env.c1a1" ;;
    c1:a2) ENV_FILE="$ROOT_DIR/.env.c1a2" ;;
    c1:a3) ENV_FILE="$ROOT_DIR/.env.c1" ;;
    c2:*) ENV_FILE="$ROOT_DIR/.env.c2" ;;
    c3:*) ENV_FILE="$ROOT_DIR/.env.c3" ;;
  esac
fi

if [ ! -f "$ENV_FILE" ]; then
  echo "Arquivo de ambiente nao encontrado: $ENV_FILE"
  exit 1
fi

FILES=(
  "$COMPOSE_DIR/base.yml"
  "$COMPOSE_DIR/db.$SCENARIO.yml"
  "$COMPOSE_DIR/services.yml"
)

if [ "$ARCH" = "a2" ]; then
  FILES+=("$COMPOSE_DIR/db.cdc.yml" "$COMPOSE_DIR/replication.a2.yml")
elif [ "$ARCH" = "a3" ]; then
  FILES+=("$COMPOSE_DIR/replication.a3.yml")
fi

if [ "$MONITORING" = "true" ]; then
  FILES+=("$COMPOSE_DIR/monitoring.yml")
fi

CMD=(docker compose --env-file "$ENV_FILE")

if [ -n "$PROJECT_NAME" ]; then
  CMD+=(--project-name "$PROJECT_NAME")
fi

for file in "${FILES[@]}"; do
  CMD+=(-f "$file")
done

case "$ACTION" in
  up)
    CMD+=(up -d)
    ;;
  down)
    CMD+=(down -v)
    ;;
  ps)
    CMD+=(ps)
    ;;
  logs)
    CMD+=(logs -f)
    ;;
  config)
    CMD+=(config)
    ;;
  *)
    echo "Acao invalida: $ACTION"
    usage
    exit 1
    ;;
esac

if [ "${#EXTRA_ARGS[@]}" -gt 0 ]; then
  CMD+=("${EXTRA_ARGS[@]}")
fi

echo "Executando: ${CMD[*]}"
"${CMD[@]}"
