package br.com.tcc.assinatura.api.mapper;

import br.com.tcc.assinatura.api.dto.SolicitacaoAssinaturaRequest;
import br.com.tcc.assinatura.api.dto.SolicitacaoAssinaturaResponse;
import br.com.tcc.assinatura.domain.model.DocumentoAssinavel;
import br.com.tcc.assinatura.domain.model.SolicitacaoAssinatura;
import org.springframework.stereotype.Component;

@Component
public class SolicitacaoAssinaturaMapper {

  public SolicitacaoAssinatura toEntity(SolicitacaoAssinaturaRequest request, DocumentoAssinavel documentoAssinavel) {
    if (request == null || documentoAssinavel == null) {
      return null;
    }
    SolicitacaoAssinatura entity = new SolicitacaoAssinatura();
    entity.setDocumentoAssinavel(documentoAssinavel);
    entity.setStatus(request.status());
    entity.setDataSolicitacao(request.dataSolicitacao());
    entity.setDataConclusao(request.dataConclusao());
    return entity;
  }

  public SolicitacaoAssinaturaResponse toResponse(SolicitacaoAssinatura entity) {
    if (entity == null) {
      return null;
    }
    return new SolicitacaoAssinaturaResponse(
        entity.getId(),
        entity.getDocumentoAssinavel() != null ? entity.getDocumentoAssinavel().getId() : null,
        entity.getStatus(),
        entity.getDataSolicitacao(),
        entity.getDataConclusao());
  }

  public void updateEntityFromRequest(SolicitacaoAssinaturaRequest request, SolicitacaoAssinatura entity,
      DocumentoAssinavel documentoAssinavel) {
    if (request == null || entity == null || documentoAssinavel == null) {
      return;
    }
    entity.setDocumentoAssinavel(documentoAssinavel);
    entity.setStatus(request.status());
    entity.setDataSolicitacao(request.dataSolicitacao());
    entity.setDataConclusao(request.dataConclusao());
  }
}
