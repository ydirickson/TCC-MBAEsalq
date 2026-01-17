package br.com.tcc.graduacao.api.mapper;

import br.com.tcc.graduacao.api.dto.PessoaRequest;
import br.com.tcc.graduacao.api.dto.PessoaResponse;
import br.com.tcc.graduacao.domain.model.Pessoa;
import org.springframework.stereotype.Component;

@Component
public class PessoaMapper {

  public Pessoa toEntity(PessoaRequest request) {
    if (request == null) {
      return null;
    }
    Pessoa pessoa = new Pessoa();
    pessoa.setNome(request.nome());
    pessoa.setDataNascimento(request.dataNascimento());
    pessoa.setNomeSocial(request.nomeSocial());
    return pessoa;
  }

  public PessoaResponse toResponse(Pessoa pessoa) {
    if (pessoa == null) {
      return null;
    }
    return new PessoaResponse(
        pessoa.getId(),
        pessoa.getNome(),
        pessoa.getDataNascimento(),
        pessoa.getNomeSocial());
  }

  public void updateEntityFromRequest(PessoaRequest request, Pessoa pessoa) {
    if (request == null || pessoa == null) {
      return;
    }
    pessoa.setNome(request.nome());
    pessoa.setDataNascimento(request.dataNascimento());
    pessoa.setNomeSocial(request.nomeSocial());
  }
}
