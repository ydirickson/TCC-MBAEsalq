package br.com.tcc.assinatura.domain.repository;

import br.com.tcc.assinatura.domain.model.VinculoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VinculoAcademicoRepository extends JpaRepository<VinculoAcademico, Long> {
}
