package br.com.tcc.posgraduacao.api.dto;

public record ContatoResponse(
    Long id,
    Long pessoaId,
    String email,
    String telefone
) {
}
