package br.com.tcc.graduacao.api.mapper;

import br.com.tcc.graduacao.api.dto.CursoRequest;
import br.com.tcc.graduacao.api.dto.CursoResponse;
import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import org.springframework.stereotype.Component;

@Component
public class CursoMapper {

  public CursoGraduacao toEntity(CursoRequest request) {
    if (request == null) {
      return null;
    }
    CursoGraduacao entity = new CursoGraduacao();
    entity.setCodigo(request.codigo());
    entity.setNome(request.nome());
    entity.setCargaHoraria(request.cargaHoraria());
    return entity;
  }

  public CursoResponse toResponse(CursoGraduacao entity) {
    if (entity == null) {
      return null;
    }
    return new CursoResponse(
        entity.getId(),
        entity.getCodigo(),
        entity.getNome(),
        entity.getCargaHoraria());
  }

  public void updateEntityFromRequest(CursoRequest request, CursoGraduacao entity) {
    if (request == null || entity == null) {
      return;
    }
    entity.setCodigo(request.codigo());
    entity.setNome(request.nome());
    entity.setCargaHoraria(request.cargaHoraria());
  }
}
