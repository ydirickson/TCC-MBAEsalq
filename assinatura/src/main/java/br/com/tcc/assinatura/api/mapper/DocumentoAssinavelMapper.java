package br.com.tcc.assinatura.api.mapper;

import br.com.tcc.assinatura.api.dto.DocumentoAssinavelRequest;
import br.com.tcc.assinatura.api.dto.DocumentoAssinavelResponse;
import br.com.tcc.assinatura.domain.model.DocumentoAssinavel;
import br.com.tcc.assinatura.domain.model.DocumentoDiploma;
import org.springframework.stereotype.Component;

@Component
public class DocumentoAssinavelMapper {

  public DocumentoAssinavel toEntity(DocumentoAssinavelRequest request, DocumentoDiploma documentoDiploma) {
    if (request == null || documentoDiploma == null) {
      return null;
    }
    DocumentoAssinavel entity = new DocumentoAssinavel();
    entity.setDocumentoDiploma(documentoDiploma);
    entity.setDescricao(request.descricao());
    entity.setDataCriacao(request.dataCriacao());
    return entity;
  }

  public DocumentoAssinavelResponse toResponse(DocumentoAssinavel entity) {
    if (entity == null) {
      return null;
    }
    return new DocumentoAssinavelResponse(
        entity.getId(),
        entity.getDocumentoDiploma() != null ? entity.getDocumentoDiploma().getId() : null,
        entity.getDescricao(),
        entity.getDataCriacao());
  }

  public void updateEntityFromRequest(DocumentoAssinavelRequest request, DocumentoAssinavel entity,
      DocumentoDiploma documentoDiploma) {
    if (request == null || entity == null || documentoDiploma == null) {
      return;
    }
    entity.setDocumentoDiploma(documentoDiploma);
    entity.setDescricao(request.descricao());
    entity.setDataCriacao(request.dataCriacao());
  }
}
