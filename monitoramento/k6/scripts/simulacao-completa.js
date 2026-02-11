import http from 'k6/http';
import { sleep, check, fail } from 'k6';
import { Trend, Counter, Rate } from 'k6/metrics';
import { runGraduacaoFlow, runPosGraduacaoFlow } from '../helpers/business-flows.js';
import { waitForReplication, readReplicationConfig } from '../helpers/replication.js';
import { businessThroughput, stalenessWindow } from '../helpers/metrics.js';

// --- Métricas Customizadas (M1 a M5) ---
// M1: Latência ponta-a-ponta (capturado pelo waitForReplication via replication_latency_ms)
// M2: Throughput (via businessThroughput e http_reqs do k6)
// M3: Taxa de erro (via replication_failure_total e checks)
// M4: Consistência (via replicationSuccess)
// M5: Staleness (via stalenessWindow)

const replicationLatency = new Trend('replication_latency_ms', true);
const replicationSuccess = new Counter('replication_success_total');
const replicationFailure = new Counter('replication_failure_total');

// Configuração Base
const BASE_URLS = {
  graduacao: __ENV.GRADUACAO_URL || 'http://localhost:8081',
  posGraduacao: __ENV.POS_GRADUACAO_URL || 'http://localhost:8082',
  diplomas: __ENV.DIPLOMAS_URL || 'http://localhost:8083',
  assinatura: __ENV.ASSINATURA_URL || 'http://localhost:8084',
};

// Intensidade
const INTENSITY = __ENV.INTENSITY || 'low';
const CONFIGS = {
  low: { vus: 5, duration: '5m' },
  medium: { vus: 20, duration: '10m' },
  high: { vus: 50, duration: '15m' },
};
const config = CONFIGS[INTENSITY] || CONFIGS['low'];

// Sobrescrita manual
const VUS = __ENV.VUS ? parseInt(__ENV.VUS) : config.vus;
const DURATION = __ENV.DURATION || config.duration;

export const options = {
  scenarios: {
    graduacao_load: {
      executor: 'constant-vus',
      vus: Math.ceil(VUS / 2),
      duration: DURATION,
      exec: 'graduacaoScenario',
    },
    pos_load: {
      executor: 'constant-vus',
      vus: Math.floor(VUS / 2),
      duration: DURATION,
      exec: 'posScenario',
    },
  },
  thresholds: {
    'http_req_duration': ['p(95)<2000'], // M1/M6 latency check
    'replication_success_total': ['count>0'],
    'replication_failure_total': ['count<100'], // M3 threshold
  },
};

const replicationConfig = readReplicationConfig({
  defaults: {
    timeoutMs: 15000,
    pollIntervalMs: 500,
    mode: 'strict', // Valida consistência
  }
});

