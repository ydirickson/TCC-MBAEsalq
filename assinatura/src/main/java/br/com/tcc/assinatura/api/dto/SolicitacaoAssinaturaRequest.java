package br.com.tcc.assinatura.api.dto;

import br.com.tcc.assinatura.domain.model.StatusSolicitacaoAssinatura;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record SolicitacaoAssinaturaRequest(
    @NotNull(message = "Documento assinavel e obrigatorio")
    Long documentoAssinavelId,
    @NotNull(message = "Status e obrigatorio")
    StatusSolicitacaoAssinatura status,
    @NotNull(message = "Data de solicitacao e obrigatoria")
    LocalDateTime dataSolicitacao,
    LocalDateTime dataConclusao
) {
}
