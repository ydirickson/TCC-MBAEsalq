package br.com.tcc.assinatura.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UsuarioAssinanteRequest(
    @NotNull(message = "Pessoa e obrigatoria")
    Long pessoaId,
    @NotBlank(message = "Email e obrigatorio")
    String email,
    @NotNull(message = "Status ativo e obrigatorio")
    Boolean ativo,
    @NotNull(message = "Data de cadastro e obrigatoria")
    LocalDate dataCadastro
) {
}
