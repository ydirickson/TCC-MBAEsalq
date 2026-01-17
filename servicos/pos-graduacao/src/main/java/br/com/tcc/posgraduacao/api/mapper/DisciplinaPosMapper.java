package br.com.tcc.posgraduacao.api.mapper;

import br.com.tcc.posgraduacao.api.dto.DisciplinaPosRequest;
import br.com.tcc.posgraduacao.api.dto.DisciplinaPosResponse;
import br.com.tcc.posgraduacao.domain.model.DisciplinaPos;
import br.com.tcc.posgraduacao.domain.model.ProgramaPos;
import org.springframework.stereotype.Component;

@Component
public class DisciplinaPosMapper {

  public DisciplinaPos toEntity(ProgramaPos programa, DisciplinaPosRequest request) {
    if (programa == null || request == null) {
      return null;
    }
    DisciplinaPos disciplina = new DisciplinaPos();
    disciplina.setPrograma(programa);
    disciplina.setCodigo(request.codigo());
    disciplina.setNome(request.nome());
    disciplina.setCargaHoraria(request.cargaHoraria());
    return disciplina;
  }

  public DisciplinaPosResponse toResponse(DisciplinaPos disciplina) {
    if (disciplina == null) {
      return null;
    }
    Long programaId = disciplina.getPrograma() != null ? disciplina.getPrograma().getId() : null;
    return new DisciplinaPosResponse(
        disciplina.getId(),
        programaId,
        disciplina.getCodigo(),
        disciplina.getNome(),
        disciplina.getCargaHoraria());
  }

  public void updateEntityFromRequest(DisciplinaPosRequest request, DisciplinaPos disciplina) {
    if (request == null || disciplina == null) {
      return;
    }
    disciplina.setCodigo(request.codigo());
    disciplina.setNome(request.nome());
    disciplina.setCargaHoraria(request.cargaHoraria());
  }
}
