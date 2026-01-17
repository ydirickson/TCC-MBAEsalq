# Serviço de Pós-graduação

## Entidades
- Pessoa (owner secundário quando não existir no catálogo da graduação), DocumentoIdentificacao, Contato, Endereco
- AlunoPosGraduacao (perfil acadêmico, com orientador integrado)
- ProgramaPos
- DisciplinaPos, OfertaDisciplinaPos, MatriculaDisciplinaPos (nota global por disciplina/aluno)
- ProfessorPosGraduacao
- VinculoAcademico — vínculo unificado por pessoa/programa
- Defesa (qualificação ou defesa final)
- DefesaMembro (membros da banca com nota e presidente)
- SituacaoAcademica (projeção do vínculo, status atual)

## Regras de Negócio da Simulação
- Cria Pessoa somente se não encontrada localmente e publica `PessoaCriada`; caso encontre versão replicada, atualiza e publica `PessoaAtualizada`.
- Criação de vínculo (status=ativo) publica `VinculoAcademicoCriado`.
- Atualizações de orientador ou defesa publicam `EventoAcademicoPos` (carga de escrita) e, quando defesa aprovada, `ConclusaoPublicada` com `vinculoId`.
- Mudanças de status do vínculo publicam `VinculoAcademicoAtualizado`.
- Consome eventos de Diploma/Assinatura apenas para read models (`DocumentoDiploma`, `Assinatura` cópias).
