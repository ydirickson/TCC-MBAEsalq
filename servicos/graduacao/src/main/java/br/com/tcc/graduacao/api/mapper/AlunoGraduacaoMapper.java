package br.com.tcc.graduacao.api.mapper;

import br.com.tcc.graduacao.api.dto.AlunoGraduacaoResponse;
import br.com.tcc.graduacao.domain.model.AlunoGraduacao;
import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import br.com.tcc.graduacao.domain.model.TurmaGraduacao;
import org.springframework.stereotype.Component;

@Component
public class AlunoGraduacaoMapper {

  public AlunoGraduacaoResponse toResponse(AlunoGraduacao entity) {
    if (entity == null) {
      return null;
    }
    CursoGraduacao curso = entity.getCurso();
    Long cursoId = curso != null ? curso.getId() : null;
    String cursoCodigo = curso != null ? curso.getCodigo() : null;
    String cursoNome = curso != null ? curso.getNome() : null;
    TurmaGraduacao turma = entity.getTurma();
    String turmaId = turma != null ? turma.getId() : null;

    return new AlunoGraduacaoResponse(
        entity.getId(),
        entity.getPessoaId(),
        cursoId,
        cursoCodigo,
        cursoNome,
        turmaId,
        entity.getDataMatricula(),
        entity.getDataConclusao(),
        entity.getStatus());
  }
}
