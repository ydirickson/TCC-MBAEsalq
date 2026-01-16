# 5. Definição das intersecções e regras de replicação
[← Voltar ao índice](./README.md)

## 5.1. Pessoa (intersecção principal)
- Pessoa é usada por todos os serviços.
- **Fontes de produção:** Graduação e Pós criam e atualizam Pessoa; Diplomas e Assinatura apenas consomem (mantêm cópias locais/read models).
- Regra de criação: Pessoa pode nascer em qualquer um dos dois serviços principais (Graduação ou Pós) e ambos publicam `PessoaCriada`.
- Regra de atualização: Graduação e Pós podem atualizar Pessoa; ambos publicam `PessoaAtualizada`. Consumidores aplicam de forma idempotente (id + timestamp/versão) em seus read models.

## 5.2. Vínculo Acadêmico (VinculoAcademico)
- **Cardinalidade:** uma Pessoa pode ter múltiplos vínculos (1:N), por exemplo duas graduações, um mestrado e um doutorado, cada um com seu próprio `vinculoId`.
- **Histórico:** manter tabela de histórico/versões de `VinculoAcademico` para registrar mudanças de status/curso/orientador (auditoria).
- **Fontes de produção:** Graduação e Pós criam e atualizam `VinculoAcademico`.
- Eventos: `VinculoAcademicoCriado` (status inicial ativo) e `VinculoAcademicoAtualizado` (mudança de status, curso/programa, orientador/colegiado).
- Consumo: Diplomas e Assinatura mantêm read models; Graduação e Pós também consomem para reconciliação.
- Status “concluído” gera `ConclusaoPublicada` (coberto na seção seguinte).

## 5.3. Conclusão / elegibilidade para diploma
- Diplomas precisa saber quando um vínculo (grad/pós) está concluído.
- Graduação e Pós produzem o estado “concluído” (ou equivalente).
- Diplomas consome esse estado para permitir emissão.

## 5.4. Pedido de Diploma (tabela replicada)
- Quando um vínculo é marcado como concluído, Graduação ou Pós cria um registro em uma tabela de `PedidoDiploma` (ou `RequerimentoDiploma`) local.
- Essa tabela é replicada para o serviço de Diplomas, que consome os pedidos e inicia o fluxo de emissão.
- Status e resposta da emissão retornam pelo mesmo caminho lógico: Diplomas atualiza o pedido (ou uma tabela espelho) e replica de volta para os serviços de origem para consulta.

## 5.5. Documento de diploma e assinatura
- Diplomas gera **DocumentoDiploma** quando emite.
- Assinatura eletrônica consome o evento de documento gerado para criar **DocumentoAssinavel**.
- Assinatura produz eventos de “assinatura concluída” que Diplomas pode consumir para atualizar status do diploma/documento.

## 5.6. Solicitação de Assinatura e Certificados (tabela replicada)
- Ao criar um `DocumentoDiploma`, Diplomas insere/atualiza uma tabela de `SolicitacaoAssinatura` (ou equivalente) que é replicada para o serviço de Assinatura Eletrônica.
- O serviço de Assinatura consome a tabela replicada, cria o fluxo de assinatura e registra certificados simulados.
- O resultado (assinatura concluída, rejeitada ou parcial) é replicado de volta pela mesma via, permitindo que Diplomas atualize o status do documento e do pedido.
