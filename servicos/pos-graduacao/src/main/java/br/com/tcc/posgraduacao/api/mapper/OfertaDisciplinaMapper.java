package br.com.tcc.posgraduacao.api.mapper;

import br.com.tcc.posgraduacao.api.dto.OfertaDisciplinaRequest;
import br.com.tcc.posgraduacao.api.dto.OfertaDisciplinaResponse;
import br.com.tcc.posgraduacao.domain.model.DisciplinaPos;
import br.com.tcc.posgraduacao.domain.model.OfertaDisciplina;
import br.com.tcc.posgraduacao.domain.model.ProfessorPosGraduacao;
import org.springframework.stereotype.Component;

@Component
public class OfertaDisciplinaMapper {

  public OfertaDisciplina toEntity(
      DisciplinaPos disciplina,
      ProfessorPosGraduacao professor,
      OfertaDisciplinaRequest request) {
    if (disciplina == null || professor == null || request == null) {
      return null;
    }
    OfertaDisciplina oferta = new OfertaDisciplina();
    oferta.setDisciplina(disciplina);
    oferta.setProfessor(professor);
    oferta.setAno(request.ano());
    oferta.setSemestre(request.semestre());
    return oferta;
  }

  public OfertaDisciplinaResponse toResponse(OfertaDisciplina oferta) {
    if (oferta == null) {
      return null;
    }
    Long disciplinaId = oferta.getDisciplina() != null ? oferta.getDisciplina().getId() : null;
    Long professorId = oferta.getProfessor() != null ? oferta.getProfessor().getId() : null;
    return new OfertaDisciplinaResponse(
        oferta.getId(),
        disciplinaId,
        professorId,
        oferta.getAno(),
        oferta.getSemestre());
  }

  public void updateEntityFromRequest(
      OfertaDisciplinaRequest request,
      DisciplinaPos disciplina,
      ProfessorPosGraduacao professor,
      OfertaDisciplina oferta) {
    if (request == null || disciplina == null || professor == null || oferta == null) {
      return;
    }
    oferta.setDisciplina(disciplina);
    oferta.setProfessor(professor);
    oferta.setAno(request.ano());
    oferta.setSemestre(request.semestre());
  }
}
