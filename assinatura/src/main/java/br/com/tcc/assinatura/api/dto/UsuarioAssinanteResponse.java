package br.com.tcc.assinatura.api.dto;

import java.time.LocalDate;

public record UsuarioAssinanteResponse(
    Long id,
    Long pessoaId,
    String email,
    Boolean ativo,
    LocalDate dataCadastro
) {
}
