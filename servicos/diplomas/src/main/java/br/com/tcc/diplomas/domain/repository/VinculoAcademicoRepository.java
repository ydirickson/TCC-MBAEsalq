package br.com.tcc.diplomas.domain.repository;

import br.com.tcc.diplomas.domain.model.VinculoAcademico;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VinculoAcademicoRepository extends JpaRepository<VinculoAcademico, Long> {

  List<VinculoAcademico> findByPessoaId(Long pessoaId);
}
