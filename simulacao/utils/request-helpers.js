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