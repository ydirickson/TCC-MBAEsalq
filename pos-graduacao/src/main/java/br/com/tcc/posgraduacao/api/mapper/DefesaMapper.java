package br.com.tcc.posgraduacao.api.mapper;

import br.com.tcc.posgraduacao.api.dto.DefesaResponse;
import br.com.tcc.posgraduacao.domain.model.Defesa;
import org.springframework.stereotype.Component;

@Component
public class DefesaMapper {

  public DefesaResponse toResponse(Defesa defesa) {
    if (defesa == null) {
      return null;
    }
    Long alunoId = defesa.getAluno() != null ? defesa.getAluno().getId() : null;
    return new DefesaResponse(
        defesa.getId(),
        alunoId,
        defesa.getTipo(),
        defesa.getNota());
  }
}
