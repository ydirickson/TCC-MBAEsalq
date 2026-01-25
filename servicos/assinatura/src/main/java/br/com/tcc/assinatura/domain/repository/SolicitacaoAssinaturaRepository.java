package br.com.tcc.assinatura.domain.repository;

import br.com.tcc.assinatura.domain.model.SolicitacaoAssinatura;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoAssinaturaRepository extends JpaRepository<SolicitacaoAssinatura, Long> {

  List<SolicitacaoAssinatura> findAllByDocumentoAssinavelIdOrderByDataSolicitacaoDesc(Long documentoAssinavelId);

  Optional<SolicitacaoAssinatura> findByIdAndDocumentoAssinavelId(Long id, Long documentoAssinavelId);

  boolean existsByIdAndDocumentoAssinavelId(Long id, Long documentoAssinavelId);
}
