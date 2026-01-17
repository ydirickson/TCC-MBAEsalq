package br.com.tcc.posgraduacao.api.mapper;

import br.com.tcc.posgraduacao.api.dto.ContatoRequest;
import br.com.tcc.posgraduacao.api.dto.ContatoResponse;
import br.com.tcc.posgraduacao.domain.model.Contato;
import br.com.tcc.posgraduacao.domain.model.Pessoa;
import org.springframework.stereotype.Component;

@Component
public class ContatoMapper {

  public Contato toEntity(Pessoa pessoa, ContatoRequest request) {
    if (pessoa == null || request == null) {
      return null;
    }
    Contato contato = new Contato();
    contato.setPessoa(pessoa);
    contato.setEmail(request.email());
    contato.setTelefone(request.telefone());
    return contato;
  }

  public ContatoResponse toResponse(Contato contato) {
    if (contato == null) {
      return null;
    }
    Long pessoaId = contato.getPessoa() != null ? contato.getPessoa().getId() : null;
    return new ContatoResponse(
        contato.getId(),
        pessoaId,
        contato.getEmail(),
        contato.getTelefone());
  }

  public void updateEntityFromRequest(ContatoRequest request, Contato contato) {
    if (request == null || contato == null) {
      return;
    }
    contato.setEmail(request.email());
    contato.setTelefone(request.telefone());
  }
}
