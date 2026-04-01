import { check, group } from 'k6';

import { GRADUACAO, ASSINATURA, DIPLOMAS, POS_GRADUACAO } from './utils/constantes.js';
import {
  replicacaoLatencia,
  testeAtualizacaoPessoa,
  testeConclusaoInvalida,
  testeConclusaoValidaGeraRequerimento,
  testeReplicacaoPessoa,
  testeReplicacaoVinculoAcademico,
} from './utils/replication-helpers.js';

const REPLICACAO_P95_MS = Number(__ENV.REPLICACAO_P95_MS || 5000);
const REPLICACAO_P99_MS = Number(__ENV.REPLICACAO_P99_MS || 15000);

const pendente = (descricao) => {
  check({}, { [`[PENDENTE] ${descricao}`]: () => false });
};

export const options = {
  thresholds: {
    'replicacao_latencia_ms': [
      { threshold: `p(95)<${REPLICACAO_P95_MS}`, abortOnFail: false },
      { threshold: `p(99)<${REPLICACAO_P99_MS}`, abortOnFail: false },
    ],
  },
  scenarios: {
    replicacao: {
      executor: 'per-vu-iterations',
      vus: 1,
      iterations: 1,
    }
  }
};

export default function () {
  let pessoaGraduacao = null;
  let vinculoCriado = null;

  // Replicações globais — Pessoa (fan-out de todos os produtores para todos os consumidores)
  group('Entidade Pessoa:', function() {

    group(`${GRADUACAO} -> [${POS_GRADUACAO}, ${DIPLOMAS}, ${ASSINATURA}]`, function(){
      const resultado = testeReplicacaoPessoa(GRADUACAO, [POS_GRADUACAO, DIPLOMAS, ASSINATURA]);
      pessoaGraduacao = resultado.pessoaCriada;
    });

    group(`${POS_GRADUACAO} -> [${GRADUACAO}, ${DIPLOMAS}, ${ASSINATURA}]`, function(){
      testeReplicacaoPessoa(POS_GRADUACAO, [GRADUACAO, DIPLOMAS, ASSINATURA]);
    });
  });

  // Replicações globais — Atualização de Pessoa
  group('Atualização de Pessoa:', function() {
    if (!pessoaGraduacao?.id) {
      console.warn('Atualização de pessoa pulada: pessoa de graduação não disponível.');
      return;
    }

    group(`${GRADUACAO} -> [${DIPLOMAS}, ${ASSINATURA}]`, function(){
      testeAtualizacaoPessoa(GRADUACAO, [DIPLOMAS, ASSINATURA], pessoaGraduacao);
    });

    group(`${GRADUACAO} -> ${POS_GRADUACAO}`, function(){
      pendente(`Atualização de Pessoa: ${GRADUACAO} -> ${POS_GRADUACAO}`);
    });

    group(`${POS_GRADUACAO} -> [${GRADUACAO}, ${DIPLOMAS}, ${ASSINATURA}]`, function(){
      pendente(`Atualização de Pessoa: ${POS_GRADUACAO} -> [${GRADUACAO}, ${DIPLOMAS}, ${ASSINATURA}]`);
    });
  });

  // Replicações globais — VínculoAcadêmico (fan-out de Grad/Pós para Diplomas e Assinatura)
  group('Vínculo Acadêmico e Conclusão:', function() {
    if (!pessoaGraduacao?.id) {
      console.warn('Teste de vínculo pulado: pessoa de graduação não disponível.');
      return;
    }

    group(`${GRADUACAO} -> ${DIPLOMAS} (vínculo)`, function() {
      const resultado = testeReplicacaoVinculoAcademico(pessoaGraduacao.id, GRADUACAO, DIPLOMAS);
      vinculoCriado = resultado.alunoCriado;
    });

    group(`${GRADUACAO} -> ${ASSINATURA} (vínculo)`, function() {
      pendente(`Vínculo Acadêmico: ${GRADUACAO} -> ${ASSINATURA}`);
    });

    group(`${POS_GRADUACAO} -> ${DIPLOMAS} (vínculo)`, function() {
      pendente(`Vínculo Acadêmico: ${POS_GRADUACAO} -> ${DIPLOMAS}`);
    });

    group(`${POS_GRADUACAO} -> ${ASSINATURA} (vínculo)`, function() {
      pendente(`Vínculo Acadêmico: ${POS_GRADUACAO} -> ${ASSINATURA}`);
    });

    group(`${GRADUACAO} (regra CONCLUIDO sem dataConclusao)`, function() {
      if (!vinculoCriado?.id) {
        console.warn('Validação de conclusão inválida pulada: vínculo não foi criado.');
        return;
      }

      testeConclusaoInvalida(vinculoCriado, GRADUACAO);
    });

    group(`${POS_GRADUACAO} (regra CONCLUIDO sem dataConclusao)`, function() {
      pendente(`Regra de negócio: ${POS_GRADUACAO} CONCLUIDO sem dataConclusao rejeitado`);
    });

    group(`${GRADUACAO} -> ${DIPLOMAS} (conclusão válida gera requerimento)`, function() {
      if (!vinculoCriado?.id) {
        console.warn('Validação de requerimento pulada: vínculo não foi criado.');
        return;
      }

      testeConclusaoValidaGeraRequerimento(vinculoCriado, GRADUACAO, DIPLOMAS);
    });

    group(`${POS_GRADUACAO} -> ${DIPLOMAS} (conclusão válida gera requerimento)`, function() {
      pendente(`Conclusão válida: ${POS_GRADUACAO} -> ${DIPLOMAS} gera RequerimentoDiploma`);
    });
  });

  // Fluxo de comunicação — Diplomas → Assinatura
  group('Diplomas -> Assinatura:', function() {
    group(`${DIPLOMAS} -> ${ASSINATURA} (DocumentoDiploma gera DocumentoAssinavel)`, function() {
      pendente(`${DIPLOMAS} -> ${ASSINATURA}: DocumentoDiploma emitido gera DocumentoAssinavel`);
    });

    group(`${DIPLOMAS} -> ${ASSINATURA} (SolicitacaoAssinatura criada como PENDENTE)`, function() {
      pendente(`${DIPLOMAS} -> ${ASSINATURA}: SolicitacaoAssinatura criada com Assinatura PENDENTE`);
    });

    group(`${ASSINATURA} (regra não-duplicidade de SolicitacaoAssinatura)`, function() {
      pendente(`Regra de negócio: segunda solicitação para mesmo documento não abre nova SolicitacaoAssinatura ativa`);
    });
  });

  // Fluxo de retorno — Assinatura → Diplomas
  group('Assinatura -> Diplomas:', function() {
    group(`${ASSINATURA} -> ${DIPLOMAS} (StatusEmissao = ASSINADO)`, function() {
      pendente(`${ASSINATURA} -> ${DIPLOMAS}: conclusão de assinatura atualiza StatusEmissao para ASSINADO`);
    });
  });

}