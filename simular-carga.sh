#!/usr/bin/env bash
set -e

# Defaults
INTENSITY="low"
MONITORING="false"
TIMEOUT_SECONDS=120

usage() {
    echo "Uso: ./simular-carga.sh [nivel-intensidade] [--monitoring]"
    echo "  nivel-intensidade: low (default), medium, high"
    echo "  --monitoring: Habilita output do k6 para o Prometheus (requer stack de monitoramento)"
    exit 1
}

wait_for_services() {
    echo "=== Aguardando servicos estarem prontos (healthcheck) ==="
    
    local services=("http://localhost:8081/actuator/health" "http://localhost:8082/actuator/health" "http://localhost:8083/actuator/health" "http://localhost:8084/actuator/health")
    local start_time=$(date +%s)

    while true; do
        local all_up=true
        
        for url in "${services[@]}"; do
            if ! curl -s -f "$url" > /dev/null; then
                all_up=false
                break
            fi
        done

        if [ "$all_up" = true ]; then
            echo "Todos os servicos estao UP!"
            break
        fi

        local current_time=$(date +%s)
        local elapsed=$((current_time - start_time))
        
        if [ "$elapsed" -ge "$TIMEOUT_SECONDS" ]; then
            echo "Timeout aguardando servicos!"
            exit 1
        fi
        
        echo "Aguardando servicos... (${elapsed}s/${TIMEOUT_SECONDS}s)"
        sleep 5
    done
}

# Parse args
while [[ "$#" -gt 0 ]]; do
    case $1 in
        low|medium|high) INTENSITY="$1" ;;
        --monitoring) MONITORING="true" ;;
        -h|--help) usage ;;
        *) echo "Opção desconhecida: $1"; usage ;;
    esac
    shift
done

# Aguarda servicos
wait_for_services

echo "=== Iniciando Simulação Completa ==="
echo "Intensidade: $INTENSITY"
echo "Monitoramento: $MONITORING"

K6_CMD="k6 run"
K6_ENV=""

if [ "$MONITORING" = "true" ]; then
    # Configura remote write para Prometheus se solicitado
    K6_CMD="$K6_CMD --out experimental-prometheus-rw"
    # Assume que o Prometheus está rodando no localhost padrão ou via docker network,
    # mas o k6 roda local neste script. Se k6 rodar em container, precisaria de ajuste de rede.
    export K6_PROMETHEUS_RW_SERVER_URL="http://localhost:9090/api/v1/write"
    export K6_PROMETHEUS_RW_TREND_AS_NATIVE_HISTOGRAM=true
fi

# Executa o script
INTENSITY=$INTENSITY $K6_CMD monitoramento/k6/scripts/simulacao-completa.js

echo "=== Simulação Finalizada ==="
