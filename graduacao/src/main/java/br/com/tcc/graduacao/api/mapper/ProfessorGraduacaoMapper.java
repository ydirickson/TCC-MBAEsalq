package br.com.tcc.graduacao.api.mapper;

import br.com.tcc.graduacao.api.dto.ProfessorGraduacaoResponse;
import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import br.com.tcc.graduacao.domain.model.ProfessorGraduacao;
import org.springframework.stereotype.Component;

@Component
public class ProfessorGraduacaoMapper {

  public ProfessorGraduacaoResponse toResponse(ProfessorGraduacao entity) {
    if (entity == null) {
      return null;
    }
    CursoGraduacao curso = entity.getCurso();
    Long cursoId = curso != null ? curso.getId() : null;
    String cursoCodigo = curso != null ? curso.getCodigo() : null;
    String cursoNome = curso != null ? curso.getNome() : null;

    return new ProfessorGraduacaoResponse(
        entity.getId(),
        entity.getPessoaId(),
        cursoId,
        cursoCodigo,
        cursoNome,
        entity.getDataIngresso(),
        entity.getNivelDocente(),
        entity.getStatus());
  }
}
