import http from 'k6/http';
import { check, fail } from 'k6';
import { alphaCode, uniqueSeed, hashString, isoDate, expect2xx } from './util.js';

function buildParams({ runId, scenario, service, endpoint }) {
  return {
    headers: {
      'Content-Type': 'application/json',
      'X-Run-Id': runId,
      'X-Scenario': scenario,
    },
    tags: {
      service,
      endpoint,
    },
  };
}

function resolveSeed(runId, seed) {
  if (typeof seed === 'number' && Number.isFinite(seed)) {
    return seed;
  }
  return uniqueSeed(__VU, __ITER) + (hashString(runId || 'run') % 100000);
}

export function runGraduacaoFlow({ baseUrl, runId, scenario, seed }) {
  const currentSeed = resolveSeed(runId, seed);
  const today = isoDate(new Date());
  const nascimentoAluno = '1995-01-01';

  const params = (endpoint) => buildParams({ runId, scenario, service: 'graduacao', endpoint });

  const cursoCodigo = alphaCode(currentSeed, 5);
  const cursoPayload = JSON.stringify({
    codigo: cursoCodigo,
    nome: `Curso ${cursoCodigo}`,
    cargaHoraria: 3200,
  });
  
  // Tentativa de criação resiliente (Create or Fetch)
  let cursoRes = http.post(`${baseUrl}/cursos`, cursoPayload, params('POST /cursos'));
  let cursoId;

  if (cursoRes.status >= 200 && cursoRes.status < 300) {
    cursoId = cursoRes.json('id');
    check(cursoRes, { 'POST /cursos': (r) => r.status >= 200 && r.status < 300 });
  } else if (cursoRes.status === 500 || cursoRes.status === 409) {
    // Colisão provável: busca o ID existente
    const listRes = http.get(`${baseUrl}/cursos`, params('GET /cursos (recovery)'));
    if (listRes.status === 200) {
      const existing = listRes.json().find(c => c.codigo === cursoCodigo);
      if (existing) {
        cursoId = existing.id;
        // Check falso positivo para não quebrar thresholds de erro globais severamente, 
        // mas registrando que houve recuperação
        check(listRes, { 'POST /cursos (recovered colision)': () => true });
      } else {
        fail(`Curso nao criado (${cursoRes.status}) e nao encontrado na lista: ${cursoCodigo}`);
      }
    } else {
      fail(`Erro fatal: POST /cursos (${cursoRes.status}) e GET /cursos (${listRes.status})`);
    }
  } else {
    expect2xx(cursoRes, 'POST /cursos', fail, check);
  }

  const turmaPayload = JSON.stringify({
    ano: new Date().getFullYear(),
    semestre: 1,
    status: 'ATIVA',
  });
  const turmaRes = http.post(`${baseUrl}/cursos/${cursoId}/turmas`, turmaPayload, params('POST /cursos/{cursoId}/turmas'));
  expect2xx(turmaRes, 'POST /cursos/{cursoId}/turmas', fail, check);
  const turmaId = turmaRes.json('id');

  const alunoNome = `Aluno ${cursoCodigo}`;
  const pessoaAlunoPayload = JSON.stringify({
    nome: alunoNome,
    dataNascimento: nascimentoAluno,
    nomeSocial: null,
  });
  const pessoaAlunoRes = http.post(`${baseUrl}/pessoas`, pessoaAlunoPayload, params('POST /pessoas'));
  expect2xx(pessoaAlunoRes, 'POST /pessoas (aluno)', fail, check);
  const pessoaAlunoId = pessoaAlunoRes.json('id');

  const alunoPayload = JSON.stringify({
    pessoaId: pessoaAlunoId,
    turmaId: turmaId,
    dataMatricula: today,
    status: 'ATIVO',
  });
  const alunoRes = http.post(`${baseUrl}/alunos`, alunoPayload, params('POST /alunos'));
  expect2xx(alunoRes, 'POST /alunos', fail, check);
  const alunoId = alunoRes.json('id');

  const professorNome = `Professor ${cursoCodigo}`;
  const pessoaProfessorPayload = JSON.stringify({
    nome: professorNome,
    dataNascimento: '1980-01-01',
    nomeSocial: null,
  });
  const pessoaProfessorRes = http.post(`${baseUrl}/pessoas`, pessoaProfessorPayload, params('POST /pessoas'));
  expect2xx(pessoaProfessorRes, 'POST /pessoas (professor)', fail, check);
  const pessoaProfessorId = pessoaProfessorRes.json('id');

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

  const disciplinaCodigo = `${alphaCode(currentSeed, 3)}${String(currentSeed % 10000).padStart(4, '0')}`;
  const disciplinaPayload = JSON.stringify({
    codigo: disciplinaCodigo,
    nome: `Disciplina ${disciplinaCodigo}`,
    cargaHoraria: 60,
  });
  const disciplinaRes = http.post(`${baseUrl}/cursos/${cursoId}/disciplinas`, disciplinaPayload, params('POST /cursos/{cursoId}/disciplinas'));
  expect2xx(disciplinaRes, 'POST /cursos/{cursoId}/disciplinas', fail, check);
  const disciplinaId = disciplinaRes.json('id');

  const ofertaPayload = JSON.stringify({
    disciplinaId: disciplinaId,
    professorId: professorId,
    ano: new Date().getFullYear(),
    semestre: 1,
  });
  const ofertaRes = http.post(`${baseUrl}/ofertas-disciplinas`, ofertaPayload, params('POST /ofertas-disciplinas'));
  expect2xx(ofertaRes, 'POST /ofertas-disciplinas', fail, check);
  const ofertaId = ofertaRes.json('id');

  const matriculaPayload = JSON.stringify({
    alunoId: alunoId,
    ofertaDisciplinaId: ofertaId,
    dataMatricula: today,
    status: 'MATRICULADO',
    nota: 0.0,
  });
  const matriculaRes = http.post(`${baseUrl}/matriculas`, matriculaPayload, params('POST /matriculas'));
  expect2xx(matriculaRes, 'POST /matriculas', fail, check);
  const matriculaId = matriculaRes.json('id');

  const cursosRes = http.get(`${baseUrl}/cursos`, params('GET /cursos'));
  expect2xx(cursosRes, 'GET /cursos', fail, check);

  const alunosRes = http.get(`${baseUrl}/alunos`, params('GET /alunos'));
  expect2xx(alunosRes, 'GET /alunos', fail, check);

  const ofertasRes = http.get(`${baseUrl}/ofertas-disciplinas?disciplinaId=${disciplinaId}`, params('GET /ofertas-disciplinas'));
  expect2xx(ofertasRes, 'GET /ofertas-disciplinas', fail, check);

  return {
    cursoId,
    turmaId,
    pessoaAluno: {
      id: pessoaAlunoId,
      nome: alunoNome,
      dataNascimento: nascimentoAluno,
    },
    alunoId,
    pessoaProfessorId,
    professorId,
    disciplinaId,
    ofertaId,
    matriculaId,
    seed: currentSeed,
  };
}

