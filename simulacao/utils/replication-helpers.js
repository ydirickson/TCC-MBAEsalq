import { createPessoaPayload } from "./payload-factory.js";
import {
  atualizarAlunoRequest,
  atualizarPessoaRequest,
  atualizarSolicitacaoAssinaturaRequest,
  criarAlunoRequest,
  criarDocumentoDiplomaRequest,
  criarDiplomaRequest,
  criarPessoaRequest,
  criarSolicitacaoAssinaturaRequest,
  listarCursosRequest,
  listarDocumentosAssinaveis,
  listarProgramasRequest,
  listarRequerimentosPorPessoaRequest,
  listarSolicitacoesRequest,
  listarTurmasPorCursoRequest,
  listarVinculosPorPessoaRequest,
  obterPessoaRequest,
  obterStatusEmissaoRequest,
} from "./request-helpers.js";


import { SERVICOS } from "./constantes.js";
import { check, sleep } from "k6";
import { Trend } from "k6/metrics";

export const replicacaoLatencia = new Trend('replicacao_latencia_ms', true);

const REPLICATION_TIMEOUT_MS = Number(__ENV.REPLICATION_TIMEOUT_MS || 30000);
const POLL_INTERVAL_MS = Number(__ENV.POLL_INTERVAL_MS || 500);

const parseResponseJson = (response) => {
  try {
    return response.json();
  } catch {
    return null;
  }
};

const logErro = (descricao, response) => {
  console.error(`[ERRO] ${descricao} — status: ${response?.status} url: ${response?.url} body: ${response?.body} error: ${response?.error}`);
};

const aguardarReplicacao = ({ requestFn, validateFn, timeoutMs = REPLICATION_TIMEOUT_MS, pollIntervalMs = POLL_INTERVAL_MS }) => {
  const inicio = Date.now();
  let ultimaResposta = null;
  let ultimoData = null;

  while ((Date.now() - inicio) <= timeoutMs) {
    ultimaResposta = requestFn();
    ultimoData = parseResponseJson(ultimaResposta);

    if (ultimaResposta.status === 200 && validateFn(ultimoData)) {
      return {
        sucesso: true,
        response: ultimaResposta,
        data: ultimoData,
        latenciaMs: Date.now() - inicio,
      };
    }

    sleep(pollIntervalMs / 1000);
  }

  return {
    sucesso: false,
    response: ultimaResposta,
    data: ultimoData,
    latenciaMs: Date.now() - inicio,
  };
}

// Polling de múltiplos destinos em paralelo num único loop.
// Retorna um Map<key, { sucesso, response, data, latenciaMs }>.
const aguardarReplicacaoParalela = (destinos) => {
  const destinoMap = new Map(destinos.map((d) => [d.key, d]));
  const resultados = new Map(
    destinos.map(({ key }) => [key, { sucesso: false, response: null, data: null, latenciaMs: null }])
  );
  const pendentes = new Set(destinos.map(({ key }) => key));
  const inicio = Date.now();

  while (pendentes.size > 0 && (Date.now() - inicio) <= REPLICATION_TIMEOUT_MS) {
    for (const key of [...pendentes]) {
      const { requestFn, validateFn } = destinoMap.get(key);
      const resp = requestFn();
      const data = parseResponseJson(resp);

      if (resp.status === 200 && validateFn(data)) {
        pendentes.delete(key);
        resultados.set(key, { sucesso: true, response: resp, data, latenciaMs: Date.now() - inicio });
      } else {
        resultados.get(key).response = resp;
      }
    }

    if (pendentes.size > 0) sleep(POLL_INTERVAL_MS / 1000);
  }

  return resultados;
}

