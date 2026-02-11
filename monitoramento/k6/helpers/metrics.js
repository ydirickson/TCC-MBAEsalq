import { Counter, Trend } from 'k6/metrics';

// M2 - Throughput de Processos de Negócio
export const businessThroughput = new Counter('business_throughput');

// M5 - Janela de Staleness (tempo que o sistema ficou inconsistente na visão do usuário)
// Aqui aproximamos usando a latência de replicação que já capturamos, 
// mas adicionamos uma métrica específica para clareza
export const stalenessWindow = new Trend('staleness_window_ms'); 
