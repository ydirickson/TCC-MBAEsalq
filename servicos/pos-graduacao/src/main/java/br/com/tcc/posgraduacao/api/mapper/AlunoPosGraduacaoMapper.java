package br.com.tcc.posgraduacao.api.mapper;

import br.com.tcc.posgraduacao.api.dto.AlunoPosGraduacaoResponse;
import br.com.tcc.posgraduacao.domain.model.AlunoPosGraduacao;
import br.com.tcc.posgraduacao.domain.model.ProfessorPosGraduacao;
import br.com.tcc.posgraduacao.domain.model.ProgramaPos;
import org.springframework.stereotype.Component;

@Component
public class AlunoPosGraduacaoMapper {

  public AlunoPosGraduacaoResponse toResponse(AlunoPosGraduacao entity) {
    if (entity == null) {
      return null;
    }
    ProgramaPos programa = entity.getPrograma();
    Long programaId = programa != null ? programa.getId() : null;
    String programaCodigo = programa != null ? programa.getCodigo() : null;
    String programaNome = programa != null ? programa.getNome() : null;
    ProfessorPosGraduacao orientador = entity.getOrientador();
    Long orientadorId = orientador != null ? orientador.getId() : null;

    return new AlunoPosGraduacaoResponse(
        entity.getId(),
        entity.getPessoaId(),
        programaId,
        programaCodigo,
        programaNome,
        orientadorId,
        entity.getDataMatricula(),
        entity.getDataConclusao(),
        entity.getStatus());
  }
}
