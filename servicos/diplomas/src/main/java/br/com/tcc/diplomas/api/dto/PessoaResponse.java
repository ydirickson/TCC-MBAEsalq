package br.com.tcc.diplomas.api.dto;

import java.time.LocalDate;

public record PessoaResponse(
    Long id,
    String nome,
    LocalDate dataNascimento,
    String nomeSocial
) {
}
