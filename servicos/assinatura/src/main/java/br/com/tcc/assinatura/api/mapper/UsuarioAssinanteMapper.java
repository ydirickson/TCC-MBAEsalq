package br.com.tcc.assinatura.api.mapper;

import br.com.tcc.assinatura.api.dto.UsuarioAssinanteRequest;
import br.com.tcc.assinatura.api.dto.UsuarioAssinanteResponse;
import br.com.tcc.assinatura.domain.model.Pessoa;
import br.com.tcc.assinatura.domain.model.UsuarioAssinante;
import org.springframework.stereotype.Component;

@Component
public class UsuarioAssinanteMapper {

  public UsuarioAssinante toEntity(UsuarioAssinanteRequest request, Pessoa pessoa) {
    if (request == null || pessoa == null) {
      return null;
    }
    UsuarioAssinante entity = new UsuarioAssinante();
    entity.setPessoa(pessoa);
    entity.setEmail(request.email());
    entity.setAtivo(request.ativo());
    entity.setDataCadastro(request.dataCadastro());
    return entity;
  }

  public UsuarioAssinanteResponse toResponse(UsuarioAssinante entity) {
    if (entity == null) {
      return null;
    }
    return new UsuarioAssinanteResponse(
        entity.getId(),
        entity.getPessoa() != null ? entity.getPessoa().getId() : null,
        entity.getEmail(),
        entity.getAtivo(),
        entity.getDataCadastro());
  }

  public void updateEntityFromRequest(UsuarioAssinanteRequest request, UsuarioAssinante entity, Pessoa pessoa) {
    if (request == null || entity == null || pessoa == null) {
      return;
    }
    entity.setPessoa(pessoa);
    entity.setEmail(request.email());
    entity.setAtivo(request.ativo());
    entity.setDataCadastro(request.dataCadastro());
  }
}
