package br.com.tcc.diplomas.api.mapper;

import br.com.tcc.diplomas.api.dto.VinculoAcademicoRequest;
import br.com.tcc.diplomas.api.dto.VinculoAcademicoResponse;
import br.com.tcc.diplomas.domain.model.Pessoa;
import br.com.tcc.diplomas.domain.model.VinculoAcademico;
import org.springframework.stereotype.Component;

@Component
public class VinculoAcademicoMapper {

  public VinculoAcademico toEntity(VinculoAcademicoRequest request, Pessoa pessoa) {
    if (request == null || pessoa == null) {
      return null;
    }
    VinculoAcademico entity = new VinculoAcademico();
    entity.setPessoa(pessoa);
    entity.setTipoVinculo(request.tipoVinculo());
    entity.setDataIngresso(request.dataIngresso());
    entity.setDataConclusao(request.dataConclusao());
    entity.setSituacao(request.situacao());
    return entity;
  }

  public VinculoAcademicoResponse toResponse(VinculoAcademico entity) {
    if (entity == null) {
      return null;
    }
    return new VinculoAcademicoResponse(
        entity.getId(),
        entity.getPessoa() != null ? entity.getPessoa().getId() : null,
        entity.getTipoVinculo(),
        entity.getDataIngresso(),
        entity.getDataConclusao(),
        entity.getSituacao());
  }

  public void updateEntityFromRequest(VinculoAcademicoRequest request, VinculoAcademico entity, Pessoa pessoa) {
    if (request == null || entity == null || pessoa == null) {
      return;
    }
    entity.setPessoa(pessoa);
    entity.setTipoVinculo(request.tipoVinculo());
    entity.setDataIngresso(request.dataIngresso());
    entity.setDataConclusao(request.dataConclusao());
    entity.setSituacao(request.situacao());
  }
}
