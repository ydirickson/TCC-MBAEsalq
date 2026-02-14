# Serviço de Pós-graduação

## Entidades
- Pessoa (owner secundário quando não existir no catálogo da graduação), DocumentoIdentificação, Contato, Endereço
- AlunoPósGraduação (perfil acadêmico, com orientador integrado)
- ProgramaPós
- DisciplinaPós, OfertaDisciplinaPós, MatrículaDisciplinaPós (nota global por disciplina/aluno)
- ProfessorPósGraduação
- VínculoAcadêmico (pessoa + programa referenciado via `CursoProgramaReferencia`)
- Defesa (qualificação ou defesa final)
- DefesaMembro (membros da banca com nota e presidente)
- SituaçãoAcadêmica (projeção do vínculo, status atual)
- DocumentoOficialPós (documentos oficiais emitidos pela pós)

## Regras de Negócio da Simulação
- Criação e atualização de `AlunoPosGraduacao` e `ProfessorPosGraduacao` criam/atualizam o `VinculoAcademico` correspondente.
- Mudança de status do vínculo (trancado, desligado, concluído) atualiza a situação acadêmica.
- Status `CONCLUIDO` exige `dataConclusao` preenchida no vínculo/aluno (ver [pendências](../pendencias.md) para o que ainda falta reforçar no banco).
- Dados acadêmicos de pós (`Defesa`, `DefesaMembro`, `MatriculaDisciplinaPos`) permanecem no domínio local da pós.
- Documento oficial é solicitado na Pós-graduação e encaminhado ao fluxo de assinatura: via banco em A1, via banco em A2 (pendente), e por composição de serviços em A3.

## Replicação por Arquitetura

### Diretriz de paridade
Todas as entidades compartilhadas devem ser replicadas da mesma forma entre serviços, seja por tabela (A1/A2) ou por eventos (A3). Qualquer divergência do objetivo está listada em [Pendências](../pendencias.md).

### A1 - DB Based (replicação via banco)
- Entrada em Pós: `graduacao.pessoa` -> `pos_graduacao.pessoa`
- Entrada em Pós: `graduacao.documento_identificacao` -> `pos_graduacao.documento_identificacao`
- Entrada em Pós: `graduacao.contato` -> `pos_graduacao.contato`
- Entrada em Pós: `graduacao.endereco` -> `pos_graduacao.endereco`
- Saída da Pós: `pos_graduacao.vinculo_academico` -> `diplomas.vinculo_academico`, `assinatura.vinculo_academico`
- `documento_oficial_pos` -> fluxo de assinatura no banco (`documento_oficial`, `documento_assinavel`, `solicitacao_assinatura`)

### A2 - CDC + Kafka (replicação por tabela capturada)
- Entrada em Pós: tópico `tcc.graduacao.pessoa` -> `pos_graduacao.pessoa`
- Saída da Pós: `tcc.pos_graduacao.vinculo_academico` -> `diplomas.vinculo_academico`, `assinatura.vinculo_academico`
- Captura de `tcc.pos_graduacao.pessoa` já configurada na origem da pós; integração completa de saída permanece pendente.
- Fluxo de documento oficial para assinatura no A2: previsto, ainda em implementação

### A3 - EDA + Kafka (eventos ainda a implementar)
| Evento | Significado | Compartilha |
| --- | --- | --- |
| `PessoaCriada` | Nova pessoa cadastrada na Pós-graduação. | `pessoaId`, `nome`, `dataNascimento`, `nomeSocial`, `versao`/`timestamp`. |
| `PessoaAtualizada` | Atualização cadastral de pessoa já existente. | `pessoaId`, dados cadastrais atualizados e `versao`/`timestamp`. |
| `VinculoAcademicoCriado` | Novo vínculo acadêmico (aluno/professor) criado na Pós. | `vinculoId`, `pessoaId`, `cursoId`, `cursoCodigo`, `cursoNome`, `cursoTipo`, `tipoVinculo`, `dataIngresso`, `situacao`. |
| `VinculoAcademicoAtualizado` | Alteração de situação ou dados do vínculo da Pós. | `vinculoId`, `pessoaId`, dados de vínculo atualizados, `dataConclusao` (quando existir), `versao`/`timestamp`. |
| `EventoAcademicoPos` | Evento acadêmico específico da Pós (orientação, qualificação, defesa). | `alunoId`, `orientadorId` (quando houver), `defesaId` (quando houver), `tipoDefesa`, `nota`, `banca`, `timestamp`. |
| `ConclusaoPublicada` | Conclusão acadêmica publicada para consumo externo. | `vinculoId`, `pessoaId`, `dataConclusao`, `situacao=CONCLUIDO`. |
| `DocumentoOficialCriado` | Emissão de novo documento oficial da Pós. | `documentoOficialId`, `origemServico`, `origemId`, `pessoaId`, `tipoDocumento`, `dataEmissao`, `versao`, `urlArquivo`, `hashDocumento`. |
| `DocumentoOficialAtualizado` | Atualização de versão/metadados de documento oficial da Pós. | `documentoOficialId`, metadados atualizados, `versao`, `timestamp`. |
| `SolicitacaoAssinaturaCriada` | Abertura de solicitação de assinatura. | `solicitacaoId`, `documentoAssinavelId`/`documentoOficialId`, `status`, `dataSolicitacao`. |
| `SolicitacaoAssinaturaCancelada` | Cancelamento de solicitação de assinatura. | `solicitacaoId`, `status`, `motivo`, `dataAtualizacao`. |
| `AssinaturaParcial` | Uma assinatura registrada, mas fluxo ainda incompleto. | `solicitacaoId`, `assinaturaId`, `assinanteId`, `status`, `dataAssinatura`. |
| `AssinaturaConcluida` | Processo de assinatura concluído. | `solicitacaoId`, `status=CONCLUIDA`, `dataConclusao`, `hashFinal` (quando aplicável). |
| `AssinaturaRejeitada` | Assinatura rejeitada por um assinante/regra. | `solicitacaoId`, `assinaturaId`, `status=REJEITADA`, `motivoRecusa`. |
