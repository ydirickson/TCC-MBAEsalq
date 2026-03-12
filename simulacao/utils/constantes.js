export const GRADUACAO_URL = 'http://localhost:8081'
export const POS_GRADUACAO_URL = 'http://localhost:8082'
export const DIPLOMAS_URL = 'http://localhost:8083'
export const CERTIFICADOS_URL = 'http://localhost:8084'

export const GRADUACAO = 'GRADUACAO';
export const POS_GRADUACAO = 'POS_GRADUACAO';
export const DIPLOMAS = 'DIPLOMAS';
export const CERTIFICADOS = 'CERTIFICADOS';

export const SERVICOS = {
  [GRADUACAO]: {
    url: GRADUACAO_URL,
    nome: 'Graduação'
  },
  [POS_GRADUACAO]: {
    url: POS_GRADUACAO_URL,
    nome: 'Pós-Graduação'
  },
  [DIPLOMAS]: {
    url: DIPLOMAS_URL,
    nome: 'Diplomas'
  },
  [CERTIFICADOS]: {
    url: CERTIFICADOS_URL,
    nome: 'Certificados'
  }
}