package br.com.tcc.graduacao.api.mapper;

import br.com.tcc.graduacao.api.dto.CursoGraduacaoRequest;
import br.com.tcc.graduacao.api.dto.CursoGraduacaoResponse;
import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import org.springframework.stereotype.Component;

@Component
public class CursoGraduacaoMapper {

  public CursoGraduacao toEntity(CursoGraduacaoRequest request) {
    if (request == null) {
      return null;
    }
    CursoGraduacao entity = new CursoGraduacao();
    entity.setCodigo(request.codigo());
    entity.setNome(request.nome());
    entity.setCargaHoraria(request.cargaHoraria());
    return entity;
  }

  public CursoGraduacaoResponse toResponse(CursoGraduacao entity) {
    if (entity == null) {
      return null;
    }
    return new CursoGraduacaoResponse(
        entity.getId(),
        entity.getCodigo(),
        entity.getNome(),
        entity.getCargaHoraria());
  }

  public void updateEntityFromRequest(CursoGraduacaoRequest request, CursoGraduacao entity) {
    if (request == null || entity == null) {
      return;
    }
    entity.setCodigo(request.codigo());
    entity.setNome(request.nome());
    entity.setCargaHoraria(request.cargaHoraria());
  }
}
