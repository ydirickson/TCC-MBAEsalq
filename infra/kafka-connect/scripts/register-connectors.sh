#!/bin/sh
set -eu

CONNECT_URL="${CONNECT_URL:-http://connect:8083}"
CONNECTORS_DIR="${CONNECTORS_DIR:-/connectors}"
MAX_RETRIES="${CONNECT_MAX_RETRIES:-90}"
SLEEP_SECONDS="${CONNECT_RETRY_SLEEP_SECONDS:-2}"

CONNECTOR_FILES="${CONNECTOR_FILES:-source-graduacao-pessoa.json source-pos-graduacao-pessoa.json sink-pessoa-diplomas.json sink-pessoa-pos-graduacao.json sink-pessoa-assinatura.json source-graduacao-vinculo.json source-pos-graduacao-vinculo.json sink-vinculo-graduacao-diplomas.json sink-vinculo-graduacao-assinatura.json sink-vinculo-pos-diplomas.json sink-vinculo-pos-assinatura.json}"

wait_for_connect() {
  i=1
  while [ "$i" -le "$MAX_RETRIES" ]; do
    if curl -fsS "$CONNECT_URL/connector-plugins" >/tmp/connector-plugins.json 2>/dev/null; then
      return 0
    fi
    echo "[connect-init] Aguardando Connect ($i/$MAX_RETRIES)..."
    sleep "$SLEEP_SECONDS"
    i=$((i + 1))
  done

  echo "[connect-init] Connect nao ficou disponivel em tempo habil."
  return 1
}

validate_plugins() {
  if ! grep -q 'io.debezium.connector.postgresql.PostgresConnector' /tmp/connector-plugins.json; then
    echo "[connect-init] Plugin Debezium Postgres nao encontrado."
    return 1
  fi

  # Debezium Connect 3.4 ja inclui io.debezium.connector.jdbc.JdbcSinkConnector.
  # Mantemos compatibilidade com setups antigos que usam o plugin Confluent JDBC Sink.
  if ! grep -q 'io.debezium.connector.jdbc.JdbcSinkConnector' /tmp/connector-plugins.json \
    && ! grep -q 'io.confluent.connect.jdbc.JdbcSinkConnector' /tmp/connector-plugins.json; then
    echo "[connect-init] Nenhum plugin JDBC Sink disponivel no worker Connect."
    echo "[connect-init] Classes esperadas: io.debezium.connector.jdbc.JdbcSinkConnector ou io.confluent.connect.jdbc.JdbcSinkConnector."
    return 1
  fi

  return 0
}

register_connector() {
  connector_file="$1"
  full_path="$CONNECTORS_DIR/$connector_file"

  if [ ! -f "$full_path" ]; then
    echo "[connect-init] Arquivo nao encontrado: $full_path"
    return 1
  fi

  http_code="$(curl -sS -o /tmp/connector-response.json -w '%{http_code}' \
    -X POST "$CONNECT_URL/connectors" \
    -H 'Content-Type: application/json' \
    --data-binary "@$full_path")"

  if [ "$http_code" = "201" ]; then
    echo "[connect-init] Connector criado: $connector_file"
    return 0
  fi

  if [ "$http_code" = "409" ]; then
    echo "[connect-init] Connector ja existe (mantido): $connector_file"
    return 0
  fi

  echo "[connect-init] Falha ao criar connector $connector_file (HTTP $http_code):"
  cat /tmp/connector-response.json
  echo
  return 1
}

main() {
  wait_for_connect
  validate_plugins

  for connector in $CONNECTOR_FILES; do
    register_connector "$connector"
  done

  echo "[connect-init] Bootstrap de conectores concluido."
}

main "$@"
