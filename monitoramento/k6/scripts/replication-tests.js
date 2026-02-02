import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { Counter, Trend } from 'k6/metrics';

// Métricas customizadas
const replicationLatency = new Trend('replication_latency_ms');
const replicationSuccess = new Counter('replication_success_total');
const replicationFailure = new Counter('replication_failure_total');

// Configuração de URLs base
const BASE_URLS = {
  graduacao: __ENV.GRADUACAO_URL || 'http://localhost:8081',
  posGraduacao: __ENV.POS_GRADUACAO_URL || 'http://localhost:8082',
  diplomas: __ENV.DIPLOMAS_URL || 'http://localhost:8083',
  assinatura: __ENV.ASSINATURA_URL || 'http://localhost:8084',
};

// Configuração de timeout e polling
const REPLICATION_TIMEOUT_MS = parseInt(__ENV.REPLICATION_TIMEOUT_MS || '30000');
const POLL_INTERVAL_MS = parseInt(__ENV.POLL_INTERVAL_MS || '500');
const MAX_POLL_ATTEMPTS = Math.floor(REPLICATION_TIMEOUT_MS / POLL_INTERVAL_MS);

export const options = {
  scenarios: {
    replication_test: {
      executor: 'shared-iterations',
      vus: 1,
      iterations: 1,
      maxDuration: '5m',
    },
  },
  thresholds: {
    'checks': ['rate>0.95'],
    'http_req_duration': ['p(95)<5000'],
  },
};

/**
 * Aguarda até que um recurso seja replicado verificando sua existência via GET
 * @param {string} url - URL do endpoint de consulta
 * @param {function} validateFn - Função que valida se o recurso foi replicado corretamente
 * @param {number} maxAttempts - Número máximo de tentativas
 * @param {number} intervalMs - Intervalo entre tentativas em ms
 * @returns {object} - {success: boolean, attempts: number, latency: number, data: object}
 */
function waitForReplication(url, validateFn, maxAttempts = MAX_POLL_ATTEMPTS, intervalMs = POLL_INTERVAL_MS) {
  const startTime = Date.now();
  
  for (let attempt = 1; attempt <= maxAttempts; attempt++) {
    const response = http.get(url);
    
    if (response.status === 200) {
      try {
        const data = JSON.parse(response.body);
        const isValid = validateFn(data);
        
        if (isValid) {
          const latency = Date.now() - startTime;
          console.log(`✓ Replicação confirmada após ${attempt} tentativa(s), ${latency}ms`);
          return { 
            success: true, 
            attempts: attempt, 
            latency: latency,
            data: data 
          };
        }
      } catch (e) {
        console.warn(`Erro ao validar resposta (tentativa ${attempt}): ${e}`);
      }
    }
    
    if (attempt < maxAttempts) {
      sleep(intervalMs / 1000);
    }
  }
  
  const latency = Date.now() - startTime;
  console.error(`✗ Replicação falhou após ${maxAttempts} tentativas, ${latency}ms`);
  return { success: false, attempts: maxAttempts, latency: latency, data: null };
}

/**
 * Teste 1: Pessoa criada em Graduação deve ser replicada para todos os serviços
 */
function testPessoaReplication() {
  console.log('\n=== Teste 1: Replicação de Pessoa (Graduação → Todos) ===');
  
  // 1. Criar pessoa em Graduação
  const pessoaPayload = JSON.stringify({
    nome: `Test User ${Date.now()}`,
    dataNascimento: '1995-05-15',
    nomeSocial: null,
    documentoIdentificacao: {
      tipo: 'CPF',
      numero: `${Math.floor(Math.random() * 90000000000) + 10000000000}`
    },
    contato: {
      email: `test${Date.now()}@example.com`,
      telefone: '11987654321'
    },
    endereco: {
      logradouro: 'Rua Teste, 123',
      cidade: 'São Paulo',
      uf: 'SP',
      cep: '01234-567'
    }
  });
  
  const createResponse = http.post(
    `${BASE_URLS.graduacao}/pessoas`,
    pessoaPayload,
    { headers: { 'Content-Type': 'application/json' } }
  );
  
  const createCheck = check(createResponse, {
    'Pessoa criada com sucesso': (r) => r.status === 201,
    'Pessoa possui ID': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.id !== undefined;
      } catch {
        return false;
      }
    },
  });
  
  if (!createCheck) {
    console.error('Falha ao criar pessoa em Graduação');
    replicationFailure.add(1);
    return;
  }
  
  const pessoaCriada = JSON.parse(createResponse.body);
  const pessoaId = pessoaCriada.id;
  console.log(`Pessoa criada com ID: ${pessoaId}`);
  
  // 2. Verificar replicação em cada serviço
  const servicesToCheck = ['posGraduacao', 'diplomas', 'assinatura'];
  let allReplicationSuccess = true;
  
  for (const service of servicesToCheck) {
    console.log(`\n  Verificando replicação em ${service}...`);
    
    const result = waitForReplication(
      `${BASE_URLS[service]}/pessoas/${pessoaId}`,
      (data) => {
        // Validar que os dados essenciais foram replicados corretamente
        return data.id === pessoaId &&
               data.nome === pessoaCriada.nome &&
               data.dataNascimento === pessoaCriada.dataNascimento;
      }
    );
    
    replicationLatency.add(result.latency);
    
    const replicationCheck = check(result, {
      [`${service}: Pessoa replicada com sucesso`]: (r) => r.success,
      [`${service}: Dados consistentes`]: (r) => {
        if (!r.data) return false;
        return r.data.nome === pessoaCriada.nome;
      },
    });
    
    if (replicationCheck) {
      replicationSuccess.add(1);
      console.log(`  ✓ ${service}: OK (${result.latency}ms)`);
    } else {
      replicationFailure.add(1);
      allReplicationSuccess = false;
      console.error(`  ✗ ${service}: FALHOU`);
    }
  }
  
  return { pessoaId, allReplicationSuccess };
}

