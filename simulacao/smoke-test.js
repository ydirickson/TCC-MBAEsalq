import http from 'k6/http';
import { check } from 'k6';

import { createPessoaPayload } from './utils/payload-factory.js';

import { GRADUACAO_URL, CERTIFICADOS_URL, DIPLOMAS_URL, POS_GRADUACAO_URL } from './utils/constantes.js';

export function setup() {
  return {
    graduacaoUrl: GRADUACAO_URL,
    posGraduacaoUrl: POS_GRADUACAO_URL,
    diplomasUrl: DIPLOMAS_URL,
    certificadosUrl: CERTIFICADOS_URL
  }
}

export default function (data) {  
  // 1- Cria uma pessoa (Verifica resposta) e verifica que existe em: Graduação, Pós-Graduação, Diplomas e Certificados
  const pessoaPayload = JSON.stringify(createPessoaPayload());
  const pessoaNova = http.post(
    `${data.graduacaoUrl}/pessoas`, 
    pessoaPayload, 
    {
      headers: { 'Content-Type': 'application/json' },
      tags: {
        acao: 'Criar Pessoa',
        servico: 'Graduação',
      }
    }
  );
  check(pessoaNova, {
    '(Criar Pessoa - Graduação) status is 201': (r) => r.status === 201,
    '(Criar Pessoa - Graduação) response body is correct': (r) => r.body.includes('Test User'),
  });

  // 1.1 Recuperando do Graduação
  let pessoaRecuperada = http.get(
    `${data.graduacaoUrl}/pessoas/${pessoaNova.json().id}`,
    {
      tags: {
        acao: 'Obter Pessoa',
        servico: 'Graduação',
      }
    }
  );
  check(pessoaRecuperada, {
    '(Obter Pessoa - Graduação) status is 200': (r) => r.status === 200,
    '(Obter Pessoa - Graduação) pessoa recuperada matches pessoa criada': (r) => {
      const pessoaCriada = pessoaNova.json();
      const pessoaRecuperada = r.json();
      return pessoaCriada.id === pessoaRecuperada.id &&
        pessoaCriada.nome === pessoaRecuperada.nome &&
        pessoaCriada.email === pessoaRecuperada.email &&
        pessoaCriada.cpf === pessoaRecuperada.cpf;
    },
  });

  // 1.2 Recuperando do Pós-Graduação
  pessoaRecuperada = http.get(
    `${data.posGraduacaoUrl}/pessoas/${pessoaNova.json().id}`,
    {
      tags: {
        acao: 'Obter Pessoa',
        servico: 'Pós-Graduação',
      }
    }
  );
  check(pessoaRecuperada, {
    '(Obter Pessoa - Pós-Graduação) status is 200': (r) => r.status === 200,
    '(Obter Pessoa - Pós-Graduação) pessoa recuperada matches pessoa criada': (r) => {
      const pessoaCriada = pessoaNova.json();
      const pessoaRecuperada = r.json();
      return pessoaCriada.id === pessoaRecuperada.id &&
        pessoaCriada.nome === pessoaRecuperada.nome &&
        pessoaCriada.email === pessoaRecuperada.email &&
        pessoaCriada.cpf === pessoaRecuperada.cpf;
    },
  });

  // 1.3 Recuperando do Diplomas
  pessoaRecuperada = http.get(
    `${data.diplomasUrl}/pessoas/${pessoaNova.json().id}`,
    {
      tags: {
        acao: 'Obter Pessoa',
        servico: 'Diplomas',
      }
    }
  );
  check(pessoaRecuperada, {
    '(Obter Pessoa - Diplomas) status is 200': (r) => r.status === 200,
    '(Obter Pessoa - Diplomas) pessoa recuperada matches pessoa criada': (r) => {
      const pessoaCriada = pessoaNova.json();
      const pessoaRecuperada = r.json();
      return pessoaCriada.id === pessoaRecuperada.id &&
        pessoaCriada.nome === pessoaRecuperada.nome &&
        pessoaCriada.email === pessoaRecuperada.email &&
        pessoaCriada.cpf === pessoaRecuperada.cpf;
    },
  });

  // 1.4 Recuperando do Certificados
  pessoaRecuperada = http.get(
    `${data.certificadosUrl}/pessoas/${pessoaNova.json().id}`,
    {
      tags: {
        acao: 'Obter Pessoa',
        servico: 'Certificados',
      }
    }
  );
  check(pessoaRecuperada, {
    '(Obter Pessoa - Certificados) status is 200': (r) => r.status === 200,
    '(Obter Pessoa - Certificados) pessoa recuperada matches pessoa criada': (r) => {
      const pessoaCriada = pessoaNova.json();
      const pessoaRecuperada = r.json();
      return pessoaCriada.id === pessoaRecuperada.id &&
        pessoaCriada.nome === pessoaRecuperada.nome &&
        pessoaCriada.email === pessoaRecuperada.email &&
        pessoaCriada.cpf === pessoaRecuperada.cpf;
    },
  });

}