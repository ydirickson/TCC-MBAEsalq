package com.tcc.graduacao.api.mapper;

import com.tcc.graduacao.api.dto.AlunoGraduacaoResponse;
import com.tcc.graduacao.domain.model.AlunoGraduacao;
import com.tcc.graduacao.domain.model.CursoGraduacao;
import org.springframework.stereotype.Component;

@Component
public class AlunoGraduacaoMapper {

  public AlunoGraduacaoResponse toResponse(AlunoGraduacao entity) {
    if (entity == null) {
      return null;
    }
    CursoGraduacao curso = entity.getCurso();
    Long cursoId = curso != null ? curso.getId() : null;
    String cursoCodigo = curso != null ? curso.getCodigo() : null;
    String cursoNome = curso != null ? curso.getNome() : null;

    return new AlunoGraduacaoResponse(
        entity.getId(),
        entity.getPessoaId(),
        cursoId,
        cursoCodigo,
        cursoNome,
        entity.getDataIngresso(),
        entity.getStatus());
  }
}
