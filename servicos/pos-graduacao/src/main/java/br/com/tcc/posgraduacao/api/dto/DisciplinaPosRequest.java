package br.com.tcc.posgraduacao.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record DisciplinaPosRequest(
    @NotBlank(message = "Codigo da disciplina e obrigatorio")
    @Pattern(regexp = "^[A-Z]{3}\\d{4}$", message = "Codigo deve conter 3 letras maiusculas seguidas de 4 digitos")
    String codigo,
    @NotBlank(message = "Nome da disciplina e obrigatorio")
    String nome,
    @NotNull(message = "Carga horaria e obrigatoria")
    @Positive(message = "Carga horaria deve ser um numero positivo")
    Integer cargaHoraria
) {
}
