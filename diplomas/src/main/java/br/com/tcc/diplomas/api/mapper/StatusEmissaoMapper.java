package br.com.tcc.diplomas.api.mapper;

import br.com.tcc.diplomas.api.dto.StatusEmissaoRequest;
import br.com.tcc.diplomas.api.dto.StatusEmissaoResponse;
import br.com.tcc.diplomas.domain.model.RequerimentoDiploma;
import br.com.tcc.diplomas.domain.model.StatusEmissao;
import org.springframework.stereotype.Component;

@Component
public class StatusEmissaoMapper {

  public StatusEmissao toEntity(StatusEmissaoRequest request, RequerimentoDiploma requerimento) {
    if (request == null || requerimento == null) {
      return null;
    }
    StatusEmissao entity = new StatusEmissao();
    entity.setRequerimento(requerimento);
    entity.setStatus(request.status());
    entity.setDataAtualizacao(request.dataAtualizacao());
    return entity;
  }

  public StatusEmissaoResponse toResponse(StatusEmissao entity) {
    if (entity == null) {
      return null;
    }
    return new StatusEmissaoResponse(
        entity.getId(),
        entity.getRequerimento() != null ? entity.getRequerimento().getId() : null,
        entity.getStatus(),
        entity.getDataAtualizacao());
  }

  public void updateEntityFromRequest(StatusEmissaoRequest request, StatusEmissao entity, RequerimentoDiploma requerimento) {
    if (request == null || entity == null || requerimento == null) {
      return;
    }
    entity.setRequerimento(requerimento);
    entity.setStatus(request.status());
    entity.setDataAtualizacao(request.dataAtualizacao());
  }
}
