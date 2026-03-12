import { createPessoaPayload } from "./payload-factory.js";
import { criarPessoaRequest, obterPessoaRequest } from "./request-helpers.js";


import { SERVICOS } from "./constantes.js";
import { pessoaEquals } from "./equals-helpers.js";
import { check } from "k6";


export const testeReplicacaoPessoa = (servicoOrigem, servicosDestino) => {
  const pessoaPayload = createPessoaPayload();
  const { url, nome } = SERVICOS[servicoOrigem];
  // 1- Cria uma pessoa (Verifica resposta) no serviço de origem
  const pessoaNova = criarPessoaRequest(url, pessoaPayload, nome);
  console.log(`Resposta da criação da pessoa: ${pessoaNova.status} - ${pessoaNova.body}`);
  check(pessoaNova, {
    [`(Criar Pessoa - ${nome}) Status 201`]: (r) => r.status === 201,
    [`(Criar Pessoa - ${nome}) Body Correto`]: (r) => pessoaEquals(JSON.parse(r.body), pessoaPayload),
  });

  // 2- Verifica que existe em: Graduação, Pós-Graduação, Diplomas e Certificados
  for (let servicoDestino of servicosDestino) {
    const { url: urlDestino, nome: nomeDestino } = SERVICOS[servicoDestino];
    const pessoaRecuperada = obterPessoaRequest(urlDestino, pessoaNova.json().id, nomeDestino);
    console.log(`Resposta da criação da pessoa no serviço ${nomeDestino}: ${pessoaRecuperada.status} - ${pessoaRecuperada.body}`);
    check(pessoaRecuperada, {
      [`(Obter Pessoa - ${nomeDestino}) Status 200`]: (r) => r.status === 200,
      [`(Obter Pessoa - ${nomeDestino}) Body Correto`]: (r) => pessoaEquals(JSON.parse(r.body), pessoaPayload),
    });
  }
}