# Serviço de Pós-graduação

## Entidades
- Pessoa (owner secundário quando não existir no catálogo da graduação), DocumentoIdentificacao, Contato, Endereco
- AlunoPosGraduacao (perfil acadêmico)
- ProgramaPos
- VinculoAcademico (origem=pos) — vínculo unificado por pessoa/programa
- Orientacao
- Qualificacao
- Defesa
- SituacaoAcademica (projeção do vínculo, status atual)

## Regras de Negócio da Simulação
- Cria Pessoa somente se não encontrada localmente (origem=pos) e publica `PessoaCriada`; caso encontre versão replicada, atualiza e publica `PessoaAtualizada`.
- Criação de vínculo (origem=pos, status=ativo) publica `VinculoAcademicoCriado`.
- Atualizações de orientador, qualificação ou defesa publicam `EventoAcademicoPos` (carga de escrita) e, quando defesa aprovada, `ConclusaoPublicada` com `academicLinkId`.
- Mudanças de status do vínculo publicam `VinculoAcademicoAtualizado`.
- Consome eventos de Diploma/Assinatura apenas para read models (`DocumentoDiploma`, `Assinatura` cópias).
