package br.com.tcc.graduacao.api.dto;

import java.time.Instant;
import java.time.LocalDate;

public record PessoaResponse(
    Long id,
    String nome,
    LocalDate dataNascimento,
    String nomeSocial,
    Instant criadoEm,
    Instant replicadoEm
) {
}
