# Serviço de Pós-graduação

## Entidades
- Pessoa (owner secundário quando não existir no catálogo da graduação), DocumentoIdentificação, Contato, Endereço
- AlunoPósGraduação (perfil acadêmico, com orientador integrado)
- ProgramaPós
- DisciplinaPós, OfertaDisciplinaPós, MatrículaDisciplinaPós (nota global por disciplina/aluno)
- ProfessorPósGraduação
- VínculoAcadêmico — vínculo unificado por pessoa/programa
- Defesa (qualificação ou defesa final)
- DefesaMembro (membros da banca com nota e presidente)
- SituaçãoAcadêmica (projeção do vínculo, status atual)
- DocumentoOficialPós (documentos oficiais emitidos pela pós)

## Regras de Negócio da Simulação
- Cria Pessoa somente se não encontrada localmente e publica `PessoaCriada`; caso encontre versão replicada, atualiza e publica `PessoaAtualizada`.
- Criação de vínculo (status=ativo) publica `VinculoAcademicoCriado`.
- Atualizações de orientador ou defesa publicam `EventoAcademicoPos` (carga de escrita) e, quando defesa aprovada, `ConclusaoPublicada` com `vinculoId`.
- Mudanças de status do vínculo publicam `VinculoAcademicoAtualizado`.
- Status `CONCLUIDO` exige `dataConclusao` preenchida no vínculo/aluno.
- Documento oficial criado/atualizado gera fluxo de assinatura (`DocumentoAssinavel` + `SolicitacaoAssinatura`).
- Consome eventos de Diploma/Assinatura apenas para read models (`DocumentoDiploma`, `Assinatura` cópias).
