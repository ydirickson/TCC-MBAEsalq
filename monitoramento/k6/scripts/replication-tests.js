import http from 'k6/http';
import { check, sleep } from 'k6';
import {
  waitForReplication,
  registerReplicationOutcome,
  markReplicationFailure,
  readReplicationConfig,
} from '../helpers/replication.js';

// Configuração de URLs base
const BASE_URLS = {
  graduacao: __ENV.GRADUACAO_URL || 'http://localhost:8081',
  posGraduacao: __ENV.POS_GRADUACAO_URL || 'http://localhost:8082',
  diplomas: __ENV.DIPLOMAS_URL || 'http://localhost:8083',
  assinatura: __ENV.ASSINATURA_URL || 'http://localhost:8084',
};

// Configuração de timeout e polling
const replicationConfig = readReplicationConfig({
  defaults: {
    timeoutMs: 30000,
    pollIntervalMs: 500,
    mode: 'strict',
    sampleRate: 1,
  },
});
const REPLICATION_TIMEOUT_MS = replicationConfig.timeoutMs;
const POLL_INTERVAL_MS = replicationConfig.pollIntervalMs;
const MAX_POLL_ATTEMPTS = replicationConfig.maxAttempts;
const TESTE1_PESSOA_COUNT = 5;
const PESSOA_TARGET_SERVICES = ['posGraduacao', 'diplomas', 'assinatura'];

function buildTest1Summary() {
  const perTarget = {};

  for (const service of PESSOA_TARGET_SERVICES) {
    perTarget[service] = {
      expected: TESTE1_PESSOA_COUNT,
      success: 0,
      failure: 0,
    };
  }

  return {
    requestedPersons: TESTE1_PESSOA_COUNT,
    createdPersons: 0,
    failedPersonCreations: 0,
    createdPessoaIds: [],
    perTarget,
  };
}

let test1Summary = buildTest1Summary();

function printTest1SummaryTable() {
  const totalExpectedTest1 = TESTE1_PESSOA_COUNT * PESSOA_TARGET_SERVICES.length;
  const totalSuccessTest1 = PESSOA_TARGET_SERVICES
    .reduce((acc, target) => acc + test1Summary.perTarget[target].success, 0);
  const totalFailureTest1 = PESSOA_TARGET_SERVICES
    .reduce((acc, target) => acc + test1Summary.perTarget[target].failure, 0);

  console.log('\nTabela de sumário (Teste 1 - Pessoa):');
  console.log('| ENTIDADE | ORIGEM | DESTINO | QUANTIDADE | SUCESSO | FALHA |');
  console.log('| --- | --- | --- | --- | --- | --- |');
  for (const target of PESSOA_TARGET_SERVICES) {
    const serviceSummary = test1Summary.perTarget[target];
    console.log(`| PESSOA | GRADUACAO | ${target.toUpperCase()} | ${serviceSummary.expected} | ${serviceSummary.success} | ${serviceSummary.failure} |`);
  }
  console.log(`| PESSOA | GRADUACAO | TODOS | ${totalExpectedTest1} | ${totalSuccessTest1} | ${totalFailureTest1} |`);
  console.log(`Pessoas criadas em graduação (Teste 1): ${test1Summary.createdPersons}/${test1Summary.requestedPersons}`);
  console.log(`Falhas de criação em graduação (Teste 1): ${test1Summary.failedPersonCreations}`);
  console.log(`IDs criados (Teste 1): ${test1Summary.createdPessoaIds.join(', ') || 'nenhum'}`);
}

export const options = {
  scenarios: {
    replication_test: {
      executor: 'shared-iterations',
      vus: 1,
      iterations: 1,
      maxDuration: '12m',
    },
  },
  thresholds: {
    'checks': ['rate>0.95'],
    'http_req_duration': ['p(95)<5000'],
  },
};

/**
 * Teste 1: Pessoa criada em Graduação deve ser replicada para todos os serviços
 */
