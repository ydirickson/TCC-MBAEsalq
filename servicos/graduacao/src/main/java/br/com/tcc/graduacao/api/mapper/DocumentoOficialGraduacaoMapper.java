package br.com.tcc.graduacao.api.mapper;

import br.com.tcc.graduacao.api.dto.DocumentoOficialGraduacaoRequest;
import br.com.tcc.graduacao.api.dto.DocumentoOficialGraduacaoResponse;
import br.com.tcc.graduacao.domain.model.DocumentoOficialGraduacao;
import br.com.tcc.graduacao.domain.model.Pessoa;
import org.springframework.stereotype.Component;

@Component
public class DocumentoOficialGraduacaoMapper {

  public DocumentoOficialGraduacao toEntity(DocumentoOficialGraduacaoRequest request, Pessoa pessoa) {
    if (request == null) {
      return null;
    }
    DocumentoOficialGraduacao entity = new DocumentoOficialGraduacao();
    entity.setPessoa(pessoa);
    entity.setTipoDocumento(request.tipoDocumento());
    entity.setDataEmissao(request.dataEmissao());
    entity.setVersao(request.versao());
    entity.setUrlArquivo(request.urlArquivo());
    entity.setHashDocumento(request.hashDocumento());
    return entity;
  }

  public DocumentoOficialGraduacaoResponse toResponse(DocumentoOficialGraduacao entity) {
    if (entity == null) {
      return null;
    }
    return new DocumentoOficialGraduacaoResponse(
        entity.getId(),
        entity.getPessoaId(),
        entity.getTipoDocumento(),
        entity.getDataEmissao(),
        entity.getVersao(),
        entity.getUrlArquivo(),
        entity.getHashDocumento());
  }

  public void updateEntityFromRequest(DocumentoOficialGraduacaoRequest request, DocumentoOficialGraduacao entity,
      Pessoa pessoa) {
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
