package br.com.tcc.posgraduacao.api.dto;

import jakarta.validation.constraints.NotBlank;

public record EnderecoRequest(
    @NotBlank String logradouro,
    @NotBlank String cidade,
    @NotBlank String uf,
    @NotBlank String cep
) {
}
