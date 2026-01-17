package br.com.tcc.posgraduacao.api.dto;

public record ContatoRequest(
    String email,
    String telefone
) {
}