export const testeReplicacaoPessoa = (servicoOrigem, servicosDestino) => {
  const pessoaPayload = createPessoaPayload();
  const { url, nome } = SERVICOS[servicoOrigem];
  // 1- Cria uma pessoa (Verifica resposta) no serviço de origem
  const pessoaNova = criarPessoaRequest(url, pessoaPayload, nome);
  check(pessoaNova, {
    [`(Criar Pessoa - ${nome}) Status 201`]: (r) => r.status === 201,
  });

  if (pessoaNova.status !== 201) {
    logErro(`Criar Pessoa - ${nome}`, pessoaNova);
    return { sucessoGlobal: false, pessoaCriada: null };
  }

  const pessoaCriada = pessoaNova.json();
  let sucessoGlobal = true;

  // 2- Verifica que existe em todos os serviços destino (polling paralelo)
  const resultados = aguardarReplicacaoParalela(
    servicosDestino.map((servicoDestino) => {
      const { url: urlDestino, nome: nomeDestino } = SERVICOS[servicoDestino];
      return {
        key: servicoDestino,
        requestFn: () => obterPessoaRequest(urlDestino, pessoaCriada.id, nomeDestino),
        validateFn: (pessoaDestino) =>
          !!pessoaDestino &&
          pessoaDestino.id === pessoaCriada.id &&
          pessoaDestino.nome === pessoaCriada.nome &&
          pessoaDestino.dataNascimento === pessoaCriada.dataNascimento,
      };
    })
  );

  for (const [servicoDestino, resultado] of resultados) {
    const { nome: nomeDestino } = SERVICOS[servicoDestino];

    check(resultado.response || {}, {
      [`(${nome} -> ${nomeDestino}) Replicação concluída`]: () => resultado.sucesso,
    });

    if (!resultado.sucesso) {
      sucessoGlobal = false;
      continue;
    }

    // 3- Medir latência de replicação (replicadoEm - criadoEm)
    const pessoaDestino = resultado.data;
    if (pessoaDestino.replicadoEm && pessoaDestino.criadoEm) {
      const latenciaMs = new Date(pessoaDestino.replicadoEm) - new Date(pessoaDestino.criadoEm);
      replicacaoLatencia.add(latenciaMs, { origem: nome, destino: nomeDestino });
      console.log(`Latência de replicação ${nome} (${pessoaDestino.criadoEm}) -> ${nomeDestino} (${pessoaDestino.replicadoEm}): ${latenciaMs}ms`);
    } else {
      replicacaoLatencia.add(resultado.latenciaMs, { origem: nome, destino: nomeDestino });
      console.log(`Latência de replicação ${nome} -> ${nomeDestino}: ${resultado.latenciaMs}ms`);
    }
  }

  return { sucessoGlobal, pessoaCriada };
}

export const testeAtualizacaoPessoa = (servicoOrigem, servicosDestino, pessoaCriada) => {
  if (!pessoaCriada || !pessoaCriada.id) {
    return { sucessoGlobal: false };
  }

  const { url: urlOrigem, nome: nomeOrigem } = SERVICOS[servicoOrigem];
  const nomeSocialAtualizado = `Nome Social ${Date.now()}`;
  const payloadAtualizacao = {
    nome: pessoaCriada.nome,
    dataNascimento: pessoaCriada.dataNascimento,
    nomeSocial: nomeSocialAtualizado,
  };

  const atualizacao = atualizarPessoaRequest(urlOrigem, pessoaCriada.id, payloadAtualizacao, nomeOrigem);
  check(atualizacao, {
    [`(${nomeOrigem}) Atualizar Pessoa Status 200`]: (r) => r.status === 200
  });

  if (atualizacao.status !== 200) {
    logErro(`Atualizar Pessoa - ${nomeOrigem}`, atualizacao);
    return { sucessoGlobal: false };
  }

  let sucessoGlobal = true;

  const resultados = aguardarReplicacaoParalela(
    servicosDestino.map((servicoDestino) => {
      const { url: urlDestino, nome: nomeDestino } = SERVICOS[servicoDestino];
      return {
        key: servicoDestino,
        requestFn: () => obterPessoaRequest(urlDestino, pessoaCriada.id, nomeDestino),
        validateFn: (pessoaDestino) => !!pessoaDestino && pessoaDestino.nomeSocial === nomeSocialAtualizado,
      };
    })
  );

  for (const [servicoDestino, resultado] of resultados) {
    const { nome: nomeDestino } = SERVICOS[servicoDestino];

    check(resultado.response || {}, {
      [`(${nomeOrigem} -> ${nomeDestino}) Atualização replicada`]: () => resultado.sucesso,
    });

    if (!resultado.sucesso) {
      sucessoGlobal = false;
      continue;
    }

    replicacaoLatencia.add(resultado.latenciaMs, { origem: nomeOrigem, destino: nomeDestino });
  }

  return { sucessoGlobal, nomeSocialAtualizado };
}

