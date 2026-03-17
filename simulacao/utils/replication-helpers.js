import { createPessoaPayload } from "./payload-factory.js";
import { criarPessoaRequest, obterPessoaRequest } from "./request-helpers.js";


import { SERVICOS } from "./constantes.js";
import { check } from "k6";
import { Trend } from "k6/metrics";

export const replicacaoLatencia = new Trend('replicacao_latencia_ms', true);

export const testeReplicacaoPessoa = (servicoOrigem, servicosDestino) => {
  const pessoaPayload = createPessoaPayload();
  const { url, nome } = SERVICOS[servicoOrigem];
  // 1- Cria uma pessoa (Verifica resposta) no serviço de origem
  const pessoaNova = criarPessoaRequest(url, pessoaPayload, nome);
  console.log(`Resposta da criação da pessoa: ${pessoaNova.status} - ${pessoaNova.body}`);
  check(pessoaNova, {
    [`(Criar Pessoa - ${nome}) Status 201`]: (r) => r.status === 201,
  });

  const pessoaCriada = pessoaNova.json();
  const criadoEmOrigem = pessoaCriada.criadoEm;

  // 2- Verifica que existe em: Graduação, Pós-Graduação, Diplomas e Certificados
  for (let servicoDestino of servicosDestino) {
    const { url: urlDestino, nome: nomeDestino } = SERVICOS[servicoDestino];
    const pessoaRecuperada = obterPessoaRequest(urlDestino, pessoaCriada.id, nomeDestino);
    check(pessoaRecuperada, {
      [`(${nome} -> ${nomeDestino}) Status 200`]: (r) => r.status === 200
    });

    const pessoaDestino = pessoaRecuperada.json();

    // 3- Medir latência de replicação (replicadoEm - criadoEm)
    if (pessoaDestino.replicadoEm && pessoaDestino.criadoEm) {
      const latenciaMs = new Date(pessoaDestino.replicadoEm) - new Date(pessoaDestino.criadoEm);
      replicacaoLatencia.add(latenciaMs, { origem: nome, destino: nomeDestino });
      console.log(`Latência de replicação ${nome} (${pessoaDestino.criadoEm}) -> ${nomeDestino} (${pessoaDestino.replicadoEm}): ${latenciaMs}ms`);
    }
  }
}