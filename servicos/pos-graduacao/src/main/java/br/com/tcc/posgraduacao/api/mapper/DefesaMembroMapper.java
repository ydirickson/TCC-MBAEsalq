package br.com.tcc.posgraduacao.api.mapper;

import br.com.tcc.posgraduacao.api.dto.DefesaMembroResponse;
import br.com.tcc.posgraduacao.domain.model.DefesaMembro;
import org.springframework.stereotype.Component;

@Component
public class DefesaMembroMapper {

  public DefesaMembroResponse toResponse(DefesaMembro membro) {
    if (membro == null) {
      return null;
    }
    Long professorId = membro.getProfessor() != null ? membro.getProfessor().getId() : null;
    return new DefesaMembroResponse(
        membro.getId(),
        professorId,
        membro.getNota(),
        membro.isPresidente());
  }
}
