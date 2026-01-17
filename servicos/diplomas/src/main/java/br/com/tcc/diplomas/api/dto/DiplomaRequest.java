package br.com.tcc.diplomas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DiplomaRequest(
    @NotNull(message = "Requerimento e obrigatorio")
    Long requerimentoId,
    @NotBlank(message = "Numero de registro e obrigatorio")
    String numeroRegistro,
    @NotNull(message = "Data de emissao e obrigatoria")
    LocalDate dataEmissao
) {
}
