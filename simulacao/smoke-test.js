import { group } from 'k6';

import { GRADUACAO, ASSINATURA, DIPLOMAS, POS_GRADUACAO } from './utils/constantes.js';
import {
  replicacaoLatencia,
  testeAtualizacaoPessoa,
  testeConclusaoInvalida,
  testeConclusaoInvalidaPosGraduacao,
  testeConclusaoValidaGeraRequerimento,
  testeConclusaoValidaGeraRequerimentoPosGraduacao,
  testeDocumentoAssinavel,
  testeNaoDuplicidadeAssinatura,
  testeReplicacaoPessoa,
  testeReplicacaoVinculoAcademico,
  testeReplicacaoVinculoPosGraduacao,
  testeRetornoAssinatura,
  testeSolicitacaoAssinatura,
  testeVerificarVinculoReplicado,
} from './utils/replication-helpers.js';

const REPLICACAO_P95_MS = Number(__ENV.REPLICACAO_P95_MS || 5000);
const REPLICACAO_P99_MS = Number(__ENV.REPLICACAO_P99_MS || 15000);


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
      maxDuration: '25m',
    }
  }
};

export default function () {
  let pessoaGraduacao = null;
  let pessoaPosGraduacao = null;
  let vinculoCriado = null;
  let vinculoPosGraduacao = null;
  let requerimentoGraduacao = null;
  let documentoAssinavelId = null;
  let solicitacaoAssinatura = null;

  // Replicações globais — Pessoa (fan-out de todos os produtores para todos os consumidores)
  group('Entidade Pessoa:', function() {

    group(`${GRADUACAO} -> [${POS_GRADUACAO}, ${DIPLOMAS}, ${ASSINATURA}]`, function(){
      const resultado = testeReplicacaoPessoa(GRADUACAO, [POS_GRADUACAO, DIPLOMAS, ASSINATURA]);
      pessoaGraduacao = resultado.pessoaCriada;
    });

    group(`${POS_GRADUACAO} -> [${GRADUACAO}, ${DIPLOMAS}, ${ASSINATURA}]`, function(){
      const resultado = testeReplicacaoPessoa(POS_GRADUACAO, [GRADUACAO, DIPLOMAS, ASSINATURA]);
      pessoaPosGraduacao = resultado.pessoaCriada;
    });
  });

  // Replicações globais — Atualização de Pessoa
  group('Atualização de Pessoa:', function() {
    if (!pessoaGraduacao?.id) {
      console.warn('Atualização de pessoa pulada: pessoa de graduação não disponível.');
      return;
    }

    group(`${GRADUACAO} -> [${POS_GRADUACAO}, ${DIPLOMAS}, ${ASSINATURA}]`, function(){
      testeAtualizacaoPessoa(GRADUACAO, [POS_GRADUACAO, DIPLOMAS, ASSINATURA], pessoaGraduacao);
    });

    group(`${POS_GRADUACAO} -> [${GRADUACAO}, ${DIPLOMAS}, ${ASSINATURA}]`, function(){
      if (!pessoaPosGraduacao?.id) {
        console.warn('Atualização de pessoa pulada: pessoa de pós-graduação não disponível.');
        return;
      }

      testeAtualizacaoPessoa(POS_GRADUACAO, [GRADUACAO, DIPLOMAS, ASSINATURA], pessoaPosGraduacao);
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
      if (!vinculoCriado?.id) {
        console.warn('Verificação de vínculo em Assinatura pulada: vínculo de graduação não disponível.');
        return;
      }

      testeVerificarVinculoReplicado(pessoaGraduacao.id, ASSINATURA);
    });

    group(`${POS_GRADUACAO} -> ${DIPLOMAS} (vínculo)`, function() {
      if (!pessoaPosGraduacao?.id) {
        console.warn('Vínculo de pós-graduação pulado: pessoa de pós-graduação não disponível.');
        return;
      }

      const resultado = testeReplicacaoVinculoPosGraduacao(pessoaPosGraduacao.id, POS_GRADUACAO, DIPLOMAS);
      vinculoPosGraduacao = resultado.alunoCriado;
    });

    group(`${POS_GRADUACAO} -> ${ASSINATURA} (vínculo)`, function() {
      if (!pessoaPosGraduacao?.id || !vinculoPosGraduacao?.id) {
        console.warn('Verificação de vínculo em Assinatura pulada: vínculo de pós-graduação não disponível.');
        return;
      }

      testeVerificarVinculoReplicado(pessoaPosGraduacao.id, ASSINATURA);
    });

    group(`${GRADUACAO} (regra CONCLUIDO sem dataConclusao)`, function() {
      if (!vinculoCriado?.id) {
        console.warn('Validação de conclusão inválida pulada: vínculo não foi criado.');
        return;
      }

      testeConclusaoInvalida(vinculoCriado, GRADUACAO);
    });

    group(`${POS_GRADUACAO} (regra CONCLUIDO sem dataConclusao)`, function() {
      if (!vinculoPosGraduacao?.id) {
        console.warn('Validação de conclusão inválida pulada: vínculo de pós-graduação não foi criado.');
        return;
      }

      testeConclusaoInvalidaPosGraduacao(vinculoPosGraduacao, POS_GRADUACAO);
    });

    group(`${GRADUACAO} -> ${DIPLOMAS} (conclusão válida gera requerimento)`, function() {
      if (!vinculoCriado?.id) {
        console.warn('Validação de requerimento pulada: vínculo não foi criado.');
        return;
      }

      const resultado = testeConclusaoValidaGeraRequerimento(vinculoCriado, GRADUACAO, DIPLOMAS);
      requerimentoGraduacao = resultado.requerimento;
    });

    group(`${POS_GRADUACAO} -> ${DIPLOMAS} (conclusão válida gera requerimento)`, function() {
      if (!vinculoPosGraduacao?.id) {
        console.warn('Validação de requerimento pulada: vínculo de pós-graduação não foi criado.');
        return;
      }

      testeConclusaoValidaGeraRequerimentoPosGraduacao(vinculoPosGraduacao, POS_GRADUACAO, DIPLOMAS);
    });
  });

  // Fluxo de comunicação — Diplomas → Assinatura
  group('Diplomas -> Assinatura:', function() {
    group(`${DIPLOMAS} -> ${ASSINATURA} (DocumentoDiploma gera DocumentoAssinavel)`, function() {
      if (!requerimentoGraduacao?.id) {
        console.warn('Teste de DocumentoAssinavel pulado: requerimento de graduação não disponível.');
        return;
      }

      const resultado = testeDocumentoAssinavel(requerimentoGraduacao, DIPLOMAS, ASSINATURA);
      documentoAssinavelId = resultado.documentoAssinavelId;
    });

    group(`${DIPLOMAS} -> ${ASSINATURA} (SolicitacaoAssinatura criada como PENDENTE)`, function() {
      if (!documentoAssinavelId) {
        console.warn('Teste de SolicitacaoAssinatura pulado: documentoAssinavelId não disponível.');
        return;
      }

      const resultado = testeSolicitacaoAssinatura(documentoAssinavelId, ASSINATURA);
      solicitacaoAssinatura = resultado.solicitacao;
    });

    group(`${ASSINATURA} (regra não-duplicidade de SolicitacaoAssinatura)`, function() {
      if (!documentoAssinavelId) {
        console.warn('Teste de não-duplicidade pulado: documentoAssinavelId não disponível.');
        return;
      }

      testeNaoDuplicidadeAssinatura(documentoAssinavelId, ASSINATURA);
    });
  });

  // Fluxo de retorno — Assinatura → Diplomas
  group('Assinatura -> Diplomas:', function() {
    group(`${ASSINATURA} -> ${DIPLOMAS} (StatusEmissao = ASSINADO)`, function() {
      if (!solicitacaoAssinatura?.id || !documentoAssinavelId || !requerimentoGraduacao?.statusEmissaoId) {
        console.warn('Teste de retorno de assinatura pulado: solicitação ou requerimento não disponíveis.');
        return;
      }

      testeRetornoAssinatura(solicitacaoAssinatura, documentoAssinavelId, requerimentoGraduacao, ASSINATURA, DIPLOMAS);
    });
  });

}