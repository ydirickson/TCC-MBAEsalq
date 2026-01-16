package br.com.tcc.graduacao.api.mapper;

import br.com.tcc.graduacao.api.dto.AvaliacaoAlunoRequest;
import br.com.tcc.graduacao.api.dto.AvaliacaoAlunoResponse;
import br.com.tcc.graduacao.domain.model.AvaliacaoAluno;
import br.com.tcc.graduacao.domain.model.AvaliacaoOfertaDisciplina;
import br.com.tcc.graduacao.domain.model.MatriculaDisciplina;
import org.springframework.stereotype.Component;

@Component
public class AvaliacaoAlunoMapper {

  public AvaliacaoAluno toEntity(
      MatriculaDisciplina matricula,
      AvaliacaoOfertaDisciplina avaliacao,
      AvaliacaoAlunoRequest request) {
    if (matricula == null || avaliacao == null || request == null) {
      return null;
    }
    AvaliacaoAluno avaliacaoAluno = new AvaliacaoAluno();
    avaliacaoAluno.setMatricula(matricula);
    avaliacaoAluno.setAvaliacao(avaliacao);
    avaliacaoAluno.setNota(request.nota());
    return avaliacaoAluno;
  }

  public AvaliacaoAlunoResponse toResponse(AvaliacaoAluno avaliacaoAluno) {
    if (avaliacaoAluno == null) {
      return null;
    }
    Long matriculaId = avaliacaoAluno.getMatricula() != null ? avaliacaoAluno.getMatricula().getId() : null;
    Long avaliacaoId = avaliacaoAluno.getAvaliacao() != null ? avaliacaoAluno.getAvaliacao().getId() : null;
    return new AvaliacaoAlunoResponse(
        avaliacaoAluno.getId(),
        matriculaId,
        avaliacaoId,
        avaliacaoAluno.getNota());
  }

  public void updateEntityFromRequest(
      AvaliacaoAlunoRequest request,
      MatriculaDisciplina matricula,
      AvaliacaoOfertaDisciplina avaliacao,
      AvaliacaoAluno avaliacaoAluno) {
    if (request == null || matricula == null || avaliacao == null || avaliacaoAluno == null) {
      return;
    }
    avaliacaoAluno.setMatricula(matricula);
    avaliacaoAluno.setAvaliacao(avaliacao);
    avaliacaoAluno.setNota(request.nota());
  }
}
