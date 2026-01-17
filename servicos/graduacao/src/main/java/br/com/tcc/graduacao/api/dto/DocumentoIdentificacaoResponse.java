package br.com.tcc.graduacao.api.dto;

import br.com.tcc.graduacao.domain.model.TipoDocumentoIdentificacao;

public record DocumentoIdentificacaoResponse(
    Long id,
    Long pessoaId,
    TipoDocumentoIdentificacao tipo,
    String numero
) {
}
