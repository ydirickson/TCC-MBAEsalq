package br.com.tcc.diplomas.api.dto;

import br.com.tcc.diplomas.domain.model.StatusEmissaoTipo;
import java.time.LocalDateTime;

public record StatusEmissaoResponse(
    Long id,
    Long requerimentoId,
    StatusEmissaoTipo status,
    LocalDateTime dataAtualizacao
) {
}
