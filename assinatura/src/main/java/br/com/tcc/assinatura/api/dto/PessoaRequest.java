package br.com.tcc.assinatura.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PessoaRequest(
    @NotBlank(message = "Nome e obrigatorio")
    String nome,
    @NotNull(message = "Data de nascimento e obrigatoria")
    LocalDate dataNascimento,
    String nomeSocial
) {
}
