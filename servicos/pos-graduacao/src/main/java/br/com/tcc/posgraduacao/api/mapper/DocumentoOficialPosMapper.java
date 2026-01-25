package br.com.tcc.posgraduacao.api.mapper;

import br.com.tcc.posgraduacao.api.dto.DocumentoOficialPosRequest;
import br.com.tcc.posgraduacao.api.dto.DocumentoOficialPosResponse;
import br.com.tcc.posgraduacao.domain.model.DocumentoOficialPos;
import br.com.tcc.posgraduacao.domain.model.Pessoa;
import org.springframework.stereotype.Component;

@Component
public class DocumentoOficialPosMapper {

  public DocumentoOficialPos toEntity(DocumentoOficialPosRequest request, Pessoa pessoa) {
    if (request == null) {
      return null;
    }
    DocumentoOficialPos entity = new DocumentoOficialPos();
    entity.setPessoa(pessoa);
    entity.setTipoDocumento(request.tipoDocumento());
    entity.setDataEmissao(request.dataEmissao());
    entity.setVersao(request.versao());
    entity.setUrlArquivo(request.urlArquivo());
    entity.setHashDocumento(request.hashDocumento());
    return entity;
  }

  public DocumentoOficialPosResponse toResponse(DocumentoOficialPos entity) {
    if (entity == null) {
      return null;
    }
    return new DocumentoOficialPosResponse(
        entity.getId(),
        entity.getPessoaId(),
        entity.getTipoDocumento(),
        entity.getDataEmissao(),
        entity.getVersao(),
        entity.getUrlArquivo(),
        entity.getHashDocumento());
  }

  public void updateEntityFromRequest(DocumentoOficialPosRequest request, DocumentoOficialPos entity, Pessoa pessoa) {
    if (request == null || entity == null) {
      return;
    }
    entity.setPessoa(pessoa);
    entity.setTipoDocumento(request.tipoDocumento());
    entity.setDataEmissao(request.dataEmissao());
    entity.setVersao(request.versao());
    entity.setUrlArquivo(request.urlArquivo());
    entity.setHashDocumento(request.hashDocumento());
  }
}
