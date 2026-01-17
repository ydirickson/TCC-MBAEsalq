package br.com.tcc.graduacao.api.dto;

public record ContatoResponse(
    Long id,
    Long pessoaId,
    String email,
    String telefone
) {
}
