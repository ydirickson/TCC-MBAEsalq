package br.com.tcc.assinatura.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record DocumentoAssinavelRequest(
    @NotNull(message = "Documento de diploma e obrigatorio")
    Long documentoDiplomaId,
    @NotBlank(message = "Descricao e obrigatoria")
    String descricao,
    @NotNull(message = "Data de criacao e obrigatoria")
    LocalDateTime dataCriacao
) {
}
