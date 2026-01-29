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

// Script de teste inicial do servico de graduacao (CRUD + fluxo basico).
const config = loadConfig();
const scenario = envValue('SCENARIO', 'graduacao-crud');
const runId = envValue('RUN_ID', `run_${Date.now()}`);
const baseUrl = envValue('GRADUACAO_BASE_URL', config.baseUrls && config.baseUrls.graduacao);

if (!baseUrl) {
  fail('Base URL não configurada para graduacao. Use GRADUACAO_BASE_URL ou config baseUrls.graduacao.');
}

// Duração total do fluxo de graduação por iteração (métrica de tempo de resposta de ponta a ponta).
const flowDuration = new Trend('graduacao_flow_duration', true);

export const options = buildOptions({ envValue, envNumber, envJson, runId, scenario });

function params(endpoint) {
  return {
    headers: {
      'Content-Type': 'application/json',
      'X-Run-Id': runId,
      'X-Scenario': scenario,
    },
    tags: {
      service: 'graduacao',
      endpoint,
    },
  };
}

export default function () {
  const startedAt = Date.now();
  const seed = uniqueSeed(__VU, __ITER) + (hashString(runId) % 100000);
  const today = isoDate(new Date());
  const nascimento = '1995-01-01';

  // 1) Curso
  const cursoCodigo = alphaCode(seed, 5);
  const cursoPayload = JSON.stringify({
    codigo: cursoCodigo,
    nome: `Curso ${cursoCodigo}`,
    cargaHoraria: 3200,
  });
  const cursoRes = http.post(`${baseUrl}/cursos`, cursoPayload, params('POST /cursos'));
  expect2xx(cursoRes, 'POST /cursos', fail, check);
  const cursoId = cursoRes.json('id');

  // 2) Turma (vinculada ao curso)
  const turmaPayload = JSON.stringify({
    ano: new Date().getFullYear(),
    semestre: 1,
    status: 'ATIVA',
  });
  const turmaRes = http.post(`${baseUrl}/cursos/${cursoId}/turmas`, turmaPayload, params('POST /cursos/{cursoId}/turmas'));
  expect2xx(turmaRes, 'POST /cursos/{cursoId}/turmas', fail, check);
  const turmaId = turmaRes.json('id');

  // 3) Pessoa para aluno
  const pessoaAlunoPayload = JSON.stringify({
    nome: `Aluno ${cursoCodigo}`,
    dataNascimento: nascimento,
    nomeSocial: null,
  });
  const pessoaAlunoRes = http.post(`${baseUrl}/pessoas`, pessoaAlunoPayload, params('POST /pessoas'));
  expect2xx(pessoaAlunoRes, 'POST /pessoas (aluno)', fail, check);
  const pessoaAlunoId = pessoaAlunoRes.json('id');

  // 4) Aluno
  const alunoPayload = JSON.stringify({
    pessoaId: pessoaAlunoId,
    turmaId: turmaId,
    dataMatricula: today,
    status: 'ATIVO',
  });
  const alunoRes = http.post(`${baseUrl}/alunos`, alunoPayload, params('POST /alunos'));
  expect2xx(alunoRes, 'POST /alunos', fail, check);
  const alunoId = alunoRes.json('id');

  // 5) Pessoa para professor
  const pessoaProfessorPayload = JSON.stringify({
    nome: `Professor ${cursoCodigo}`,
    dataNascimento: '1980-01-01',
    nomeSocial: null,
  });
  const pessoaProfessorRes = http.post(`${baseUrl}/pessoas`, pessoaProfessorPayload, params('POST /pessoas'));
  expect2xx(pessoaProfessorRes, 'POST /pessoas (professor)', fail, check);
  const pessoaProfessorId = pessoaProfessorRes.json('id');

  // 6) Professor
  const professorPayload = JSON.stringify({
    pessoaId: pessoaProfessorId,
    cursoId: cursoId,
    dataIngresso: today,
    nivelDocente: 'DOUTOR',
    status: 'ATIVO',
  });
  const professorRes = http.post(`${baseUrl}/professores`, professorPayload, params('POST /professores'));
  expect2xx(professorRes, 'POST /professores', fail, check);
  const professorId = professorRes.json('id');

  // 7) Disciplina
  const disciplinaCodigo = `${alphaCode(seed, 3)}${String(seed % 10000).padStart(4, '0')}`;
  const disciplinaPayload = JSON.stringify({
    codigo: disciplinaCodigo,
    nome: `Disciplina ${disciplinaCodigo}`,
    cargaHoraria: 60,
  });
  const disciplinaRes = http.post(`${baseUrl}/cursos/${cursoId}/disciplinas`, disciplinaPayload, params('POST /cursos/{cursoId}/disciplinas'));
  expect2xx(disciplinaRes, 'POST /cursos/{cursoId}/disciplinas', fail, check);
  const disciplinaId = disciplinaRes.json('id');

  // 8) Oferta de disciplina
  const ofertaPayload = JSON.stringify({
    disciplinaId: disciplinaId,
    professorId: professorId,
    ano: new Date().getFullYear(),
    semestre: 1,
  });
  const ofertaRes = http.post(`${baseUrl}/ofertas-disciplinas`, ofertaPayload, params('POST /ofertas-disciplinas'));
  expect2xx(ofertaRes, 'POST /ofertas-disciplinas', fail, check);
  const ofertaId = ofertaRes.json('id');

  // 9) Matricula
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
  const cursosRes = http.get(`${baseUrl}/cursos`, params('GET /cursos'));
  expect2xx(cursosRes, 'GET /cursos', fail, check);

  const alunosRes = http.get(`${baseUrl}/alunos`, params('GET /alunos'));
  expect2xx(alunosRes, 'GET /alunos', fail, check);

  const ofertasRes = http.get(`${baseUrl}/ofertas-disciplinas?disciplinaId=${disciplinaId}`, params('GET /ofertas-disciplinas'));
  expect2xx(ofertasRes, 'GET /ofertas-disciplinas', fail, check);

  flowDuration.add(Date.now() - startedAt, { service: 'graduacao' });
  const sleepSeconds = envNumber('SLEEP_S', envNumber('K6_SLEEP_S', 1));
  if (sleepSeconds > 0) {
    sleep(sleepSeconds);
  }
}
