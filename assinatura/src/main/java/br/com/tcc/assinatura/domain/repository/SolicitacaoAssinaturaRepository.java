package br.com.tcc.assinatura.domain.repository;

import br.com.tcc.assinatura.domain.model.SolicitacaoAssinatura;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoAssinaturaRepository extends JpaRepository<SolicitacaoAssinatura, Long> {
}
