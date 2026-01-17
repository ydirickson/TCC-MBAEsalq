package br.com.tcc.assinatura.api.mapper;

import br.com.tcc.assinatura.api.dto.ManifestoAssinaturaRequest;
import br.com.tcc.assinatura.api.dto.ManifestoAssinaturaResponse;
import br.com.tcc.assinatura.domain.model.ManifestoAssinatura;
import br.com.tcc.assinatura.domain.model.SolicitacaoAssinatura;
import org.springframework.stereotype.Component;

@Component
public class ManifestoAssinaturaMapper {

  public ManifestoAssinatura toEntity(ManifestoAssinaturaRequest request, SolicitacaoAssinatura solicitacao) {
    if (request == null || solicitacao == null) {
      return null;
    }
    ManifestoAssinatura entity = new ManifestoAssinatura();
    entity.setSolicitacao(solicitacao);
    entity.setAuditoria(request.auditoria());
    entity.setCarimboTempo(request.carimboTempo());
    entity.setHashFinal(request.hashFinal());
    return entity;
  }

  public ManifestoAssinaturaResponse toResponse(ManifestoAssinatura entity) {
    if (entity == null) {
      return null;
    }
    return new ManifestoAssinaturaResponse(
        entity.getId(),
        entity.getSolicitacao() != null ? entity.getSolicitacao().getId() : null,
        entity.getAuditoria(),
        entity.getCarimboTempo(),
        entity.getHashFinal());
  }

  public void updateEntityFromRequest(ManifestoAssinaturaRequest request, ManifestoAssinatura entity,
      SolicitacaoAssinatura solicitacao) {
    if (request == null || entity == null || solicitacao == null) {
      return;
    }
    entity.setSolicitacao(solicitacao);
    entity.setAuditoria(request.auditoria());
    entity.setCarimboTempo(request.carimboTempo());
    entity.setHashFinal(request.hashFinal());
  }
}
