# Serviço de Diplomas

## Entidades
- RequerimentoDiploma
- BaseEmissãoDiploma (snapshot de Pessoa, Curso/Programa e Conclusão no momento da solicitação)
- Diploma
- DocumentoDiploma (metadados do PDF emitido)
- StatusEmissão (estado do fluxo)
- Read models: Pessoa (cópia) e VínculoAcadêmico (cópia, com `CursoProgramaReferencia` embutido)

## Regras de Negócio da Simulação
- Ao registrar `RequerimentoDiploma`, valida `Pessoa` e `VinculoAcademico` e exige vínculo com situação `CONCLUIDO`.
- A criação de requerimento gera automaticamente `BaseEmissaoDiploma` (snapshot dos dados) e `StatusEmissao` inicial como `SOLICITADO`.
- A emissão cria `Diploma`, atualiza `StatusEmissao` para `EMITIDO` e gera automaticamente `DocumentoDiploma` inicial (versão 1).
- Reemissão/atualização de documento é feita via operações de `DocumentoDiploma`.
- Atualização automática de `StatusEmissao` com resultado de assinatura depende da arquitetura/cenário (ver [pendências](../pendencias.md)).

## Replicação por Arquitetura

### Diretriz de paridade
Todas as entidades compartilhadas devem ser replicadas da mesma forma entre serviços, seja por tabela (A1/A2) ou por eventos (A3). Qualquer divergência do objetivo está listada em [Pendências](../pendencias.md).

### A1 - DB Based (replicação via banco)
- Entrada em Diplomas: `graduacao.pessoa` -> `diplomas.pessoa`
- Entrada em Diplomas: `graduacao.documento_identificacao` -> `diplomas.documento_identificacao`
- Entrada em Diplomas: `graduacao.contato` -> `diplomas.contato`
- Entrada em Diplomas: `graduacao.endereco` -> `diplomas.endereco`
- Entrada em Diplomas: `graduacao.vinculo_academico` -> `diplomas.vinculo_academico`
- Entrada em Diplomas: `pos_graduacao.vinculo_academico` -> `diplomas.vinculo_academico`
- No cenário C1/A1: vínculo concluído gera `requerimento_diploma` + `base_emissao_diploma` + `status_emissao`.
- No cenário C1/A1: `documento_diploma` gera fluxo de assinatura em banco (`documento_assinavel`, `solicitacao_assinatura`) e atualiza `status_emissao`.

### A2 - CDC + Kafka (replicação por tabela capturada)
- Entrada em Diplomas: tópico `tcc.graduacao.pessoa` -> `diplomas.pessoa`
- Entrada em Diplomas: tópico `tcc.graduacao.vinculo_academico` -> `diplomas.vinculo_academico`
- Entrada em Diplomas: tópico `tcc.pos_graduacao.vinculo_academico` -> `diplomas.vinculo_academico`
- Fluxo automático de requerimento por conclusão e fluxo automático de assinatura por `documento_diploma` no A2: previstos, ainda em implementação.

### A3 - EDA + Kafka (eventos ainda a implementar)
| Evento | Papel | Significado | Compartilha |
| --- | --- | --- | --- |
| `VinculoAcademicoAtualizado` | Consome | Atualização de vínculo para manter elegibilidade local. | `vinculoId`, `pessoaId`, `situacao`, `dataConclusao`, `curso*`, `versao`/`timestamp`. |
| `ConclusaoPublicada` | Consome | Conclusão acadêmica publicada por serviço acadêmico. | `vinculoId`, `pessoaId`, `dataConclusao`, `situacao=CONCLUIDO`. |
| `DiplomaEmitido` | Publica | Diploma emitido com sucesso. | `diplomaId`, `requerimentoId`, `pessoaId`, `vinculoId`, `numeroRegistro`, `dataEmissao`. |
| `DocumentoDiplomaCriado` | Publica | Primeira versão do documento do diploma gerada. | `documentoDiplomaId`, `diplomaId`, `versao`, `dataGeracao`, `urlArquivo`, `hashDocumento`. |
| `DocumentoDiplomaAtualizado` | Publica | Nova versão/reemissão do documento do diploma. | `documentoDiplomaId`, `diplomaId`, `versao`, `dataGeracao`, `urlArquivo`, `hashDocumento`, `timestamp`. |
| `SolicitacaoAssinaturaCriada` | Publica | Solicitação de assinatura aberta para o documento de diploma. | `solicitacaoId`, `documentoAssinavelId`/`documentoDiplomaId`, `status`, `dataSolicitacao`. |
| `AssinaturaConcluida` | Consome | Assinatura finalizada com sucesso. | `solicitacaoId`, `status=CONCLUIDA`, `dataConclusao`, `hashFinal` (quando aplicável). |
| `AssinaturaRejeitada` | Consome | Assinatura rejeitada. | `solicitacaoId`, `assinaturaId`, `status=REJEITADA`, `motivoRecusa`. |

## Interfaces REST (simulação)
- Read model de pessoas: `GET/POST/PUT/DELETE /pessoas`
- Read model de vínculos: `GET/POST/PUT/DELETE /vinculos`
- Requerimentos: `GET/POST/PUT/DELETE /requerimentos`
- Bases de emissão: `GET/POST/PUT/DELETE /bases-emissao`
- Diplomas: `GET/POST/PUT/DELETE /diplomas`
- Documento do diploma como subrecurso: `GET/POST/PUT/DELETE /diplomas/{id}/documentos`
- Status de emissão: `GET/POST/PUT/DELETE /status-emissao`