export const testeReplicacaoVinculoAcademico = (pessoaId, servicoOrigem, servicoDestino) => {
  const { url: urlOrigem, nome: nomeOrigem } = SERVICOS[servicoOrigem];
  const { url: urlDestino, nome: nomeDestino } = SERVICOS[servicoDestino];

  const cursosResponse = listarCursosRequest(urlOrigem, nomeOrigem);
  check(cursosResponse, {
    [`(${nomeOrigem}) Listar cursos Status 200`]: (r) => r.status === 200,
  });

  if (cursosResponse.status !== 200) {
    logErro(`Listar cursos - ${nomeOrigem}`, cursosResponse);
    return { sucesso: false, alunoCriado: null };
  }

  const cursos = parseResponseJson(cursosResponse) || [];
  if (!Array.isArray(cursos) || cursos.length === 0) {
    check(cursosResponse, {
      [`(${nomeOrigem}) Possui cursos para teste`]: () => false,
    });
    return { sucesso: false, alunoCriado: null };
  }

  const curso = cursos[0];
  const turmasResponse = listarTurmasPorCursoRequest(urlOrigem, curso.id, nomeOrigem);
  check(turmasResponse, {
    [`(${nomeOrigem}) Listar turmas Status 200`]: (r) => r.status === 200,
  });

  if (turmasResponse.status !== 200) {
    logErro(`Listar turmas - ${nomeOrigem}`, turmasResponse);
    return { sucesso: false, alunoCriado: null };
  }

  const turmas = parseResponseJson(turmasResponse) || [];
  if (!Array.isArray(turmas) || turmas.length === 0) {
    check(turmasResponse, {
      [`(${nomeOrigem}) Possui turmas para teste`]: () => false,
    });
    return { sucesso: false, alunoCriado: null };
  }

  const turma = turmas[0];
  const alunoPayload = {
    pessoaId,
    turmaId: turma.id,
    dataMatricula: new Date().toISOString().split('T')[0],
    status: 'ATIVO',
  };

  const alunoResponse = criarAlunoRequest(urlOrigem, alunoPayload, nomeOrigem);
  check(alunoResponse, {
    [`(${nomeOrigem}) Criar aluno Status 201`]: (r) => r.status === 201,
  });

  if (alunoResponse.status !== 201) {
    logErro(`Criar aluno - ${nomeOrigem}`, alunoResponse);
    return { sucesso: false, alunoCriado: null };
  }

  const alunoCriado = parseResponseJson(alunoResponse);
  const resultadoReplicacao = aguardarReplicacao({
    requestFn: () => listarVinculosPorPessoaRequest(urlDestino, pessoaId, nomeDestino),
    validateFn: (vinculos) => Array.isArray(vinculos) && vinculos.length > 0,
  });

  check(resultadoReplicacao.response || {}, {
    [`(${nomeOrigem} -> ${nomeDestino}) Vínculo replicado`]: () => resultadoReplicacao.sucesso,
  });

  if (resultadoReplicacao.sucesso) {
    replicacaoLatencia.add(resultadoReplicacao.latenciaMs, { origem: nomeOrigem, destino: nomeDestino });
  }

  return { sucesso: resultadoReplicacao.sucesso, alunoCriado };
}

export const testeConclusaoInvalida = (alunoCriado, servicoOrigem) => {
  if (!alunoCriado || !alunoCriado.id) {
    return false;
  }

  const { url: urlOrigem, nome: nomeOrigem } = SERVICOS[servicoOrigem];
  const payloadInvalido = {
    pessoaId: alunoCriado.pessoaId,
    turmaId: alunoCriado.turmaId,
    dataMatricula: alunoCriado.dataMatricula,
    dataConclusao: null,
    status: 'CONCLUIDO',
  };

  const resposta = atualizarAlunoRequest(urlOrigem, alunoCriado.id, payloadInvalido, nomeOrigem);
  check(resposta, {
    [`(${nomeOrigem}) CONCLUIDO sem dataConclusao falha`]: (r) => r.status >= 400 && r.status < 500,
  });

  return resposta.status >= 400 && resposta.status < 500;
}

