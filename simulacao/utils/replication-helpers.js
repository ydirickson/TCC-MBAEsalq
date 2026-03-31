import { createPessoaPayload } from "./payload-factory.js";
import {
  atualizarAlunoRequest,
  atualizarPessoaRequest,
  criarAlunoRequest,
  criarPessoaRequest,
  listarCursosRequest,
  listarRequerimentosPorPessoaRequest,
  listarTurmasPorCursoRequest,
  listarVinculosPorPessoaRequest,
  obterPessoaRequest
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
  console.error(`[ERRO] ${descricao} — status: ${response?.status} body: ${response?.body}`);
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

  return resultadoReplicacao.sucesso;
}