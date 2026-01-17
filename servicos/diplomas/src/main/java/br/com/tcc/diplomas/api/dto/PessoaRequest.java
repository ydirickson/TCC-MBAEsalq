package br.com.tcc.diplomas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PessoaRequest(
    @NotBlank(message = "Nome da pessoa e obrigatorio")
    String nome,
    @NotNull(message = "Data de nascimento e obrigatoria")
    LocalDate dataNascimento,
    String nomeSocial
) {
}
