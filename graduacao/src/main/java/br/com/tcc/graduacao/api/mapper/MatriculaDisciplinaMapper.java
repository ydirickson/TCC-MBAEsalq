package br.com.tcc.graduacao.api.mapper;

import br.com.tcc.graduacao.api.dto.MatriculaDisciplinaRequest;
import br.com.tcc.graduacao.api.dto.MatriculaDisciplinaResponse;
import br.com.tcc.graduacao.domain.model.AlunoGraduacao;
import br.com.tcc.graduacao.domain.model.MatriculaDisciplina;
import br.com.tcc.graduacao.domain.model.OfertaDisciplina;
import org.springframework.stereotype.Component;

@Component
public class MatriculaDisciplinaMapper {

  public MatriculaDisciplina toEntity(
      AlunoGraduacao aluno,
      OfertaDisciplina ofertaDisciplina,
      MatriculaDisciplinaRequest request) {
    if (aluno == null || ofertaDisciplina == null || request == null) {
      return null;
    }
    MatriculaDisciplina matricula = new MatriculaDisciplina();
    matricula.setAluno(aluno);
    matricula.setOfertaDisciplina(ofertaDisciplina);
    matricula.setDataMatricula(request.dataMatricula());
    matricula.setStatus(request.status());
    matricula.setNota(request.nota());
    return matricula;
  }

  public MatriculaDisciplinaResponse toResponse(MatriculaDisciplina matricula) {
    if (matricula == null) {
      return null;
    }
    Long alunoId = matricula.getAluno() != null ? matricula.getAluno().getId() : null;
    Long ofertaDisciplinaId = matricula.getOfertaDisciplina() != null ? matricula.getOfertaDisciplina().getId() : null;
    return new MatriculaDisciplinaResponse(
        matricula.getId(),
        alunoId,
        ofertaDisciplinaId,
        matricula.getDataMatricula(),
        matricula.getStatus(),
        matricula.getNota());
  }
}