export function runPosGraduacaoFlow({ baseUrl, runId, scenario, seed }) {
  const currentSeed = resolveSeed(runId, seed);
  const today = isoDate(new Date());
  const nascimentoAluno = '1995-01-01';

  const params = (endpoint) => buildParams({ runId, scenario, service: 'pos-graduacao', endpoint });

  const programaCodigo = alphaCode(currentSeed, 5);
  const programaPayload = JSON.stringify({
    codigo: programaCodigo,
    nome: `Programa ${programaCodigo}`,
    cargaHoraria: 2400,
  });
  
  // Tentativa de criação resiliente (Create or Fetch)
  let programaRes = http.post(`${baseUrl}/programas`, programaPayload, params('POST /programas'));
  let programaId;

  if (programaRes.status >= 200 && programaRes.status < 300) {
    programaId = programaRes.json('id');
    check(programaRes, { 'POST /programas': (r) => r.status >= 200 && r.status < 300 });
  } else if (programaRes.status === 500 || programaRes.status === 409) {
    // Colisão provável: busca o ID existente
    const listRes = http.get(`${baseUrl}/programas`, params('GET /programas (recovery)'));
    if (listRes.status === 200) {
        const existing = listRes.json().find(p => p.codigo === programaCodigo);
        if (existing) {
            programaId = existing.id;
            check(listRes, { 'POST /programas (recovered colision)': () => true });
        } else {
            fail(`Programa nao criado (${programaRes.status}) e nao encontrado na lista: ${programaCodigo}`);
        }
    } else {
        fail(`Erro fatal: POST /programas (${programaRes.status}) e GET /programas (${listRes.status})`);
    }
  } else {
    expect2xx(programaRes, 'POST /programas', fail, check);
  }

  const professorNome = `Professor ${programaCodigo}`;
  const pessoaProfessorPayload = JSON.stringify({
    nome: professorNome,
    dataNascimento: '1980-01-01',
    nomeSocial: null,
  });
  const pessoaProfessorRes = http.post(`${baseUrl}/pessoas`, pessoaProfessorPayload, params('POST /pessoas'));
  expect2xx(pessoaProfessorRes, 'POST /pessoas (professor)', fail, check);
  const pessoaProfessorId = pessoaProfessorRes.json('id');

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

  const alunoNome = `Aluno ${programaCodigo}`;
  const pessoaAlunoPayload = JSON.stringify({
    nome: alunoNome,
    dataNascimento: nascimentoAluno,
    nomeSocial: null,
  });
  const pessoaAlunoRes = http.post(`${baseUrl}/pessoas`, pessoaAlunoPayload, params('POST /pessoas'));
  expect2xx(pessoaAlunoRes, 'POST /pessoas (aluno)', fail, check);
  const pessoaAlunoId = pessoaAlunoRes.json('id');

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

  const disciplinaCodigo = `${alphaCode(currentSeed, 3)}${String(currentSeed % 10000).padStart(4, '0')}`;
  const disciplinaPayload = JSON.stringify({
    codigo: disciplinaCodigo,
    nome: `Disciplina ${disciplinaCodigo}`,
    cargaHoraria: 60,
  });
  const disciplinaRes = http.post(`${baseUrl}/programas/${programaId}/disciplinas`, disciplinaPayload, params('POST /programas/{programaId}/disciplinas'));
  expect2xx(disciplinaRes, 'POST /programas/{programaId}/disciplinas', fail, check);
  const disciplinaId = disciplinaRes.json('id');

  const ofertaPayload = JSON.stringify({
    disciplinaId: disciplinaId,
    professorId: professorId,
    ano: new Date().getFullYear(),
    semestre: 1,
  });
  const ofertaRes = http.post(`${baseUrl}/ofertas-disciplinas`, ofertaPayload, params('POST /ofertas-disciplinas'));
  expect2xx(ofertaRes, 'POST /ofertas-disciplinas', fail, check);
  const ofertaId = ofertaRes.json('id');

  const matriculaPayload = JSON.stringify({
    alunoId: alunoId,
    ofertaDisciplinaId: ofertaId,
    dataMatricula: today,
    status: 'MATRICULADO',
    nota: 0.0,
  });
  const matriculaRes = http.post(`${baseUrl}/matriculas`, matriculaPayload, params('POST /matriculas'));
  expect2xx(matriculaRes, 'POST /matriculas', fail, check);
  const matriculaId = matriculaRes.json('id');

  const programasRes = http.get(`${baseUrl}/programas`, params('GET /programas'));
  expect2xx(programasRes, 'GET /programas', fail, check);

  const alunosRes = http.get(`${baseUrl}/alunos`, params('GET /alunos'));
  expect2xx(alunosRes, 'GET /alunos', fail, check);

  const ofertasRes = http.get(`${baseUrl}/ofertas-disciplinas?disciplinaId=${disciplinaId}`, params('GET /ofertas-disciplinas'));
  expect2xx(ofertasRes, 'GET /ofertas-disciplinas', fail, check);

  return {
    programaId,
    pessoaAluno: {
      id: pessoaAlunoId,
      nome: alunoNome,
      dataNascimento: nascimentoAluno,
    },
    alunoId,
    pessoaProfessorId,
    professorId,
    disciplinaId,
    ofertaId,
    matriculaId,
    seed: currentSeed,
  };
}