function completeBusinessFlow(serviceName, data) {
    if(!data.alunoId) return;

    // 1. Concluir Aluno na Origem (Necessário para diploma)
    const conclusionDate = new Date().toISOString().slice(0, 10);
    const updatePayload = JSON.stringify({
        pessoaId: data.pessoaAluno.id,
        turmaId: data.turmaId || undefined,     // Graduação
        programaId: data.programaId || undefined, // Pós
        orientadorId: data.professorId || undefined, // Pós
        dataMatricula: new Date().toISOString().slice(0, 10),
        dataConclusao: conclusionDate,
        status: 'CONCLUIDO'
    });

    const updateUrl = serviceName === 'graduacao' 
        ? `${BASE_URLS.graduacao}/alunos/${data.alunoId}`
        : `${BASE_URLS.posGraduacao}/alunos/${data.alunoId}`;
    
    const headers = { 'Content-Type': 'application/json' };
    
    const updateRes = http.put(updateUrl, updatePayload, { headers });
    
    if(!check(updateRes, { 'Aluno Concluido': (r) => r.status === 200 })) {
        console.warn(`Falha ao concluir aluno: ${updateRes.status} body=${updateRes.body}`);
        return;
    }

    // 2. Aguardar replicação da atualização de status (M5 Staleness check)
    // Precisamos saber se o vinculo no sistema de diplomas está atualizado
    const startStaleness = Date.now();
    const vinculoCheck = waitForReplication({
        url: `${BASE_URLS.diplomas}/vinculos-academicos?pessoaId=${data.pessoaAluno.id}`,
        validateFn: (lista) => {
             // Precisamos encontrar um vinculo CONCLUIDO
             return Array.isArray(lista) && lista.some(v => v.situacao === 'CONCLUIDO'); 
        },
        maxAttempts: replicationConfig.maxAttempts,
        intervalMs: replicationConfig.pollIntervalMs,
    });

    if(!vinculoCheck.success) {
        console.warn('Falha replicação status CONCLUIDO');
        return;
    }
    
    stalenessWindow.add(Date.now() - startStaleness);
    
    const vinculo = vinculoCheck.data.find(v => v.situacao === 'CONCLUIDO');
    
    // 3. Solicitar Diploma (Geração de documento)
    const diplomaPayload = JSON.stringify({
         pessoaId: data.pessoaAluno.id,
         vinculoId: vinculo.id,
         cursoCodigo: vinculo.cursoCodigo,
         cursoNome: vinculo.cursoNome,
         cursoTipo: vinculo.cursoTipo,
         dataConclusao: conclusionDate 
    });
    
    const diplomaRes = http.post(`${BASE_URLS.diplomas}/requerimentos`, diplomaPayload, { headers });
    
    if(!check(diplomaRes, {'Diploma Solicitado': (r) => r.status === 201})) return;
    
    businessThroughput.add(1);
}

function checkReplication(originData, serviceName, targetServiceUrl, targetServiceName) {
    const personId = originData.pessoaAluno.id;
    
    const result = waitForReplication({
        url: `${targetServiceUrl}/pessoas/${personId}`,
        validateFn: (data) => data.id === personId && data.nome === originData.pessoaAluno.nome,
        maxAttempts: replicationConfig.maxAttempts,
        intervalMs: replicationConfig.pollIntervalMs,
    });

    if (result.success) {
        replicationSuccess.add(1, { source: serviceName, target: targetServiceName });
        replicationLatency.add(result.latency, { source: serviceName, target: targetServiceName });
    } else {
        replicationFailure.add(1, { source: serviceName, target: targetServiceName });
        console.warn(`[Replication Falha] ${serviceName} -> ${targetServiceName} (ID: ${personId})`);
    }
}

export function graduacaoScenario() {
  const meta = { runId: `run_${__VU}_${__ITER}`, scenario: 'graduacao_load' };
  
  // 1. Gera escrita na Graduação
  const data = runGraduacaoFlow({  
    baseUrl: BASE_URLS.graduacao, 
    ...meta 
  });

  // 2. Verifica replicação para serviços consumidores
  checkReplication(data, 'graduacao', BASE_URLS.diplomas, 'diplomas');
  // checkReplication(data, 'graduacao', BASE_URLS.assinatura, 'assinatura'); // Otimização: focar no fluxo funcional
  
  // 3. Executa fluxo de negócio completo (Conclusão -> Diploma)
  completeBusinessFlow('graduacao', data);
  
  sleep(1);
}

export function posScenario() {
  const meta = { runId: `run_pos_${__VU}_${__ITER}`, scenario: 'pos_load' };
  
  // 1. Gera escrita na Pós-Graduação
  const data = runPosGraduacaoFlow({ 
    baseUrl: BASE_URLS.posGraduacao, 
    ...meta 
  });

  // 2. Verifica replicação para serviços consumidores
  checkReplication(data, 'pos-graduacao', BASE_URLS.diplomas, 'diplomas');
  // checkReplication(data, 'pos-graduacao', BASE_URLS.assinatura, 'assinatura');

  // 3. Executa fluxo de negócio completo (Conclusão -> Diploma)
  completeBusinessFlow('pos-graduacao', data);
}