export const testeConclusaoValidaGeraRequerimento = (alunoCriado, servicoOrigem, servicoDestino) => {
  if (!alunoCriado || !alunoCriado.id) {
    return false;
  }

  const { url: urlOrigem, nome: nomeOrigem } = SERVICOS[servicoOrigem];
  const { url: urlDestino, nome: nomeDestino } = SERVICOS[servicoDestino];
  const payloadValido = {
    pessoaId: alunoCriado.pessoaId,
    turmaId: alunoCriado.turmaId,
    dataMatricula: alunoCriado.dataMatricula,
    dataConclusao: new Date().toISOString().split('T')[0],
    status: 'CONCLUIDO',
  };

  const requerimentosAntesResponse = listarRequerimentosPorPessoaRequest(urlDestino, alunoCriado.pessoaId, nomeDestino);
  const requerimentosAntes = requerimentosAntesResponse.status === 200
    ? (parseResponseJson(requerimentosAntesResponse) || [])
    : [];
  const quantidadeAntesPessoa = Array.isArray(requerimentosAntes) ? requerimentosAntes.length : 0;

  const atualizacao = atualizarAlunoRequest(urlOrigem, alunoCriado.id, payloadValido, nomeOrigem);
  check(atualizacao, {
    [`(${nomeOrigem}) Atualizar aluno para CONCLUIDO Status 200`]: (r) => r.status === 200,
  });

  if (atualizacao.status !== 200) {
    logErro(`Atualizar aluno para CONCLUIDO - ${nomeOrigem}`, atualizacao);
    return false;
  }

  const resultadoReplicacao = aguardarReplicacao({
    requestFn: () => listarRequerimentosPorPessoaRequest(urlDestino, alunoCriado.pessoaId, nomeDestino),
    validateFn: (requerimentos) =>
      Array.isArray(requerimentos) && requerimentos.length > quantidadeAntesPessoa,
  });

  check(resultadoReplicacao.response || {}, {
    [`(${nomeOrigem} -> ${nomeDestino}) Requerimento gerado após conclusão`]: () => resultadoReplicacao.sucesso,
  });

  if (resultadoReplicacao.sucesso) {
    replicacaoLatencia.add(resultadoReplicacao.latenciaMs, { origem: nomeOrigem, destino: nomeDestino });
  }

  const requerimento = resultadoReplicacao.sucesso
    ? ((resultadoReplicacao.data || []).slice(quantidadeAntesPessoa)[0] || null)
    : null;
  return { sucesso: resultadoReplicacao.sucesso, requerimento };
}

// Verifica que um vínculo de uma pessoa já existente replicou para o serviço destino.
// Usar quando o vínculo foi criado em outro passo do teste e não precisa criar um novo aluno.
export const testeVerificarVinculoReplicado = (pessoaId, servicoDestino) => {
  const { url: urlDestino, nome: nomeDestino } = SERVICOS[servicoDestino];

  const resultadoReplicacao = aguardarReplicacao({
    requestFn: () => listarVinculosPorPessoaRequest(urlDestino, pessoaId, nomeDestino),
    validateFn: (vinculos) => Array.isArray(vinculos) && vinculos.length > 0,
  });

  check(resultadoReplicacao.response || {}, {
    [`(-> ${nomeDestino}) Vínculo replicado`]: () => resultadoReplicacao.sucesso,
  });

  if (resultadoReplicacao.sucesso) {
    replicacaoLatencia.add(resultadoReplicacao.latenciaMs, { destino: nomeDestino });
  }

  return resultadoReplicacao.sucesso;
}

