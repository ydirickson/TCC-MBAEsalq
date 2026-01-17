package br.com.tcc.assinatura.domain.repository;

import br.com.tcc.assinatura.domain.model.Assinatura;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssinaturaRepository extends JpaRepository<Assinatura, Long> {
}
