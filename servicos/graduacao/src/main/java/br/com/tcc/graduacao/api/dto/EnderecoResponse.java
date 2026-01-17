package br.com.tcc.graduacao.api.dto;

public record EnderecoResponse(
    Long id,
    Long pessoaId,
    String logradouro,
    String cidade,
    String uf,
    String cep
) {
}
