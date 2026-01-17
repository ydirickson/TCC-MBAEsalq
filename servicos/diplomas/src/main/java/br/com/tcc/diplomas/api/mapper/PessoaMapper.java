package br.com.tcc.diplomas.api.mapper;

import br.com.tcc.diplomas.api.dto.PessoaRequest;
import br.com.tcc.diplomas.api.dto.PessoaResponse;
import br.com.tcc.diplomas.domain.model.Pessoa;
import org.springframework.stereotype.Component;

@Component
public class PessoaMapper {

  public Pessoa toEntity(PessoaRequest request) {
    if (request == null) {
      return null;
    }
    Pessoa entity = new Pessoa();
    entity.setNome(request.nome());
    entity.setDataNascimento(request.dataNascimento());
    entity.setNomeSocial(request.nomeSocial());
    return entity;
  }

  public PessoaResponse toResponse(Pessoa entity) {
    if (entity == null) {
      return null;
    }
    return new PessoaResponse(
        entity.getId(),
        entity.getNome(),
        entity.getDataNascimento(),
        entity.getNomeSocial());
  }

  public void updateEntityFromRequest(PessoaRequest request, Pessoa entity) {
    if (request == null || entity == null) {
      return;
    }
    entity.setNome(request.nome());
    entity.setDataNascimento(request.dataNascimento());
    entity.setNomeSocial(request.nomeSocial());
  }
}
