package br.com.tcc.graduacao.api.dto;

import br.com.tcc.graduacao.domain.model.TipoDocumentoIdentificacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentoIdentificacaoRequest(
    @NotNull TipoDocumentoIdentificacao tipo,
    @NotBlank String numero
) {
}
