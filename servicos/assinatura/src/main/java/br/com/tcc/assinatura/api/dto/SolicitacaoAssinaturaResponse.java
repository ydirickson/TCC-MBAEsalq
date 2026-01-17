package br.com.tcc.assinatura.api.dto;

import br.com.tcc.assinatura.domain.model.StatusSolicitacaoAssinatura;
import java.time.LocalDateTime;

public record SolicitacaoAssinaturaResponse(
    Long id,
    Long documentoAssinavelId,
    StatusSolicitacaoAssinatura status,
    LocalDateTime dataSolicitacao,
    LocalDateTime dataConclusao
) {
}
