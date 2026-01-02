package com.tcc.graduacao.api.dto;

import com.tcc.graduacao.domain.model.Pessoa;
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
