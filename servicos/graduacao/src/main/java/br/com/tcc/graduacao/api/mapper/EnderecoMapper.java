package br.com.tcc.graduacao.api.mapper;

import br.com.tcc.graduacao.api.dto.EnderecoRequest;
import br.com.tcc.graduacao.api.dto.EnderecoResponse;
import br.com.tcc.graduacao.domain.model.Endereco;
import br.com.tcc.graduacao.domain.model.Pessoa;
import org.springframework.stereotype.Component;

@Component
public class EnderecoMapper {

  public Endereco toEntity(Pessoa pessoa, EnderecoRequest request) {
    if (pessoa == null || request == null) {
      return null;
    }
    Endereco endereco = new Endereco();
    endereco.setPessoa(pessoa);
    endereco.setLogradouro(request.logradouro());
    endereco.setCidade(request.cidade());
    endereco.setUf(request.uf());
    endereco.setCep(request.cep());
    return endereco;
  }

  public EnderecoResponse toResponse(Endereco endereco) {
    if (endereco == null) {
      return null;
    }
    Long pessoaId = endereco.getPessoa() != null ? endereco.getPessoa().getId() : null;
    return new EnderecoResponse(
        endereco.getId(),
        pessoaId,
        endereco.getLogradouro(),
        endereco.getCidade(),
        endereco.getUf(),
        endereco.getCep());
  }

  public void updateEntityFromRequest(EnderecoRequest request, Endereco endereco) {
    if (request == null || endereco == null) {
      return;
    }
    endereco.setLogradouro(request.logradouro());
    endereco.setCidade(request.cidade());
    endereco.setUf(request.uf());
    endereco.setCep(request.cep());
  }
}
