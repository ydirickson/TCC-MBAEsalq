import http from 'k6/http';

export const getRequest = (url, tags) => {
  return http.get(
    url,
    {
      tags: tags
    }
  );
}

export const postRequest = (url, payload, tags) => {
  return http.post(
    url, 
    JSON.stringify(payload), 
    {
      headers: { 'Content-Type': 'application/json' },
      tags: tags
    }
  );
}

export const putRequest = (url, payload, tags) => {
  return http.put(
    url,
    JSON.stringify(payload),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: tags
    }
  );
}

export const criarPessoaRequest = (baseUrl, payload, servico) => {
  return postRequest(
    `${baseUrl}/pessoas`,
    payload,
    {
      acao: 'Criar Pessoa',
      servico: servico,
      name: `(${servico}) Criar Pessoa`,
    }
  );
}

export const obterPessoaRequest = (baseUrl, pessoaId, servico) => {
  return getRequest(
    `${baseUrl}/pessoas/${pessoaId}`,
    {
      acao: 'Obter Pessoa',
      servico: servico,
      name: `(${servico}) Obter Pessoa`,
    }
  );
}

export const atualizarPessoaRequest = (baseUrl, pessoaId, payload, servico) => {
  return putRequest(
    `${baseUrl}/pessoas/${pessoaId}`,
    payload,
    {
      acao: 'Atualizar Pessoa',
      servico: servico,
      name: `(${servico}) Atualizar Pessoa`,
    }
  );
}

export const listarCursosRequest = (baseUrl, servico) => {
  return getRequest(
    `${baseUrl}/cursos`,
    {
      acao: 'Listar Cursos',
      servico: servico,
      name: `(${servico}) Listar Cursos`,
    }
  );
}

export const listarTurmasPorCursoRequest = (baseUrl, cursoId, servico) => {
  return getRequest(
    `${baseUrl}/cursos/${cursoId}/turmas`,
    {
      acao: 'Listar Turmas por Curso',
      servico: servico,
      name: `(${servico}) Listar Turmas por Curso`,
    }
  );
}

export const criarAlunoRequest = (baseUrl, payload, servico) => {
  return postRequest(
    `${baseUrl}/alunos`,
    payload,
    {
      acao: 'Criar Aluno',
      servico: servico,
      name: `(${servico}) Criar Aluno`,
    }
  );
}

export const atualizarAlunoRequest = (baseUrl, alunoId, payload, servico) => {
  return putRequest(
    `${baseUrl}/alunos/${alunoId}`,
    payload,
    {
      acao: 'Atualizar Aluno',
      servico: servico,
      name: `(${servico}) Atualizar Aluno`,
    }
  );
}

export const listarVinculosPorPessoaRequest = (baseUrl, pessoaId, servico) => {
  return getRequest(
    `${baseUrl}/vinculos?pessoaId=${pessoaId}`,
    {
      acao: 'Listar Vinculos por Pessoa',
      servico: servico,
      name: `(${servico}) Listar Vinculos por Pessoa`,
    }
  );
}

export const listarRequerimentosPorPessoaRequest = (baseUrl, pessoaId, servico) => {
  return getRequest(
    `${baseUrl}/requerimentos?pessoaId=${pessoaId}`,
    {
      acao: 'Listar Requerimentos por Pessoa',
      servico: servico,
      name: `(${servico}) Listar Requerimentos por Pessoa`,
    }
  );
}