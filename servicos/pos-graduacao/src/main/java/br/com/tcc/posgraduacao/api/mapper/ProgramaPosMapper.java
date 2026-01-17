package br.com.tcc.posgraduacao.api.mapper;

import br.com.tcc.posgraduacao.api.dto.ProgramaPosRequest;
import br.com.tcc.posgraduacao.api.dto.ProgramaPosResponse;
import br.com.tcc.posgraduacao.domain.model.ProgramaPos;
import org.springframework.stereotype.Component;

@Component
public class ProgramaPosMapper {

  public ProgramaPos toEntity(ProgramaPosRequest request) {
    if (request == null) {
      return null;
    }
    ProgramaPos entity = new ProgramaPos();
    entity.setCodigo(request.codigo());
    entity.setNome(request.nome());
    entity.setCargaHoraria(request.cargaHoraria());
    return entity;
  }

  public ProgramaPosResponse toResponse(ProgramaPos entity) {
    if (entity == null) {
      return null;
    }
    return new ProgramaPosResponse(
        entity.getId(),
        entity.getCodigo(),
        entity.getNome(),
        entity.getCargaHoraria());
  }

  public void updateEntityFromRequest(ProgramaPosRequest request, ProgramaPos entity) {
    if (request == null || entity == null) {
      return;
    }
    entity.setCodigo(request.codigo());
    entity.setNome(request.nome());
    entity.setCargaHoraria(request.cargaHoraria());
  }
}
