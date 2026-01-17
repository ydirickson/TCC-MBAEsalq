package br.com.tcc.posgraduacao.api.dto;

import br.com.tcc.posgraduacao.domain.model.TipoDocumentoIdentificacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentoIdentificacaoRequest(
    @NotNull TipoDocumentoIdentificacao tipo,
    @NotBlank String numero
) {
}
