# Serviço de Graduação

## Entidades
- Pessoa (owner primário se adotado), DocumentoIdentificacao, Contato, Endereco
- AlunoGraduacao (perfil acadêmico)
- CursoGraduacao
- VinculoAcademico (origem=grad) — vínculo unificado por pessoa/curso
- Disciplina, Turma, MatriculaDisciplina
- HistoricoAcademicoGraduacao
- SituacaoAcademica (projeção do vínculo, status atual)

## Regras de Negócio da Simulação
- Criação de Pessoa e do VinculoAcademico (origem=grad, status=ativo) emite eventos `PessoaCriada` e `VinculoAcademicoCriado`.
- Atualizações de contato/endereço emitem `PessoaAtualizada` (replicação em outros serviços).
- Mudança de status do vínculo (trancado, reaberto, desligado, concluído) emite `VinculoAcademicoAtualizado`; status concluído emite também `ConclusaoPublicada` com `academicLinkId`.
- Notas/frequência geram registros em Historico; não são replicados fora, apenas agregam carga de escrita.
- Consome eventos de Diploma/Assinatura apenas para read models locais opcionais (`DocumentoDiploma`, `Assinatura` cópias).

## Diagrama de Entidades
```mermaid
erDiagram
  Pessoa ||--o{ DocumentoIdentificacao : possui
  Pessoa ||--o{ Contato : possui
  Pessoa ||--o{ Endereco : reside_em

  Pessoa ||--o{ VinculoAcademico : vinculo
  VinculoAcademico }o--|| CursoGraduacao : matriculado_em
  VinculoAcademico ||--|{ SituacaoAcademica : status_atual

  CursoGraduacao ||--o{ Disciplina : oferece
  Disciplina ||--o{ Turma : turmas
  Turma ||--o{ MatriculaDisciplina : matriculas
  MatriculaDisciplina }o--|| AlunoGraduacao : de
  MatriculaDisciplina ||--o{ HistoricoAcademicoGraduacao : gera_registros
```

## Fluxo de Eventos e Read Models
```mermaid
flowchart LR
  subgraph Origem[Graduação]
    PessoaCriada -->|replica dados| PessoaAtualizada
    VinculoCriado[VinculoAcademicoCriado]
    VinculoAtualizado[VinculoAcademicoAtualizado]
    Conclusao[ConclusaoPublicada]
  end

  PessoaCriada --> VinculoCriado
  VinculoCriado --> SituacaoAtiva[SituacaoAcademica: ativo]
  VinculoAtualizado --> SituacaoAtiva
  Conclusao --> SituacaoConcluida[SituacaoAcademica: concluido]

  PessoaAtualizada -->|replica| DiplomaReadModel[DocumentoDiploma]
  PessoaAtualizada -->|replica| AssinaturaReadModel[Assinatura]
  Conclusao --> DiplomaReadModel
  Conclusao --> AssinaturaReadModel
```
