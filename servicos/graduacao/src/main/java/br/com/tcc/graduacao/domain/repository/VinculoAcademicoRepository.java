package br.com.tcc.graduacao.domain.repository;

import br.com.tcc.graduacao.domain.model.VinculoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VinculoAcademicoRepository extends JpaRepository<VinculoAcademico, Long> {
}
