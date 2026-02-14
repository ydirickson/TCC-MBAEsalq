# Serviço de Assinatura Eletrônica

## Entidades
- UsuárioAssinante (ligado a Pessoa)
- DocumentoAssinável (origem atual em `DocumentoDiploma`; suporte completo a `DocumentoOficial` ainda pendente na aplicação)
- SolicitaçãoAssinatura
- Assinatura
- ManifestoAssinatura (auditoria, carimbo de tempo e hash final)
- Read models: Pessoa, DocumentoIdentificação, Contato, Endereço, VínculoAcadêmico e DocumentoDiploma

## Regras de Negócio da Simulação
- `DocumentoAssinavel` só é criado quando o `DocumentoDiploma` informado existe.
- `SolicitacaoAssinatura` é subrecurso de `DocumentoAssinavel` e só pode ser criada se não houver solicitação em `PENDENTE`, `PARCIAL` ou `CONCLUIDA` para o mesmo documento.
- A criação da solicitação gera automaticamente uma `Assinatura` em `PENDENTE` (se ainda não existir assinatura para a solicitação).
- Cancelamento de solicitação muda o status para `CANCELADA` (exceto quando já `CONCLUIDA`) e marca assinaturas pendentes como `REJEITADA`.
- `Assinatura` em `ASSINADA` conclui a solicitação e gera `ManifestoAssinatura` automaticamente.
- `Assinatura` em `REJEITADA` encerra a solicitação como `REJEITADA`.
- Integrações Kafka/EDA no serviço de assinatura ainda não estão implementadas na aplicação (ver [pendências](../pendencias.md)).

## Replicação por Arquitetura

### Diretriz de paridade
Todas as entidades compartilhadas devem ser replicadas da mesma forma entre serviços, seja por tabela (A1/A2) ou por eventos (A3). Qualquer divergência do objetivo está listada em [Pendências](../pendencias.md).

### A1 - DB Based (replicação via banco)
- Entrada em Assinatura: `graduacao.pessoa` -> `assinatura.pessoa`
- Entrada em Assinatura: `graduacao.documento_identificacao` -> `assinatura.documento_identificacao`
- Entrada em Assinatura: `graduacao.contato` -> `assinatura.contato`
- Entrada em Assinatura: `graduacao.endereco` -> `assinatura.endereco`
- Entrada em Assinatura: `graduacao.vinculo_academico` -> `assinatura.vinculo_academico`
- Entrada em Assinatura: `pos_graduacao.vinculo_academico` -> `assinatura.vinculo_academico`
- Entrada em Assinatura: `diplomas.documento_diploma` -> `assinatura.documento_diploma` (bootstrap por seed em C2+A1; fluxo automático ativo em C1/A1).
- No C1/A1: `documento_diploma` gera `documento_assinavel` e `solicitacao_assinatura`; `solicitacao_assinatura` gera `assinatura` pendente; `assinatura ASSINADA` gera `manifesto_assinatura`.
- No C1/A1: `graduacao.documento_oficial_graduacao` e `pos_graduacao.documento_oficial_pos` alimentam `assinatura.documento_oficial` e abrem fluxo automático de assinatura.
- No C2+A1: automações por banco para documentos (diploma/oficial) ainda não estão ativas (ver [pendências](../pendencias.md)).

### A2 - CDC + Kafka (replicação por tabela capturada)
- Entrada em Assinatura: tópico `tcc.graduacao.pessoa` -> `assinatura.pessoa`
- Entrada em Assinatura: tópico `tcc.graduacao.vinculo_academico` -> `assinatura.vinculo_academico`
- Entrada em Assinatura: tópico `tcc.pos_graduacao.vinculo_academico` -> `assinatura.vinculo_academico`
- Replicação A2 de `documento_identificacao`, `contato`, `endereco`, `documento_diploma` e `documento_oficial` para assinatura: prevista, ainda em implementação.
- Fluxo automático de assinatura a partir de `documento_diploma` e `documento_oficial` no A2: previsto, ainda em implementação.

### A3 - EDA + Kafka (eventos ainda a implementar)
| Evento | Papel | Significado | Compartilha |
| --- | --- | --- | --- |
| `PessoaCriada` | Consome | Novo cadastro de pessoa para manter read model local. | `pessoaId`, `nome`, `dataNascimento`, `nomeSocial`, `versao`/`timestamp`. |
| `PessoaAtualizada` | Consome | Atualização cadastral de pessoa usada na assinatura. | `pessoaId`, dados atualizados, `versao`/`timestamp`. |
| `VinculoAcademicoAtualizado` | Consome | Atualiza contexto acadêmico vinculado ao signatário/documento. | `vinculoId`, `pessoaId`, `situacao`, `curso*`, `dataConclusao`, `versao`/`timestamp`. |
| `DocumentoDiplomaCriado` | Consome | Novo documento de diploma disponível para assinatura. | `documentoDiplomaId`, `diplomaId`, `versao`, `dataGeracao`, `urlArquivo`, `hashDocumento`. |
| `DocumentoDiplomaAtualizado` | Consome | Nova versão/reemissão de documento de diploma. | `documentoDiplomaId`, `diplomaId`, `versao`, `dataGeracao`, `urlArquivo`, `hashDocumento`, `timestamp`. |
| `DocumentoOficialCriado` | Consome | Novo documento oficial (graduação/pós) disponível para assinatura. | `documentoOficialId`, `origemServico`, `origemId`, `pessoaId`, `tipoDocumento`, `dataEmissao`, `versao`, `urlArquivo`, `hashDocumento`. |
| `DocumentoOficialAtualizado` | Consome | Atualização de versão/metadados de documento oficial. | `documentoOficialId`, metadados atualizados, `versao`, `timestamp`. |
| `SolicitacaoAssinaturaCriada` | Publica | Solicitação de assinatura aberta para um documento assinável. | `solicitacaoId`, `documentoAssinavelId`, `documentoDiplomaId`/`documentoOficialId`, `status`, `dataSolicitacao`. |
| `SolicitacaoAssinaturaCancelada` | Publica | Solicitação cancelada no serviço de assinatura. | `solicitacaoId`, `status=CANCELADA`, `dataAtualizacao`, `motivo` (quando houver). |
| `AssinaturaParcial` | Publica | Uma assinatura registrada, mas fluxo ainda não concluído. | `solicitacaoId`, `assinaturaId`, `assinanteId`, `status`, `dataAssinatura`. |
| `AssinaturaConcluida` | Publica | Assinatura concluída com sucesso. | `solicitacaoId`, `assinaturaId`, `status=CONCLUIDA`, `dataConclusao`, `hashFinal`. |
| `AssinaturaRejeitada` | Publica | Assinatura rejeitada. | `solicitacaoId`, `assinaturaId`, `status=REJEITADA`, `motivoRecusa`, `dataAssinatura`. |

## Interfaces REST (simulação)
- Pessoas (read model): `GET/POST/PUT/DELETE /pessoas`
- Documentos de diploma (read model): `GET/POST/PUT/DELETE /documentos-diploma`
- Usuários assinantes: `GET/POST/PUT/DELETE /usuarios-assinantes`
- Documentos assináveis: `GET/POST/PUT/DELETE /documentos-assinaveis`
- Solicitações (subrecurso): `GET/POST/PUT/DELETE /documentos-assinaveis/{documentoAssinavelId}/solicitacoes-assinatura`
- Assinaturas: `GET/POST/PUT/DELETE /assinaturas`
- Manifestos: `GET/POST/PUT/DELETE /manifestos-assinatura`