// Equivalente a testeReplicacaoVinculoAcademico para Pós-Graduação.
// Usa GET /programas (não /cursos) e cria aluno com programaId (não turmaId).
export const testeReplicacaoVinculoPosGraduacao = (pessoaId, servicoOrigem, servicoDestino) => {
  const { url: urlOrigem, nome: nomeOrigem } = SERVICOS[servicoOrigem];
  const { url: urlDestino, nome: nomeDestino } = SERVICOS[servicoDestino];

  const programasResponse = listarProgramasRequest(urlOrigem, nomeOrigem);
  check(programasResponse, {
    [`(${nomeOrigem}) Listar programas Status 200`]: (r) => r.status === 200,
  });

  if (programasResponse.status !== 200) {
    logErro(`Listar programas - ${nomeOrigem}`, programasResponse);
    return { sucesso: false, alunoCriado: null };
  }

  const programas = parseResponseJson(programasResponse) || [];
  if (!Array.isArray(programas) || programas.length === 0) {
    check(programasResponse, {
      [`(${nomeOrigem}) Possui programas para teste`]: () => false,
    });
    return { sucesso: false, alunoCriado: null };
  }

  const programa = programas[0];
  const alunoPayload = {
    pessoaId,
    programaId: programa.id,
    dataMatricula: new Date().toISOString().split('T')[0],
    status: 'ATIVO',
  };

  const alunoResponse = criarAlunoRequest(urlOrigem, alunoPayload, nomeOrigem);
  check(alunoResponse, {
    [`(${nomeOrigem}) Criar aluno Status 201`]: (r) => r.status === 201,
  });

  if (alunoResponse.status !== 201) {
    logErro(`Criar aluno - ${nomeOrigem}`, alunoResponse);
    return { sucesso: false, alunoCriado: null };
  }

  const alunoCriado = parseResponseJson(alunoResponse);
  const resultadoReplicacao = aguardarReplicacao({
    requestFn: () => listarVinculosPorPessoaRequest(urlDestino, pessoaId, nomeDestino),
    validateFn: (vinculos) => Array.isArray(vinculos) && vinculos.length > 0,
  });

  check(resultadoReplicacao.response || {}, {
    [`(${nomeOrigem} -> ${nomeDestino}) Vínculo replicado`]: () => resultadoReplicacao.sucesso,
  });

  if (resultadoReplicacao.sucesso) {
    replicacaoLatencia.add(resultadoReplicacao.latenciaMs, { origem: nomeOrigem, destino: nomeDestino });
  }

  return { sucesso: resultadoReplicacao.sucesso, alunoCriado };
}
// Equivalente a testeConclusaoInvalida para Pós-Graduação.
// Verifica que status CONCLUIDO sem dataConclusao é rejeitado (4xx) pelo serviço.
export const testeConclusaoInvalidaPosGraduacao = (alunoCriado, servicoOrigem) => {
  if (!alunoCriado || !alunoCriado.id) {
    return false;
  }

  const { url: urlOrigem, nome: nomeOrigem } = SERVICOS[servicoOrigem];
  const payloadInvalido = {
    pessoaId: alunoCriado.pessoaId,
    programaId: alunoCriado.programaId,
    orientadorId: alunoCriado.orientadorId,
    dataMatricula: alunoCriado.dataMatricula,
    dataConclusao: null,
    status: 'CONCLUIDO',
  };

  const resposta = atualizarAlunoRequest(urlOrigem, alunoCriado.id, payloadInvalido, nomeOrigem);
  check(resposta, {
    [`(${nomeOrigem}) CONCLUIDO sem dataConclusao falha`]: (r) => r.status >= 400 && r.status < 500,
  });

  return resposta.status >= 400 && resposta.status < 500;
}

// Equivalente a testeConclusaoValidaGeraRequerimento para Pós-Graduação.
// Atualiza aluno para CONCLUIDO e verifica que um RequerimentoDiploma é gerado em servicoDestino.
export const testeConclusaoValidaGeraRequerimentoPosGraduacao = (alunoCriado, servicoOrigem, servicoDestino) => {
  if (!alunoCriado || !alunoCriado.id) {
    return false;
  }

  const { url: urlOrigem, nome: nomeOrigem } = SERVICOS[servicoOrigem];
  const { url: urlDestino, nome: nomeDestino } = SERVICOS[servicoDestino];

  const requerimentosAntesResponse = listarRequerimentosPorPessoaRequest(urlDestino, alunoCriado.pessoaId, nomeDestino);
  const requerimentosAntes = requerimentosAntesResponse.status === 200
    ? (parseResponseJson(requerimentosAntesResponse) || [])
    : [];
  const quantidadeAntesPessoa = Array.isArray(requerimentosAntes) ? requerimentosAntes.length : 0;

  const payloadValido = {
    pessoaId: alunoCriado.pessoaId,
    programaId: alunoCriado.programaId,
    orientadorId: alunoCriado.orientadorId,
    dataMatricula: alunoCriado.dataMatricula,
    dataConclusao: new Date().toISOString().split('T')[0],
    status: 'CONCLUIDO',
  };

  const atualizacao = atualizarAlunoRequest(urlOrigem, alunoCriado.id, payloadValido, nomeOrigem);
  check(atualizacao, {
    [`(${nomeOrigem}) Atualizar aluno para CONCLUIDO Status 200`]: (r) => r.status === 200,
  });

  if (atualizacao.status !== 200) {
    logErro(`Atualizar aluno para CONCLUIDO - ${nomeOrigem}`, atualizacao);
    return false;
  }

  const resultadoReplicacao = aguardarReplicacao({
    requestFn: () => listarRequerimentosPorPessoaRequest(urlDestino, alunoCriado.pessoaId, nomeDestino),
    validateFn: (requerimentos) =>
      Array.isArray(requerimentos) && requerimentos.length > quantidadeAntesPessoa,
  });

  check(resultadoReplicacao.response || {}, {
    [`(${nomeOrigem} -> ${nomeDestino}) Requerimento gerado após conclusão`]: () => resultadoReplicacao.sucesso,
  });

  if (resultadoReplicacao.sucesso) {
    replicacaoLatencia.add(resultadoReplicacao.latenciaMs, { origem: nomeOrigem, destino: nomeDestino });
  }

  const requerimento = resultadoReplicacao.sucesso
    ? ((resultadoReplicacao.data || []).slice(quantidadeAntesPessoa)[0] || null)
    : null;
  return { sucesso: resultadoReplicacao.sucesso, requerimento };
}

