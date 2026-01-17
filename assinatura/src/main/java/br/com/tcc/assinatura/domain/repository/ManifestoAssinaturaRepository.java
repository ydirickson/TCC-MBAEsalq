package br.com.tcc.assinatura.domain.repository;

import br.com.tcc.assinatura.domain.model.ManifestoAssinatura;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManifestoAssinaturaRepository extends JpaRepository<ManifestoAssinatura, Long> {
}
