package br.com.tcc.assinatura.api.mapper;

import br.com.tcc.assinatura.api.dto.AssinaturaRequest;
import br.com.tcc.assinatura.api.dto.AssinaturaResponse;
import br.com.tcc.assinatura.domain.model.Assinatura;
import br.com.tcc.assinatura.domain.model.SolicitacaoAssinatura;
import br.com.tcc.assinatura.domain.model.UsuarioAssinante;
import org.springframework.stereotype.Component;

@Component
public class AssinaturaMapper {

  public Assinatura toEntity(AssinaturaRequest request, SolicitacaoAssinatura solicitacao,
      UsuarioAssinante usuarioAssinante) {
    if (request == null || solicitacao == null || usuarioAssinante == null) {
      return null;
    }
    Assinatura entity = new Assinatura();
    entity.setSolicitacao(solicitacao);
    entity.setUsuarioAssinante(usuarioAssinante);
    entity.setStatus(request.status());
    entity.setDataAssinatura(request.dataAssinatura());
    entity.setMotivoRecusa(request.motivoRecusa());
    return entity;
  }

  public AssinaturaResponse toResponse(Assinatura entity) {
    if (entity == null) {
      return null;
    }
    return new AssinaturaResponse(
        entity.getId(),
        entity.getSolicitacao() != null ? entity.getSolicitacao().getId() : null,
        entity.getUsuarioAssinante() != null ? entity.getUsuarioAssinante().getId() : null,
        entity.getStatus(),
        entity.getDataAssinatura(),
        entity.getMotivoRecusa());
  }

  public void updateEntityFromRequest(AssinaturaRequest request, Assinatura entity,
      SolicitacaoAssinatura solicitacao, UsuarioAssinante usuarioAssinante) {
    if (request == null || entity == null || solicitacao == null || usuarioAssinante == null) {
      return;
    }
    entity.setSolicitacao(solicitacao);
    entity.setUsuarioAssinante(usuarioAssinante);
    entity.setStatus(request.status());
    entity.setDataAssinatura(request.dataAssinatura());
    entity.setMotivoRecusa(request.motivoRecusa());
  }
}
