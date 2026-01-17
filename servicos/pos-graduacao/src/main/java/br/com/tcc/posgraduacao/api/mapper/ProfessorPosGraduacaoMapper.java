package br.com.tcc.posgraduacao.api.mapper;

import br.com.tcc.posgraduacao.api.dto.ProfessorPosGraduacaoResponse;
import br.com.tcc.posgraduacao.domain.model.ProfessorPosGraduacao;
import br.com.tcc.posgraduacao.domain.model.ProgramaPos;
import org.springframework.stereotype.Component;

@Component
public class ProfessorPosGraduacaoMapper {

  public ProfessorPosGraduacaoResponse toResponse(ProfessorPosGraduacao entity) {
    if (entity == null) {
      return null;
    }
    ProgramaPos programa = entity.getPrograma();
    Long programaId = programa != null ? programa.getId() : null;
    String programaCodigo = programa != null ? programa.getCodigo() : null;
    String programaNome = programa != null ? programa.getNome() : null;

    return new ProfessorPosGraduacaoResponse(
        entity.getId(),
        entity.getPessoaId(),
        programaId,
        programaCodigo,
        programaNome,
        entity.getDataIngresso(),
        entity.getNivelDocente(),
        entity.getStatus());
  }
}
