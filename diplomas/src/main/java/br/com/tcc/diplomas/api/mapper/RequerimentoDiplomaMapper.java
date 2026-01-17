package br.com.tcc.diplomas.api.mapper;

import br.com.tcc.diplomas.api.dto.RequerimentoDiplomaRequest;
import br.com.tcc.diplomas.api.dto.RequerimentoDiplomaResponse;
import br.com.tcc.diplomas.domain.model.RequerimentoDiploma;
import br.com.tcc.diplomas.domain.model.Pessoa;
import br.com.tcc.diplomas.domain.model.VinculoAcademico;
import org.springframework.stereotype.Component;

@Component
public class RequerimentoDiplomaMapper {

  public RequerimentoDiploma toEntity(RequerimentoDiplomaRequest request, Pessoa pessoa, VinculoAcademico vinculo) {
    if (request == null || pessoa == null || vinculo == null) {
      return null;
    }
    RequerimentoDiploma entity = new RequerimentoDiploma();
    entity.setPessoa(pessoa);
    entity.setVinculoAcademico(vinculo);
    entity.setDataSolicitacao(request.dataSolicitacao());
    return entity;
  }

  public RequerimentoDiplomaResponse toResponse(RequerimentoDiploma entity) {
    if (entity == null) {
      return null;
    }
    return new RequerimentoDiplomaResponse(
        entity.getId(),
        entity.getPessoa() != null ? entity.getPessoa().getId() : null,
        entity.getVinculoAcademico() != null ? entity.getVinculoAcademico().getId() : null,
        entity.getDataSolicitacao(),
        entity.getBaseEmissao() != null ? entity.getBaseEmissao().getId() : null,
        entity.getStatusEmissao() != null ? entity.getStatusEmissao().getId() : null,
        entity.getDiploma() != null ? entity.getDiploma().getId() : null);
  }

  public void updateEntityFromRequest(RequerimentoDiplomaRequest request, RequerimentoDiploma entity, Pessoa pessoa,
      VinculoAcademico vinculo) {
    if (request == null || entity == null || pessoa == null || vinculo == null) {
      return;
    }
    entity.setPessoa(pessoa);
    entity.setVinculoAcademico(vinculo);
    entity.setDataSolicitacao(request.dataSolicitacao());
  }
}
