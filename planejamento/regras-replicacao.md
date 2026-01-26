# 5. Definição das intersecções e regras de replicação
[← Voltar ao índice](./README.md)

## 5.1. Pessoa (intersecção principal)
- Pessoa é usada por todos os serviços.
- **Esquema comum:** todos os serviços possuem a mesma entidade e tabela de Pessoa.
- **Fontes de produção:** Graduação e Pós criam e atualizam Pessoa; Diplomas e Assinatura consomem e mantêm cópias locais.
- Regra de criação: Pessoa pode nascer em qualquer um dos dois serviços principais (Graduação ou Pós) e ambos publicam `PessoaCriada`.
- Regra de atualização: Graduação e Pós podem atualizar Pessoa; ambos publicam `PessoaAtualizada`. Consumidores aplicam de forma idempotente (id + timestamp/versão) em seus read models.

## 5.2. Vínculo Acadêmico (VinculoAcademico)
- **Cardinalidade:** uma Pessoa pode ter múltiplos vínculos (1:N), por exemplo duas graduações, um mestrado e um doutorado, cada um com seu próprio `vinculoId`.
- **Histórico:** manter tabela de histórico/versões de `VinculoAcademico` para registrar mudanças de status/curso/orientador (auditoria).
- **Esquema comum:** todos os serviços possuem a mesma entidade e tabela de VínculoAcadêmico (com `cursoId`, `cursoCodigo`, `cursoNome`, `tipoCursoPrograma`).
- **Fontes de produção:** Graduação e Pós criam e atualizam `VinculoAcademico`.
- Eventos: `VinculoAcademicoCriado` (status inicial ativo) e `VinculoAcademicoAtualizado` (mudança de status, curso/programa, orientador/colegiado).
- Consumo: Diplomas e Assinatura mantêm read models; Graduação e Pós também consomem para reconciliação.
- Status “concluído” gera `ConclusaoPublicada` (coberto na seção seguinte).

## 5.3. Conclusão / elegibilidade para diploma
- Diplomas precisa saber quando um vínculo (grad/pós) está concluído.
- Graduação e Pós produzem o estado “concluído” (ou equivalente).
- Diplomas consome esse estado para permitir emissão.
- Regra de consistência: status concluído exige `dataConclusao` preenchida.

## 5.4. Pedido de Diploma (tabela replicada)
- Quando um vínculo é marcado como concluído, Graduação ou Pós cria um registro em uma tabela de `PedidoDiploma` (ou `RequerimentoDiploma`) local.
- Essa tabela é replicada para o serviço de Diplomas, que consome os pedidos e inicia o fluxo de emissão.
- Status e resposta da emissão retornam pelo mesmo caminho lógico: Diplomas atualiza o pedido (ou uma tabela espelho) e replica de volta para os serviços de origem para consulta.

## 5.5. Documento de diploma e assinatura
- Diplomas gera **DocumentoDiploma** automaticamente ao emitir o diploma.
- Assinatura eletrônica cria **DocumentoAssinável** a partir de `DocumentoDiploma` ou de `DocumentoOficial`.
- Solicitação concluída atualiza `StatusEmissao` para `ASSINADO`; rejeição/cancelamento mantém o documento pendente.

## 5.6. Solicitação de Assinatura e Certificados (tabela replicada)
- Ao criar um `DocumentoDiploma` ou `DocumentoOficial`, uma `SolicitacaoAssinatura` é aberta **somente** se não existir solicitação ativa/concluída para o mesmo documento.
- A criação da solicitação gera uma `Assinatura` em `PENDENTE` (pronta para assinar).
- Ao assinar, gera `ManifestoAssinatura`; em caso de rejeição/cancelamento, a solicitação é encerrada e permite nova solicitação futura.
- O resultado (assinatura concluída ou rejeitada) é replicado de volta, permitindo que Diplomas atualize o status do documento e do pedido.

## 5.7. Documentos oficiais (grad/pós → assinatura)
- Graduação e Pós publicam documentos oficiais próprios (`DocumentoOficialGraduacao` / `DocumentoOficialPos`).
- Esses documentos são espelhados em `DocumentoOficial` e viram `DocumentoAssinavel`.
- O fluxo de assinatura segue a mesma regra de criação de solicitação e geração de manifesto.
