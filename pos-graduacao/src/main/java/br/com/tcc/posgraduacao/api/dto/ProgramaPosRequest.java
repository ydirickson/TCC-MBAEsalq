package br.com.tcc.posgraduacao.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record ProgramaPosRequest(
    @NotBlank(message = "Codigo do programa e obrigatorio")
    @Pattern(regexp = "^[A-Z]{3,5}$", message = "Codigo do programa deve conter entre 3 e 5 letras maiusculas")
    String codigo,
    @NotBlank(message = "Nome do programa e obrigatorio")
    String nome,
    @NotNull(message = "Carga horaria e obrigatoria")
    @Positive(message = "Carga horaria deve ser um numero positivo")
    Integer cargaHoraria
) {
}
