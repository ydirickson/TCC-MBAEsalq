package br.com.tcc.posgraduacao.api.dto;

public record EnderecoResponse(
    Long id,
    Long pessoaId,
    String logradouro,
    String cidade,
    String uf,
    String cep
) {
}
