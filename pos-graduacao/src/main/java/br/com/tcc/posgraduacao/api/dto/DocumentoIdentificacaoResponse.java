package br.com.tcc.posgraduacao.api.dto;

import br.com.tcc.posgraduacao.domain.model.TipoDocumentoIdentificacao;

public record DocumentoIdentificacaoResponse(
    Long id,
    Long pessoaId,
    TipoDocumentoIdentificacao tipo,
    String numero
) {
}
