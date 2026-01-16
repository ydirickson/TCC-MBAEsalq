package br.com.tcc.graduacao.api.mapper;

import br.com.tcc.graduacao.api.dto.OfertaDisciplinaRequest;
import br.com.tcc.graduacao.api.dto.OfertaDisciplinaResponse;
import br.com.tcc.graduacao.domain.model.DisciplinaGraduacao;
import br.com.tcc.graduacao.domain.model.OfertaDisciplina;
import br.com.tcc.graduacao.domain.model.ProfessorGraduacao;
import org.springframework.stereotype.Component;

@Component
public class OfertaDisciplinaMapper {

  public OfertaDisciplina toEntity(
      DisciplinaGraduacao disciplina,
      ProfessorGraduacao professor,
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
      DisciplinaGraduacao disciplina,
      ProfessorGraduacao professor,
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
