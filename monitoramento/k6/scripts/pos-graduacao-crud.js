import http from 'k6/http';
import { check, sleep, fail } from 'k6';
import { Trend } from 'k6/metrics';
import { loadEnvFile, makeEnvReaders } from '../helpers/dotenv.js';
import { loadConfig } from '../helpers/config.js';
import { buildOptions } from '../helpers/options.js';
import { alphaCode, uniqueSeed, hashString, isoDate, expect2xx } from '../helpers/util.js';

const envFilePath = __ENV.ENV_FILE || '.env';
const fileEnv = loadEnvFile([envFilePath]);
const { envValue, envNumber, envJson } = makeEnvReaders({ envVars: __ENV, envFile: fileEnv });

// Script de teste inicial do servico de pos-graduacao (CRUD + fluxo basico).
const config = loadConfig();
const scenario = envValue('SCENARIO', 'pos-graduacao-crud');
const runId = envValue('RUN_ID', `run_${Date.now()}`);
const baseUrl = envValue('POS_GRADUACAO_BASE_URL', config.baseUrls && config.baseUrls.posGraduacao);

if (!baseUrl) {
  fail('Base URL não configurada para pos-graduacao. Use POS_GRADUACAO_BASE_URL ou config baseUrls.posGraduacao.');
}

// Duração total do fluxo de pós-graduação por iteração (métrica de tempo de resposta de ponta a ponta).
const flowDuration = new Trend('pos_graduacao_flow_duration', true);

export const options = buildOptions({ envValue, envNumber, envJson, runId, scenario });

function params(endpoint) {
  return {
    headers: {
      'Content-Type': 'application/json',
      'X-Run-Id': runId,
      'X-Scenario': scenario,
    },
    tags: {
      service: 'pos-graduacao',
      endpoint,
    },
  };
}

export default function () {
  const startedAt = Date.now();
  const seed = uniqueSeed(__VU, __ITER) + (hashString(runId) % 100000);
  const today = isoDate(new Date());
  const nascimento = '1995-01-01';

  // 1) Programa de Pós-Graduação
  const programaCodigo = alphaCode(seed, 4);
  const programaPayload = JSON.stringify({
    codigo: programaCodigo,
    nome: `Programa ${programaCodigo}`,
    cargaHoraria: 2400,
  });
  const programaRes = http.post(`${baseUrl}/programas`, programaPayload, params('POST /programas'));
  expect2xx(programaRes, 'POST /programas', fail, check);
  const programaId = programaRes.json('id');

  // 2) Pessoa para orientador/professor
  const pessoaProfessorPayload = JSON.stringify({
    nome: `Professor ${programaCodigo}`,
    dataNascimento: '1980-01-01',
    nomeSocial: null,
  });
  const pessoaProfessorRes = http.post(`${baseUrl}/pessoas`, pessoaProfessorPayload, params('POST /pessoas'));
  expect2xx(pessoaProfessorRes, 'POST /pessoas (professor)', fail, check);
  const pessoaProfessorId = pessoaProfessorRes.json('id');

  // 3) Professor de Pós-Graduação
  const professorPayload = JSON.stringify({
    pessoaId: pessoaProfessorId,
    programaId: programaId,
    dataIngresso: today,
    nivelDocente: 'DOUTOR',
    status: 'ATIVO',
  });
  const professorRes = http.post(`${baseUrl}/professores`, professorPayload, params('POST /professores'));
  expect2xx(professorRes, 'POST /professores', fail, check);
  const professorId = professorRes.json('id');

  // 4) Pessoa para aluno
  const pessoaAlunoPayload = JSON.stringify({
    nome: `Aluno ${programaCodigo}`,
    dataNascimento: nascimento,
    nomeSocial: null,
  });
  const pessoaAlunoRes = http.post(`${baseUrl}/pessoas`, pessoaAlunoPayload, params('POST /pessoas'));
  expect2xx(pessoaAlunoRes, 'POST /pessoas (aluno)', fail, check);
  const pessoaAlunoId = pessoaAlunoRes.json('id');

  // 5) Aluno de Pós-Graduação
  const alunoPayload = JSON.stringify({
    pessoaId: pessoaAlunoId,
    programaId: programaId,
    orientadorId: professorId,
    dataMatricula: today,
    dataConclusao: null,
    status: 'ATIVO',
  });
  const alunoRes = http.post(`${baseUrl}/alunos`, alunoPayload, params('POST /alunos'));
  expect2xx(alunoRes, 'POST /alunos', fail, check);
  const alunoId = alunoRes.json('id');

  // 6) Disciplina
  const disciplinaCodigo = `${alphaCode(seed, 3)}${String(seed % 10000).padStart(4, '0')}`;
  const disciplinaPayload = JSON.stringify({
    codigo: disciplinaCodigo,
    nome: `Disciplina ${disciplinaCodigo}`,
    cargaHoraria: 60,
  });
  const disciplinaRes = http.post(`${baseUrl}/programas/${programaId}/disciplinas`, disciplinaPayload, params('POST /programas/{programaId}/disciplinas'));
  expect2xx(disciplinaRes, 'POST /programas/{programaId}/disciplinas', fail, check);
  const disciplinaId = disciplinaRes.json('id');

  // 7) Oferta de disciplina
  const ofertaPayload = JSON.stringify({
    disciplinaId: disciplinaId,
    professorId: professorId,
    ano: new Date().getFullYear(),
    semestre: 1,
  });
  const ofertaRes = http.post(`${baseUrl}/ofertas-disciplinas`, ofertaPayload, params('POST /ofertas-disciplinas'));
  expect2xx(ofertaRes, 'POST /ofertas-disciplinas', fail, check);
  const ofertaId = ofertaRes.json('id');

  // 8) Matricula
  const matriculaPayload = JSON.stringify({
    alunoId: alunoId,
    ofertaDisciplinaId: ofertaId,
    dataMatricula: today,
    status: 'MATRICULADO',
    nota: 0.0,
  });
  const matriculaRes = http.post(`${baseUrl}/matriculas`, matriculaPayload, params('POST /matriculas'));
  expect2xx(matriculaRes, 'POST /matriculas', fail, check);

  // Leituras simples para validar GETs basicos
  const programasRes = http.get(`${baseUrl}/programas`, params('GET /programas'));
  expect2xx(programasRes, 'GET /programas', fail, check);

  const alunosRes = http.get(`${baseUrl}/alunos`, params('GET /alunos'));
  expect2xx(alunosRes, 'GET /alunos', fail, check);

  const ofertasRes = http.get(`${baseUrl}/ofertas-disciplinas?disciplinaId=${disciplinaId}`, params('GET /ofertas-disciplinas'));
  expect2xx(ofertasRes, 'GET /ofertas-disciplinas', fail, check);

  flowDuration.add(Date.now() - startedAt, { service: 'pos-graduacao' });
  const sleepSeconds = envNumber('SLEEP_S', envNumber('K6_SLEEP_S', 1));
  if (sleepSeconds > 0) {
    sleep(sleepSeconds);
  }
}
