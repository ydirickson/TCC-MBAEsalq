package br.com.tcc.diplomas.api.mapper;

import br.com.tcc.diplomas.api.dto.DocumentoDiplomaRequest;
import br.com.tcc.diplomas.api.dto.DocumentoDiplomaResponse;
import br.com.tcc.diplomas.domain.model.Diploma;
import br.com.tcc.diplomas.domain.model.DocumentoDiploma;
import org.springframework.stereotype.Component;

@Component
public class DocumentoDiplomaMapper {

  public DocumentoDiploma toEntity(DocumentoDiplomaRequest request, Diploma diploma) {
    if (request == null || diploma == null) {
      return null;
    }
    DocumentoDiploma entity = new DocumentoDiploma();
    entity.setDiploma(diploma);
    entity.setVersao(request.versao());
    entity.setDataGeracao(request.dataGeracao());
    entity.setUrlArquivo(request.urlArquivo());
    entity.setHashDocumento(request.hashDocumento());
    return entity;
  }

  public DocumentoDiplomaResponse toResponse(DocumentoDiploma entity) {
    if (entity == null) {
      return null;
    }
    return new DocumentoDiplomaResponse(
        entity.getId(),
        entity.getDiploma() != null ? entity.getDiploma().getId() : null,
        entity.getVersao(),
        entity.getDataGeracao(),
        entity.getUrlArquivo(),
        entity.getHashDocumento());
  }

  public void updateEntityFromRequest(DocumentoDiplomaRequest request, DocumentoDiploma entity, Diploma diploma) {
    if (request == null || entity == null || diploma == null) {
      return;
    }
    entity.setDiploma(diploma);
    entity.setVersao(request.versao());
    entity.setDataGeracao(request.dataGeracao());
    entity.setUrlArquivo(request.urlArquivo());
    entity.setHashDocumento(request.hashDocumento());
  }
}