// Cria Diploma + DocumentoDiploma a partir do requerimento e verifica que Assinatura
// auto-cria um DocumentoAssinavel (via trigger em C1 ou evento em A2/A3).
export const testeDocumentoAssinavel = (requerimento, servicoOrigem, servicoDestino) => {
  if (!requerimento?.id) {
    return { sucesso: false, documentoDiploma: null, documentoAssinavelId: null };
  }

  const { url: urlOrigem, nome: nomeOrigem } = SERVICOS[servicoOrigem];
  const { url: urlDestino, nome: nomeDestino } = SERVICOS[servicoDestino];

  const diplomaPayload = {
    requerimentoId: requerimento.id,
    numeroRegistro: `REG-${Date.now()}`,
    dataEmissao: new Date().toISOString().split('T')[0],
  };
  const diplomaResponse = criarDiplomaRequest(urlOrigem, diplomaPayload, nomeOrigem);
  check(diplomaResponse, {
    [`(${nomeOrigem}) Criar Diploma Status 201`]: (r) => r.status === 201,
  });

  if (diplomaResponse.status !== 201) {
    logErro(`Criar Diploma - ${nomeOrigem}`, diplomaResponse);
    return { sucesso: false, documentoDiploma: null, documentoAssinavelId: null };
  }

  const diploma = parseResponseJson(diplomaResponse);

  const documentoPayload = {
    versao: 1,
    dataGeracao: new Date().toISOString().split('T')[0],
    urlArquivo: `https://arquivos.tcc/${diploma.id}/v1.pdf`,
    hashDocumento: `hash-${diploma.id}-${Date.now()}`,
  };
  const documentoDiplomaResponse = criarDocumentoDiplomaRequest(urlOrigem, diploma.id, documentoPayload, nomeOrigem);
  check(documentoDiplomaResponse, {
    [`(${nomeOrigem}) Criar DocumentoDiploma Status 201`]: (r) => r.status === 201,
  });

  if (documentoDiplomaResponse.status !== 201) {
    logErro(`Criar DocumentoDiploma - ${nomeOrigem}`, documentoDiplomaResponse);
    return { sucesso: false, documentoDiploma: null, documentoAssinavelId: null };
  }

  const documentoDiploma = parseResponseJson(documentoDiplomaResponse);

  const resultadoReplicacao = aguardarReplicacao({
    requestFn: () => listarDocumentosAssinaveis(urlDestino, nomeDestino),
    validateFn: (docs) => Array.isArray(docs) && docs.some((d) => d.documentoDiplomaId === documentoDiploma.id),
  });

  check(resultadoReplicacao.response || {}, {
    [`(${nomeOrigem} -> ${nomeDestino}) DocumentoAssinavel gerado`]: () => resultadoReplicacao.sucesso,
  });

  if (!resultadoReplicacao.sucesso) {
    return { sucesso: false, documentoDiploma, documentoAssinavelId: null };
  }

  replicacaoLatencia.add(resultadoReplicacao.latenciaMs, { origem: nomeOrigem, destino: nomeDestino });

  const documentoAssinavel = (resultadoReplicacao.data || []).find((d) => d.documentoDiplomaId === documentoDiploma.id);
  return { sucesso: true, documentoDiploma, documentoAssinavelId: documentoAssinavel.id };
}

