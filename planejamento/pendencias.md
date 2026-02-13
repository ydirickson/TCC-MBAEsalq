# 10. Pendências de implementação (arquivo de trabalho)
[← Voltar ao índice](./README.md)

Este documento centraliza as pendências de implementação e alinhamento entre módulos e arquiteturas.

## Módulo Graduação
- A2 ainda não replica `documento_identificacao`, `contato` e `endereco` a partir da Graduação para todos os destinos.
- Fluxo de assinatura a partir de `documento_oficial_graduacao` no A2 ainda não está implementado.
- Regra de `CONCLUIDO` com `dataConclusao` ainda não está garantida por constraint também em `vinculo_academico` (hoje o reforço está em `aluno_graduacao`).
