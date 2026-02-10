#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

usage() {
  cat <<USAGE
Uso:
  ./simulacao.sh <up|down|ps|logs|config> <cenario> [--monitoring] [--project-name <nome>] [--env-file <arquivo>] [--] [args...]

Cenarios aceitos:
  c1
  c1a1 | c1a2 | c1a3
  c2 | c2a1 | c2a2 | c2a3
  c3 | c3a1 | c3a2 | c3a3

Exemplos:
  ./simulacao.sh up c1
  ./simulacao.sh up c2a2
  ./simulacao.sh up c2a2 --monitoring
  ./simulacao.sh logs c2a2 -- connect-init
  ./simulacao.sh down c2a2 --project-name tcc-c2a2
USAGE
}

if [ "$#" -lt 2 ]; then
  usage
  exit 1
fi

ACTION="$1"
SCENARIO_INPUT="$(echo "$2" | tr '[:upper:]' '[:lower:]')"
shift 2

MONITORING="false"
PROJECT_NAME=""
ENV_FILE_OVERRIDE=""
EXTRA_ARGS=()

while [ "$#" -gt 0 ]; do
  case "$1" in
    --monitoring)
      MONITORING="true"
      shift
      ;;
    --project-name)
      PROJECT_NAME="$2"
      shift 2
      ;;
    --env-file)
      ENV_FILE_OVERRIDE="$2"
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

FILES=()
ENV_FILE=""

case "$SCENARIO_INPUT" in
  c1)
    ENV_FILE="$ROOT_DIR/.env.c1"
    FILES=("docker-compose-c1.yml")
    ;;
  c1a1)
    ENV_FILE="$ROOT_DIR/.env.c1a1"
    FILES=("compose/base.yml" "compose/db.c1.yml" "compose/services.yml")
    ;;
  c1a2)
    ENV_FILE="$ROOT_DIR/.env.c1a2"
    FILES=("compose/base.yml" "compose/db.c1.yml" "compose/services.yml" "compose/db.cdc.yml" "compose/replication.a2.yml")
    ;;
  c1a3)
    ENV_FILE="$ROOT_DIR/.env.c1"
    FILES=("compose/base.yml" "compose/db.c1.yml" "compose/services.yml" "compose/replication.a3.yml")
    ;;
  c2|c2a1)
    ENV_FILE="$ROOT_DIR/.env.c2"
    FILES=("docker-compose-c2a1.yml")
    ;;
  c2a2)
    ENV_FILE="$ROOT_DIR/.env.c2a2"
    FILES=("docker-compose-c2a2.yml")
    ;;
  c2a3)
    ENV_FILE="$ROOT_DIR/.env.c2"
    FILES=("compose/base.yml" "compose/db.c2.yml" "compose/services.yml" "compose/replication.a3.yml")
    ;;
  c3|c3a1)
    ENV_FILE="$ROOT_DIR/.env.c3"
    FILES=("compose/base.yml" "compose/db.c3.yml" "compose/services.yml")
    ;;
  c3a2)
    ENV_FILE="$ROOT_DIR/.env.c3"
    FILES=("compose/base.yml" "compose/db.c3.yml" "compose/services.yml" "compose/db.cdc.yml" "compose/replication.a2.yml")
    ;;
  c3a3)
    ENV_FILE="$ROOT_DIR/.env.c3"
    FILES=("compose/base.yml" "compose/db.c3.yml" "compose/services.yml" "compose/replication.a3.yml")
    ;;
  *)
    echo "Cenario invalido: $SCENARIO_INPUT"
    usage
    exit 1
    ;;
esac

if [ -n "$ENV_FILE_OVERRIDE" ]; then
  ENV_FILE="$ENV_FILE_OVERRIDE"
fi

if [ ! -f "$ENV_FILE" ]; then
  echo "Arquivo de ambiente nao encontrado: $ENV_FILE"
  exit 1
fi

if [ "$MONITORING" = "true" ]; then
  if [ "$SCENARIO_INPUT" = "c1" ]; then
    echo "O cenario c1 (compose unico) nao inclui monitoramento nesta versao."
    echo "Use c1a1/c2a1/c2a2/... para compor com compose/monitoring.yml."
    exit 1
  fi
  FILES+=("compose/monitoring.yml")
fi

CMD=(docker compose --env-file "$ENV_FILE")

if [ -n "$PROJECT_NAME" ]; then
  CMD+=(--project-name "$PROJECT_NAME")
fi

for file in "${FILES[@]}"; do
  CMD+=(-f "$ROOT_DIR/$file")
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