// Verifica que uma SolicitacaoAssinatura com status PENDENTE foi criada automaticamente
// para o documentoAssinavelId informado (via trigger em C1 ou evento em A2/A3).
export const testeSolicitacaoAssinatura = (documentoAssinavelId, servicoDestino) => {
  if (!documentoAssinavelId) {
    return { sucesso: false, solicitacao: null };
  }

  const { url: urlDestino, nome: nomeDestino } = SERVICOS[servicoDestino];

  const resultadoReplicacao = aguardarReplicacao({
    requestFn: () => listarSolicitacoesRequest(urlDestino, documentoAssinavelId, nomeDestino),
    validateFn: (solicitacoes) => Array.isArray(solicitacoes) && solicitacoes.some((s) => s.status === 'PENDENTE'),
  });

  check(resultadoReplicacao.response || {}, {
    [`(${nomeDestino}) SolicitacaoAssinatura PENDENTE criada`]: () => resultadoReplicacao.sucesso,
  });

  if (!resultadoReplicacao.sucesso) {
    return { sucesso: false, solicitacao: null };
  }

  const solicitacao = (resultadoReplicacao.data || []).find((s) => s.status === 'PENDENTE');
  return { sucesso: true, solicitacao };
}

// Verifica que uma segunda tentativa de criar SolicitacaoAssinatura para o mesmo
// documentoAssinavelId retorna 409 CONFLICT (regra de não-duplicidade).
export const testeNaoDuplicidadeAssinatura = (documentoAssinavelId, servicoDestino) => {
  if (!documentoAssinavelId) {
    return { sucesso: false };
  }

  const { url: urlDestino, nome: nomeDestino } = SERVICOS[servicoDestino];

  const payload = {
    status: 'PENDENTE',
    dataSolicitacao: new Date().toISOString(),
  };

  const resposta = criarSolicitacaoAssinaturaRequest(urlDestino, documentoAssinavelId, payload, nomeDestino);
  check(resposta, {
    [`(${nomeDestino}) Segunda solicitacao retorna 409 CONFLICT`]: (r) => r.status === 409,
  });

  return { sucesso: resposta.status === 409 };
}

// Conclui a assinatura (PUT solicitacao → CONCLUIDA) e verifica que o StatusEmissao
// em Diplomas é atualizado para ASSINADO (via trigger em C1 ou evento em A2/A3).
export const testeRetornoAssinatura = (solicitacao, documentoAssinavelId, requerimento, servicoAssinatura, servicoDiplomas) => {
  if (!solicitacao?.id || !documentoAssinavelId || !requerimento?.statusEmissaoId) {
    return { sucesso: false };
  }

  const { url: urlAssinatura, nome: nomeAssinatura } = SERVICOS[servicoAssinatura];
  const { url: urlDiplomas, nome: nomeDiplomas } = SERVICOS[servicoDiplomas];

  const payload = {
    status: 'CONCLUIDA',
    dataSolicitacao: solicitacao.dataSolicitacao,
    dataConclusao: new Date().toISOString(),
  };

  const atualizacao = atualizarSolicitacaoAssinaturaRequest(
    urlAssinatura, documentoAssinavelId, solicitacao.id, payload, nomeAssinatura
  );
  check(atualizacao, {
    [`(${nomeAssinatura}) Atualizar SolicitacaoAssinatura para CONCLUIDA Status 200`]: (r) => r.status === 200,
  });

  if (atualizacao.status !== 200) {
    logErro(`Atualizar SolicitacaoAssinatura - ${nomeAssinatura}`, atualizacao);
    return { sucesso: false };
  }

  const resultadoReplicacao = aguardarReplicacao({
    requestFn: () => obterStatusEmissaoRequest(urlDiplomas, requerimento.statusEmissaoId, nomeDiplomas),
    validateFn: (statusEmissao) => statusEmissao?.status === 'ASSINADO',
  });

  check(resultadoReplicacao.response || {}, {
    [`(${nomeAssinatura} -> ${nomeDiplomas}) StatusEmissao atualizado para ASSINADO`]: () => resultadoReplicacao.sucesso,
  });

  if (resultadoReplicacao.sucesso) {
    replicacaoLatencia.add(resultadoReplicacao.latenciaMs, { origem: nomeAssinatura, destino: nomeDiplomas });
  }

  return { sucesso: resultadoReplicacao.sucesso };
}