/**
 * Teste 2: VínculoAcademico criado deve ser replicado
 */
function testVinculoAcademicoReplication(pessoaId) {
  console.log('\n=== Teste 2: Replicação de VínculoAcademico ===');
  
  if (!pessoaId) {
    console.warn('Teste 2 pulado: pessoaId não fornecido');
    return;
  }
  
  // Buscar um curso disponível
  const cursosResponse = http.get(`${BASE_URLS.graduacao}/cursos`);
  if (cursosResponse.status !== 200) {
    console.error('Falha ao buscar cursos');
    replicationFailure.add(1);
    return;
  }
  
  const cursos = JSON.parse(cursosResponse.body);
  if (!cursos || cursos.length === 0) {
    console.error('Nenhum curso disponível');
    replicationFailure.add(1);
    return;
  }
  
  const curso = cursos[0];
  console.log(`Usando curso: ${curso.codigo} - ${curso.nome}`);
  
  // Buscar turmas disponíveis do curso
  const turmasResponse = http.get(`${BASE_URLS.graduacao}/cursos/${curso.id}/turmas`);
  if (turmasResponse.status !== 200) {
    console.error('Falha ao buscar turmas');
    replicationFailure.add(1);
    return;
  }
  
  const turmas = JSON.parse(turmasResponse.body);
  if (!turmas || turmas.length === 0) {
    console.error('Nenhuma turma disponível para o curso');
    replicationFailure.add(1);
    return;
  }
  
  const turma = turmas[0];
  console.log(`Usando turma: ${turma.id}`);
  
  // Criar aluno (que gera VínculoAcademico)
  const alunoPayload = JSON.stringify({
    pessoaId: pessoaId,
    turmaId: turma.id,
    dataMatricula: new Date().toISOString().split('T')[0],
    status: 'ATIVO'
  });
  
  const createResponse = http.post(
    `${BASE_URLS.graduacao}/alunos`,
    alunoPayload,
    { headers: { 'Content-Type': 'application/json' } }
  );
  
  if (createResponse.status !== 201) {
    console.error(`Falha ao criar aluno: ${createResponse.status}`);
    console.error(`Response body: ${createResponse.body}`);
    replicationFailure.add(1);
    return;
  }
  
  const aluno = JSON.parse(createResponse.body);
  console.log(`Aluno criado com ID: ${aluno.id}`);
  
  // Verificar replicação do VínculoAcademico em Diplomas
  console.log('\n  Verificando replicação em diplomas...');
  
  const result = waitForReplication(
    `${BASE_URLS.diplomas}/vinculos`,
    (data) => {
      // Verifica se existe ao menos um vínculo para a pessoa
      return Array.isArray(data) && data.length > 0 &&
             data.some(v => v.pessoaId === pessoaId);
    }
  );
  
  replicationLatency.add(result.latency);
  
  const replicationCheck = check(result, {
    'Diplomas: VínculoAcademico replicado': (r) => r.success,
  });
  
  if (replicationCheck) {
    replicationSuccess.add(1);
    console.log(`  ✓ diplomas: OK (${result.latency}ms)`);
  } else {
    replicationFailure.add(1);
    console.error(`  ✗ diplomas: FALHOU`);
  }
}

/**
 * Teste 3: Atualização de Pessoa deve ser replicada
 */
