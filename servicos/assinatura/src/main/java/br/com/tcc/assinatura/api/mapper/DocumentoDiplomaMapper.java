package br.com.tcc.assinatura.api.mapper;

import br.com.tcc.assinatura.api.dto.DocumentoDiplomaRequest;
import br.com.tcc.assinatura.api.dto.DocumentoDiplomaResponse;
import br.com.tcc.assinatura.domain.model.DocumentoDiploma;
import org.springframework.stereotype.Component;

@Component
public class DocumentoDiplomaMapper {

  public DocumentoDiploma toEntity(DocumentoDiplomaRequest request) {
    if (request == null) {
      return null;
    }
    DocumentoDiploma entity = new DocumentoDiploma();
    entity.setDiplomaId(request.diplomaId());
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
        entity.getDiplomaId(),
        entity.getVersao(),
        entity.getDataGeracao(),
        entity.getUrlArquivo(),
        entity.getHashDocumento());
  }

  public void updateEntityFromRequest(DocumentoDiplomaRequest request, DocumentoDiploma entity) {
    if (request == null || entity == null) {
      return;
    }
    entity.setDiplomaId(request.diplomaId());
    entity.setVersao(request.versao());
    entity.setDataGeracao(request.dataGeracao());
    entity.setUrlArquivo(request.urlArquivo());
    entity.setHashDocumento(request.hashDocumento());
  }
}
