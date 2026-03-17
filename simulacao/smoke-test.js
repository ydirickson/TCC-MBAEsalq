import { group } from 'k6';

import { GRADUACAO, CERTIFICADOS, DIPLOMAS, POS_GRADUACAO } from './utils/constantes.js';
import { testeReplicacaoPessoa, replicacaoLatencia } from './utils/replication-helpers.js';




export const options = {
  thresholds: {
    'replicacao_latencia_ms': [
      { threshold: 'p(95)<500', abortOnFail: false },
      { threshold: 'p(99)<1000', abortOnFail: false },
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
  // 1- Cria uma pessoa (Verifica resposta) e verifica que existe em: Graduação, Pós-Graduação, Diplomas e Certificados
  group('Entidade Pessoa:', function() {

    group (`${GRADUACAO} -> [${POS_GRADUACAO}, ${DIPLOMAS}, ${CERTIFICADOS}]`, function(){
      testeReplicacaoPessoa(GRADUACAO, [POS_GRADUACAO, DIPLOMAS, CERTIFICADOS]);
    })
    group (`${POS_GRADUACAO} -> [${GRADUACAO}, ${DIPLOMAS}, ${CERTIFICADOS}]`, function(){
      testeReplicacaoPessoa(POS_GRADUACAO, [GRADUACAO, DIPLOMAS, CERTIFICADOS]);
    })
  });

}