function testPessoaUpdateReplication(pessoaId) {
  console.log('\n=== Teste 3: Replicação de Atualização de Pessoa ===');
  
  if (!pessoaId) {
    console.warn('Teste 3 pulado: pessoaId não fornecido');
    return;
  }
  
  // Primeiro, buscar os dados atuais da pessoa
  const getResponse = http.get(`${BASE_URLS.graduacao}/pessoas/${pessoaId}`);
  if (getResponse.status !== 200) {
    console.error('Falha ao buscar pessoa para atualização');
    replicationFailure.add(1);
    return;
  }
  
  const pessoaAtual = JSON.parse(getResponse.body);
  const novoNomeSocial = `Nome Social ${Date.now()}`;
  
  // Atualizar pessoa com todos os campos (PUT requer todos os dados)
  const updatePayload = JSON.stringify({
    nome: pessoaAtual.nome,
    dataNascimento: pessoaAtual.dataNascimento,
    nomeSocial: novoNomeSocial,
    documentoIdentificacao: pessoaAtual.documentoIdentificacao,
    contato: pessoaAtual.contato,
    endereco: pessoaAtual.endereco
  });
  
  const updateResponse = http.put(
    `${BASE_URLS.graduacao}/pessoas/${pessoaId}`,
    updatePayload,
    { headers: { 'Content-Type': 'application/json' } }
  );
  
  const updateCheck = check(updateResponse, {
    'Pessoa atualizada com sucesso': (r) => r.status === 200,
  });
  
  if (!updateCheck) {
    console.error(`Falha ao atualizar pessoa. Status: ${updateResponse.status}`);
    replicationFailure.add(1);
    return;
  }
  
  console.log(`Pessoa ${pessoaId} atualizada com nomeSocial: ${novoNomeSocial}`);
  
  // Verificar replicação da atualização em Diplomas
  console.log('\n  Verificando replicação da atualização em diplomas...');
  
  const result = waitForReplication(
    `${BASE_URLS.diplomas}/pessoas/${pessoaId}`,
    (data) => {
      return data.nomeSocial === novoNomeSocial;
    }
  );
  
  replicationLatency.add(result.latency);
  
  const replicationCheck = check(result, {
    'Diplomas: Atualização de Pessoa replicada': (r) => r.success,
    'Diplomas: nomeSocial atualizado corretamente': (r) => {
      return r.data && r.data.nomeSocial === novoNomeSocial;
    },
  });
  
  if (replicationCheck) {
    replicationSuccess.add(1);
    console.log(`  ✓ diplomas: OK (${result.latency}ms)`);
  } else {
    replicationFailure.add(1);
    console.error(`  ✗ diplomas: FALHOU`);
  }
}

/**
 * Função principal de teste
 */
export default function () {
  console.log('='.repeat(60));
  console.log('INÍCIO DOS TESTES DE REPLICAÇÃO');
  console.log('='.repeat(60));
  console.log(`Timeout de replicação: ${REPLICATION_TIMEOUT_MS}ms`);
  console.log(`Intervalo de polling: ${POLL_INTERVAL_MS}ms`);
  console.log('='.repeat(60));
  
  // Teste 1: Replicação de Pessoa
  const teste1Result = testPessoaReplication();
  
  if (teste1Result && teste1Result.allReplicationSuccess) {
    // Teste 2: Replicação de VínculoAcademico
    sleep(1);
    testVinculoAcademicoReplication(teste1Result.pessoaId);
    
    // Teste 3: Replicação de atualização
    sleep(1);
    testPessoaUpdateReplication(teste1Result.pessoaId);
  }
  
  console.log('\n' + '='.repeat(60));
  console.log('FIM DOS TESTES DE REPLICAÇÃO');
  console.log('='.repeat(60));
}

/**
 * Resumo dos resultados
 */
export function handleSummary(data) {
  const successCount = data.metrics.replication_success_total?.values.count || 0;
  const failureCount = data.metrics.replication_failure_total?.values.count || 0;
  const avgLatency = data.metrics.replication_latency_ms?.values.avg || 0;
  const p95Latency = data.metrics.replication_latency_ms?.values['p(95)'] || 0;
  const p99Latency = data.metrics.replication_latency_ms?.values['p(99)'] || 0;
  
  console.log('\n' + '='.repeat(60));
  console.log('RESUMO DOS TESTES DE REPLICAÇÃO');
  console.log('='.repeat(60));
  console.log(`✓ Replicações bem-sucedidas: ${successCount}`);
  console.log(`✗ Replicações falhadas: ${failureCount}`);
  console.log(`⌀ Latência média: ${avgLatency.toFixed(2)}ms`);
  console.log(`⌀ Latência P95: ${p95Latency.toFixed(2)}ms`);
  console.log(`⌀ Latência P99: ${p99Latency.toFixed(2)}ms`);
  console.log('='.repeat(60));
  
  return {
    'stdout': JSON.stringify(data, null, 2),
  };
}
