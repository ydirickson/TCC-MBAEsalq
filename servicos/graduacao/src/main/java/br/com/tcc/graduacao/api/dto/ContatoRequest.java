package br.com.tcc.graduacao.api.dto;

public record ContatoRequest(
    String email,
    String telefone
) {
}
