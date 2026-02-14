# 8. Pendências de implementação (arquivo de trabalho)
[← Voltar ao índice](./README.md)

Este documento centraliza as pendências de implementação e alinhamento entre módulos e arquiteturas.

## Módulo Graduação
- A2 ainda não replica `documento_identificacao`, `contato` e `endereco` a partir da Graduação para todos os destinos.
- Fluxo de assinatura a partir de `documento_oficial_graduacao` no A2 ainda não está implementado.
- Regra de `CONCLUIDO` com `dataConclusao` ainda não está garantida por constraint também em `vinculo_academico` (hoje o reforço está em `aluno_graduacao`).

## Módulo Pós-graduação
- A2 ainda não replica `documento_identificacao`, `contato` e `endereco` da Pós-graduação para todos os destinos.
- Em A2, a captura de `pos_graduacao.pessoa` existe, mas ainda faltam sinks de saída para concluir a replicação para destinos.
- Fluxo de assinatura a partir de `documento_oficial_pos` no A2 ainda não está implementado.
- No cenário C2+A1, o fluxo `documento_oficial_pos` -> assinatura via banco ainda não está automatizado.
- Regra de `CONCLUIDO` com `dataConclusao` ainda não está garantida por constraint também em `vinculo_academico` (hoje o reforço está em `aluno_pos_graduacao`).
- Integração automática de defesa aprovada com atualização de vínculo/conclusão publicada ainda não está implementada.

## Módulo Diplomas
- A2 ainda não replica `documento_identificacao`, `contato` e `endereco` para o schema `diplomas` (apenas `pessoa` e `vinculo_academico` estão no fluxo atual).
- No A2, fluxo automático de assinatura a partir de `documento_diploma` ainda não está implementado.
- No cenário C2+A1, automações de banco de `requerimento_diploma` por conclusão e de assinatura por `documento_diploma` ainda não estão ativas (hoje existem no C1/A1).
- Publicação e consumo de eventos de domínio no serviço de Diplomas (A3) ainda não estão implementados na aplicação.

## Módulo Assinatura
- A2 ainda não replica `documento_identificacao`, `contato` e `endereco` para o schema `assinatura` (apenas `pessoa` e `vinculo_academico` estão no fluxo atual).
- A2 ainda não replica `documento_diploma` e `documento_oficial` para o schema `assinatura` nem aciona a abertura automática de solicitação.
- No cenário C2+A1, automações de banco para `documento_diploma`/`documento_oficial` -> `documento_assinavel`/`solicitacao_assinatura` ainda não estão ativas (hoje existem no C1/A1).
- A aplicação `servicos/assinatura` ainda não suporta `documento_oficial` no modelo/API de `DocumentoAssinavel` (somente `documento_diploma`).
- Publicação e consumo de eventos de domínio no serviço de Assinatura (A3) ainda não estão implementados na aplicação.
- A2 ainda não tem sink de `tcc.pos_graduacao.pessoa` para `assinatura.pessoa`; a estratégia final de ownership/fonte de `Pessoa` precisa ser fechada.
