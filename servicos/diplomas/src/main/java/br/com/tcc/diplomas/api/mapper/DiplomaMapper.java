package br.com.tcc.diplomas.api.mapper;

import br.com.tcc.diplomas.api.dto.DiplomaRequest;
import br.com.tcc.diplomas.api.dto.DiplomaResponse;
import br.com.tcc.diplomas.domain.model.BaseEmissaoDiploma;
import br.com.tcc.diplomas.domain.model.Diploma;
import br.com.tcc.diplomas.domain.model.RequerimentoDiploma;
import org.springframework.stereotype.Component;

@Component
public class DiplomaMapper {

  public Diploma toEntity(DiplomaRequest request, RequerimentoDiploma requerimento, BaseEmissaoDiploma baseEmissao) {
    if (request == null || requerimento == null || baseEmissao == null) {
      return null;
    }
    Diploma entity = new Diploma();
    entity.setRequerimento(requerimento);
    entity.setBaseEmissao(baseEmissao);
    entity.setNumeroRegistro(request.numeroRegistro());
    entity.setDataEmissao(request.dataEmissao());
    return entity;
  }

  public DiplomaResponse toResponse(Diploma entity) {
    if (entity == null) {
      return null;
    }
    return new DiplomaResponse(
        entity.getId(),
        entity.getRequerimento() != null ? entity.getRequerimento().getId() : null,
        entity.getBaseEmissao() != null ? entity.getBaseEmissao().getId() : null,
        entity.getNumeroRegistro(),
        entity.getDataEmissao());
  }

  public void updateEntityFromRequest(DiplomaRequest request, Diploma entity, RequerimentoDiploma requerimento,
      BaseEmissaoDiploma baseEmissao) {
    if (request == null || entity == null || requerimento == null || baseEmissao == null) {
      return;
    }
    entity.setRequerimento(requerimento);
    entity.setBaseEmissao(baseEmissao);
    entity.setNumeroRegistro(request.numeroRegistro());
    entity.setDataEmissao(request.dataEmissao());
  }
}