function testPessoaReplication() {
  console.log(`\n=== Teste 1: Replicação de Pessoa (Graduação → Todos, ${TESTE1_PESSOA_COUNT} pessoas) ===`);

  let allReplicationSuccess = true;

  for (let i = 1; i <= TESTE1_PESSOA_COUNT; i += 1) {
    console.log(`\n--- Pessoa ${i}/${TESTE1_PESSOA_COUNT} ---`);
    const personTimestamp = Date.now();
    const pessoaPayload = JSON.stringify({
      nome: `Test User ${personTimestamp}-${i}`,
      dataNascimento: '1995-05-15',
      nomeSocial: null,
      documentoIdentificacao: {
        tipo: 'CPF',
        numero: `${Math.floor(Math.random() * 90000000000) + 10000000000}`,
      },
      contato: {
        email: `test${personTimestamp}-${i}@example.com`,
        telefone: '11987654321',
      },
      endereco: {
        logradouro: 'Rua Teste, 123',
        cidade: 'São Paulo',
        uf: 'SP',
        cep: '01234-567',
      },
    });

    const createResponse = http.post(
      `${BASE_URLS.graduacao}/pessoas`,
      pessoaPayload,
      { headers: { 'Content-Type': 'application/json' } },
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
      console.error(`Falha ao criar pessoa ${i} em Graduação`);
      test1Summary.failedPersonCreations += 1;
      allReplicationSuccess = false;

      for (const service of PESSOA_TARGET_SERVICES) {
        test1Summary.perTarget[service].failure += 1;
      }

      markReplicationFailure({ entity: 'pessoa', source: 'graduacao' });
      continue;
    }

    let pessoaCriada;
    try {
      pessoaCriada = JSON.parse(createResponse.body);
    } catch {
      console.error(`Falha ao interpretar resposta da criação da pessoa ${i}`);
      test1Summary.failedPersonCreations += 1;
      allReplicationSuccess = false;

      for (const service of PESSOA_TARGET_SERVICES) {
        test1Summary.perTarget[service].failure += 1;
      }

      markReplicationFailure({ entity: 'pessoa', source: 'graduacao' });
      continue;
    }

    const pessoaId = pessoaCriada.id;
    test1Summary.createdPersons += 1;
    test1Summary.createdPessoaIds.push(pessoaId);
    console.log(`Pessoa criada com ID: ${pessoaId}`);

    for (const service of PESSOA_TARGET_SERVICES) {
      console.log(`  Verificando replicação em ${service}...`);

      const result = waitForReplication({
        url: `${BASE_URLS[service]}/pessoas/${pessoaId}`,
        validateFn: (data) => {
          // Validar que os dados essenciais foram replicados corretamente.
          return data.id === pessoaId
            && data.nome === pessoaCriada.nome
            && data.dataNascimento === pessoaCriada.dataNascimento;
        },
        maxAttempts: MAX_POLL_ATTEMPTS,
        intervalMs: POLL_INTERVAL_MS,
      });

      const replicationCheck = registerReplicationOutcome({
        result,
        successLabel: `${service}: Pessoa replicada com sucesso`,
        dataLabel: `${service}: Dados consistentes`,
        dataValidator: (data) => data.nome === pessoaCriada.nome,
        tags: {
          entity: 'pessoa',
          source: 'graduacao',
          target: service,
        },
      });

      if (replicationCheck) {
        test1Summary.perTarget[service].success += 1;
        console.log(`  ✓ ${service}: OK (${result.latency}ms)`);
      } else {
        test1Summary.perTarget[service].failure += 1;
        allReplicationSuccess = false;
        console.error(`  ✗ ${service}: FALHOU`);
      }
    }
  }

  return {
    pessoaId: test1Summary.createdPessoaIds[0],
    pessoaIds: test1Summary.createdPessoaIds,
    allReplicationSuccess,
  };
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
    markReplicationFailure({ entity: 'vinculo', source: 'graduacao' });
    return;
  }
  
  const cursos = JSON.parse(cursosResponse.body);
  if (!cursos || cursos.length === 0) {
    console.error('Nenhum curso disponível');
    markReplicationFailure({ entity: 'vinculo', source: 'graduacao' });
    return;
  }
  
  const curso = cursos[0];
  console.log(`Usando curso: ${curso.codigo} - ${curso.nome}`);
  
  // Buscar turmas disponíveis do curso
  const turmasResponse = http.get(`${BASE_URLS.graduacao}/cursos/${curso.id}/turmas`);
  if (turmasResponse.status !== 200) {
    console.error('Falha ao buscar turmas');
    markReplicationFailure({ entity: 'vinculo', source: 'graduacao' });
    return;
  }
  
  const turmas = JSON.parse(turmasResponse.body);
  if (!turmas || turmas.length === 0) {
    console.error('Nenhuma turma disponível para o curso');
    markReplicationFailure({ entity: 'vinculo', source: 'graduacao' });
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
    markReplicationFailure({ entity: 'vinculo', source: 'graduacao' });
    return;
  }
  
  const aluno = JSON.parse(createResponse.body);
  console.log(`Aluno criado com ID: ${aluno.id}`);
  
  // Verificar replicação do VínculoAcademico em Diplomas
  console.log('\n  Verificando replicação em diplomas...');
  
  const result = waitForReplication({
    url: `${BASE_URLS.diplomas}/vinculos`,
    validateFn: (data) => {
      // Verifica se existe ao menos um vínculo para a pessoa.
      return Array.isArray(data) && data.length > 0
        && data.some((v) => v.pessoaId === pessoaId);
    },
    maxAttempts: MAX_POLL_ATTEMPTS,
    intervalMs: POLL_INTERVAL_MS,
  });

  const replicationCheck = registerReplicationOutcome({
    result,
    successLabel: 'Diplomas: VínculoAcademico replicado',
    tags: {
      entity: 'vinculo',
      source: 'graduacao',
      target: 'diplomas',
    },
  });
  
  if (replicationCheck) {
    console.log(`  ✓ diplomas: OK (${result.latency}ms)`);
  } else {
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
    markReplicationFailure({ entity: 'pessoa_update', source: 'graduacao' });
    return;
  }
  
  const pessoaAtual = JSON.parse(getResponse.body);
  const novoNomeSocial = `Nome Social ${Date.now()}`;
  
  // Atualizar pessoa
  const updatePayload = JSON.stringify({
    nome: pessoaAtual.nome,
    dataNascimento: pessoaAtual.dataNascimento,
    nomeSocial: novoNomeSocial,
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
    markReplicationFailure({ entity: 'pessoa_update', source: 'graduacao' });
    return;
  }
  
  console.log(`Pessoa ${pessoaId} atualizada com nomeSocial: ${novoNomeSocial}`);
  
  // Verificar replicação da atualização em Diplomas
  console.log('\n  Verificando replicação da atualização em diplomas...');
  
  const result = waitForReplication({
    url: `${BASE_URLS.diplomas}/pessoas/${pessoaId}`,
    validateFn: (data) => data.nomeSocial === novoNomeSocial,
    maxAttempts: MAX_POLL_ATTEMPTS,
    intervalMs: POLL_INTERVAL_MS,
  });

  const replicationCheck = registerReplicationOutcome({
    result,
    successLabel: 'Diplomas: Atualização de Pessoa replicada',
    dataLabel: 'Diplomas: nomeSocial atualizado corretamente',
    dataValidator: (data) => data.nomeSocial === novoNomeSocial,
    tags: {
      entity: 'pessoa_update',
      source: 'graduacao',
      target: 'diplomas',
    },
  });
  
  if (replicationCheck) {
    console.log(`  ✓ diplomas: OK (${result.latency}ms)`);
  } else {
    console.error(`  ✗ diplomas: FALHOU`);
  }
}

/**
 * Função principal de teste
 */
export default function () {
  test1Summary = buildTest1Summary();

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

  printTest1SummaryTable();
  
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
  
  // Não enviar o JSON completo para stdout; isso polui a saída final.
  return {};
}
