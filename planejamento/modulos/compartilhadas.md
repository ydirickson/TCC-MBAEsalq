# Conceitos Compartilhados

Este documento consolida apenas os conceitos compartilhados que estao aderentes ao que esta implementado hoje no repositorio.

## Entidades Comuns Implementadas

### Presenca em todos os servicos (Graduacao, Pos-graduacao, Diplomas e Assinatura)
- `pessoa`: `id`, `nome`, `data_nascimento`, `nome_social`
- `documento_identificacao`: `id`, `pessoa_id`, `tipo`, `numero`
- `contato`: `id`, `pessoa_id`, `email`, `telefone`
- `endereco`: `id`, `pessoa_id`, `logradouro`, `cidade`, `uf`, `cep`
- `vinculo_academico`: `id`, `pessoa_id`, `curso_id`, `curso_codigo`, `curso_nome`, `curso_tipo`, `tipo_vinculo`, `data_ingresso`, `data_conclusao`, `situacao`
- `vw_vinculo_academico_completo` (view helper)
- `outbox_eventos` e `inbox_eventos` (estrutura de apoio para EDA)

### Compartilhadas parcialmente (nao estao em todos os servicos)
- `documento_diploma`: implementada em `diplomas` (origem) e `assinatura` (read model de referencia).
- `documento_oficial`: implementada como `documento_oficial_graduacao` (Graduacao), `documento_oficial_pos` (Pos) e `documento_oficial` (Assinatura).

## Replicacao Compartilhada por Arquitetura (estado atual)

### A1 - DB Based
- C1/A1: sem replicacao fisica entre servicos (tabelas no mesmo schema de experimento).
- C2+A1:
  - `graduacao.pessoa` -> `pos_graduacao.pessoa`, `diplomas.pessoa`, `assinatura.pessoa`
  - `graduacao.documento_identificacao` -> `pos_graduacao.documento_identificacao`, `diplomas.documento_identificacao`, `assinatura.documento_identificacao`
  - `graduacao.contato` -> `pos_graduacao.contato`, `diplomas.contato`, `assinatura.contato`
  - `graduacao.endereco` -> `pos_graduacao.endereco`, `diplomas.endereco`, `assinatura.endereco`
  - `graduacao.vinculo_academico` -> `diplomas.vinculo_academico`, `assinatura.vinculo_academico`
  - `pos_graduacao.vinculo_academico` -> `diplomas.vinculo_academico`, `assinatura.vinculo_academico`
- Em C2+A1, parte dos fluxos automaticos de documentos ainda depende de implementacao (ver [pendencias](../pendencias.md)).

### A2 - CDC + Kafka
- Conectores implementados hoje:
  - `tcc.graduacao.pessoa` -> `pos_graduacao.pessoa`, `diplomas.pessoa`, `assinatura.pessoa`
  - `tcc.graduacao.vinculo_academico` -> `diplomas.vinculo_academico`, `assinatura.vinculo_academico`
  - `tcc.pos_graduacao.vinculo_academico` -> `diplomas.vinculo_academico`, `assinatura.vinculo_academico`
- Existe source para `pos_graduacao.pessoa`, mas ainda sem sinks ativos para distribuicao.
- Ainda nao ha CDC implementado para `documento_identificacao`, `contato`, `endereco`, `documento_diploma` e `documento_oficial`.

### A3 - EDA + Kafka
- Nao ha produtores/consumidores de eventos de dominio implementados na aplicacao dos servicos.
- As tabelas `outbox_eventos`/`inbox_eventos` existem, mas o fluxo de publicacao/consumo A3 ainda esta pendente.
- A lista de eventos de dominio permanece documentada por modulo como planejada (Graduacao, Pos, Diplomas e Assinatura).

## Regras Gerais Implementadas
- Chaves de entidades compartilhadas usam `BIGINT` (`id`).
- As tabelas comuns (`pessoa`, `documento_identificacao`, `contato`, `endereco`, `vinculo_academico`) mantem o mesmo contrato de colunas nos quatro servicos.
- No A1/A2, sincronizacao tecnica aplica `upsert` por chave primaria para manter idempotencia de replicacao.
