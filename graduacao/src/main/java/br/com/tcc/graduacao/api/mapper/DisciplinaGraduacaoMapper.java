package br.com.tcc.graduacao.api.mapper;

import br.com.tcc.graduacao.api.dto.DisciplinaGraduacaoRequest;
import br.com.tcc.graduacao.api.dto.DisciplinaGraduacaoResponse;
import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import br.com.tcc.graduacao.domain.model.DisciplinaGraduacao;
import org.springframework.stereotype.Component;

@Component
public class DisciplinaGraduacaoMapper {

  public DisciplinaGraduacao toEntity(CursoGraduacao curso, DisciplinaGraduacaoRequest request) {
    if (curso == null || request == null) {
      return null;
    }
    DisciplinaGraduacao disciplina = new DisciplinaGraduacao();
    disciplina.setCurso(curso);
    disciplina.setCodigo(request.codigo());
    disciplina.setNome(request.nome());
    disciplina.setCargaHoraria(request.cargaHoraria());
    return disciplina;
  }

  public DisciplinaGraduacaoResponse toResponse(DisciplinaGraduacao disciplina) {
    if (disciplina == null) {
      return null;
    }
    Long cursoId = disciplina.getCurso() != null ? disciplina.getCurso().getId() : null;
    return new DisciplinaGraduacaoResponse(
        disciplina.getId(),
        cursoId,
        disciplina.getCodigo(),
        disciplina.getNome(),
        disciplina.getCargaHoraria());
  }

  public void updateEntityFromRequest(DisciplinaGraduacaoRequest request, DisciplinaGraduacao disciplina) {
    if (request == null || disciplina == null) {
      return;
    }
    disciplina.setCodigo(request.codigo());
    disciplina.setNome(request.nome());
    disciplina.setCargaHoraria(request.cargaHoraria());
  }
}
