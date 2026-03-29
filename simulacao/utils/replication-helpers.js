import { createPessoaPayload } from "./payload-factory.js";
import {
  atualizarAlunoRequest,
  atualizarPessoaRequest,
  criarAlunoRequest,
  criarPessoaRequest,
  listarCursosRequest,
  listarRequerimentosRequest,
  listarTurmasPorCursoRequest,
  listarVinculosRequest,
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

export const testeReplicacaoPessoa = (servicoOrigem, servicosDestino) => {
  const pessoaPayload = createPessoaPayload();
  const { url, nome } = SERVICOS[servicoOrigem];
  // 1- Cria uma pessoa (Verifica resposta) no serviço de origem
  const pessoaNova = criarPessoaRequest(url, pessoaPayload, nome);
  console.log(`Resposta da criação da pessoa: ${pessoaNova.status} - ${pessoaNova.body}`);
  check(pessoaNova, {
    [`(Criar Pessoa - ${nome}) Status 201`]: (r) => r.status === 201,
  });

  if (pessoaNova.status !== 201) {
    return { sucessoGlobal: false, pessoaCriada: null };
  }

  const pessoaCriada = pessoaNova.json();
  let sucessoGlobal = true;

  // 2- Verifica que existe em: Graduação, Pós-Graduação, Diplomas e Certificados
  for (let servicoDestino of servicosDestino) {
    const { url: urlDestino, nome: nomeDestino } = SERVICOS[servicoDestino];
    const resultadoReplicacao = aguardarReplicacao({
      requestFn: () => obterPessoaRequest(urlDestino, pessoaCriada.id, nomeDestino),
      validateFn: (pessoaDestino) => {
        return !!pessoaDestino
          && pessoaDestino.id === pessoaCriada.id
          && pessoaDestino.nome === pessoaCriada.nome
          && pessoaDestino.dataNascimento === pessoaCriada.dataNascimento;
      },
    });

    check(resultadoReplicacao.response || {}, {
      [`(${nome} -> ${nomeDestino}) Replicação concluída`]: () => resultadoReplicacao.sucesso
    });

    if (!resultadoReplicacao.sucesso) {
      sucessoGlobal = false;
      continue;
    }

    const pessoaDestino = resultadoReplicacao.data;

    // 3- Medir latência de replicação (replicadoEm - criadoEm)
    if (pessoaDestino.replicadoEm && pessoaDestino.criadoEm) {
      const latenciaMs = new Date(pessoaDestino.replicadoEm) - new Date(pessoaDestino.criadoEm);
      replicacaoLatencia.add(latenciaMs, { origem: nome, destino: nomeDestino });
      console.log(`Latência de replicação ${nome} (${pessoaDestino.criadoEm}) -> ${nomeDestino} (${pessoaDestino.replicadoEm}): ${latenciaMs}ms`);
    } else {
      replicacaoLatencia.add(resultadoReplicacao.latenciaMs, { origem: nome, destino: nomeDestino });
      console.log(`Latência de replicação ${nome} -> ${nomeDestino}: ${resultadoReplicacao.latenciaMs}ms`);
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
    return { sucessoGlobal: false };
  }

  let sucessoGlobal = true;

  for (let servicoDestino of servicosDestino) {
    const { url: urlDestino, nome: nomeDestino } = SERVICOS[servicoDestino];

    const resultadoReplicacao = aguardarReplicacao({
      requestFn: () => obterPessoaRequest(urlDestino, pessoaCriada.id, nomeDestino),
      validateFn: (pessoaDestino) => {
        return !!pessoaDestino && pessoaDestino.nomeSocial === nomeSocialAtualizado;
      }
    });

    check(resultadoReplicacao.response || {}, {
      [`(${nomeOrigem} -> ${nomeDestino}) Atualização replicada`]: () => resultadoReplicacao.sucesso
    });

    if (!resultadoReplicacao.sucesso) {
      sucessoGlobal = false;
      continue;
    }

    replicacaoLatencia.add(resultadoReplicacao.latenciaMs, { origem: nomeOrigem, destino: nomeDestino });
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
    return { sucesso: false, alunoCriado: null };
  }

  const alunoCriado = parseResponseJson(alunoResponse);
  const resultadoReplicacao = aguardarReplicacao({
    requestFn: () => listarVinculosRequest(urlDestino, nomeDestino),
    validateFn: (vinculos) => {
      if (!Array.isArray(vinculos)) {
        return false;
      }

      return vinculos.some((v) => {
        const pessoaVinculoId = v.pessoaId ?? v.pessoa?.id;
        return pessoaVinculoId === pessoaId;
      });
    }
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

  const requerimentosAntesResponse = listarRequerimentosRequest(urlDestino, nomeDestino);
  const requerimentosAntes = requerimentosAntesResponse.status === 200
    ? (parseResponseJson(requerimentosAntesResponse) || [])
    : [];
  const quantidadeAntesPessoa = Array.isArray(requerimentosAntes)
    ? requerimentosAntes.filter((r) => r.pessoaId === alunoCriado.pessoaId).length
    : 0;

  const atualizacao = atualizarAlunoRequest(urlOrigem, alunoCriado.id, payloadValido, nomeOrigem);
  check(atualizacao, {
    [`(${nomeOrigem}) Atualizar aluno para CONCLUIDO Status 200`]: (r) => r.status === 200,
  });

  if (atualizacao.status !== 200) {
    return false;
  }

  const resultadoReplicacao = aguardarReplicacao({
    requestFn: () => listarRequerimentosRequest(urlDestino, nomeDestino),
    validateFn: (requerimentos) => {
      if (!Array.isArray(requerimentos)) {
        return false;
      }

      const quantidadeAtualPessoa = requerimentos
        .filter((r) => r.pessoaId === alunoCriado.pessoaId)
        .length;

      return quantidadeAtualPessoa > quantidadeAntesPessoa;
    }
  });

  check(resultadoReplicacao.response || {}, {
    [`(${nomeOrigem} -> ${nomeDestino}) Requerimento gerado após conclusão`]: () => resultadoReplicacao.sucesso,
  });

  if (resultadoReplicacao.sucesso) {
    replicacaoLatencia.add(resultadoReplicacao.latenciaMs, { origem: nomeOrigem, destino: nomeDestino });
  }

  return resultadoReplicacao.sucesso;
}