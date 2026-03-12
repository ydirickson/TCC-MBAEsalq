//import http from 'k6/http';
import { check, group } from 'k6';

//import { createPessoaPayload } from './utils/payload-factory.js';

import { GRADUACAO, CERTIFICADOS, DIPLOMAS, POS_GRADUACAO } from './utils/constantes.js';
//import { criarPessoaRequest } from './utils/request-helpers.js';
import { testeReplicacaoPessoa } from './utils/replication-helpers.js';

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