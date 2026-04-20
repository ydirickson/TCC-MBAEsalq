#!/usr/bin/env bash
# Derruba todos os cenários e limpa seus volumes.
# O stack de monitoramento (Prometheus + Grafana) NÃO é afetado.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "==> Derrubando cenários e limpando volumes..."

docker compose -f "$ROOT_DIR/docker-compose-c1.yml"   --env-file "$ROOT_DIR/.env.c1"   down -v 2>/dev/null || true
docker compose -f "$ROOT_DIR/docker-compose-c2a1.yml" --env-file "$ROOT_DIR/.env.c2"   down -v 2>/dev/null || true
docker compose -f "$ROOT_DIR/docker-compose-c2a2.yml" --env-file "$ROOT_DIR/.env.c2a2" down -v 2>/dev/null || true
docker compose -f "$ROOT_DIR/docker-compose-c2a3.yml" --env-file "$ROOT_DIR/.env.c2a3" down -v 2>/dev/null || true

echo "==> Pronto. Monitoramento preservado (prometheus_data, grafana_data intactos)."
