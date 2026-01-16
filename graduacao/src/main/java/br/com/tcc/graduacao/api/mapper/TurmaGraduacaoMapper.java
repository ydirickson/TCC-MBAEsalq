package br.com.tcc.graduacao.api.mapper;

import br.com.tcc.graduacao.api.dto.TurmaGraduacaoRequest;
import br.com.tcc.graduacao.api.dto.TurmaGraduacaoResponse;
import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import br.com.tcc.graduacao.domain.model.TurmaGraduacao;
import org.springframework.stereotype.Component;

@Component
public class TurmaGraduacaoMapper {

  public TurmaGraduacao toEntity(CursoGraduacao curso, TurmaGraduacaoRequest request) {
    if (curso == null || request == null) {
      return null;
    }
    TurmaGraduacao turma = new TurmaGraduacao();
    turma.setCurso(curso);
    turma.setAno(request.ano());
    turma.setSemestre(request.semestre());
    turma.setStatus(request.status());
    return turma;
  }

  public TurmaGraduacaoResponse toResponse(TurmaGraduacao turma) {
    if (turma == null) {
      return null;
    }
    Long cursoId = turma.getCurso() != null ? turma.getCurso().getId() : null;
    return new TurmaGraduacaoResponse(
        turma.getId(),
        cursoId,
        turma.getAno(),
        turma.getSemestre(),
        turma.getStatus());
  }

  public void updateEntityFromRequest(TurmaGraduacaoRequest request, TurmaGraduacao turma) {
    if (request == null || turma == null) {
      return;
    }
    turma.setAno(request.ano());
    turma.setSemestre(request.semestre());
    turma.setStatus(request.status());
  }
}
