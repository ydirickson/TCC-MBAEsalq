package br.com.tcc.diplomas.api.dto;

import br.com.tcc.diplomas.domain.model.StatusEmissaoTipo;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record StatusEmissaoRequest(
    @NotNull(message = "Requerimento e obrigatorio")
    Long requerimentoId,
    @NotNull(message = "Status e obrigatorio")
    StatusEmissaoTipo status,
    @NotNull(message = "Data de atualizacao e obrigatoria")
    LocalDateTime dataAtualizacao
) {
}
