package br.com.tcc.posgraduacao.api.dto;

import br.com.tcc.posgraduacao.domain.model.Pessoa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PessoaRequest(
    @NotBlank String nome,
    @NotNull LocalDate dataNascimento,
    String nomeSocial
) {

  public Pessoa toEntity() {
    return new Pessoa(nome, dataNascimento, nomeSocial);
  }
}
