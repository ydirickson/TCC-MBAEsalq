package br.com.tcc.diplomas.domain.repository;

import br.com.tcc.diplomas.domain.model.RequerimentoDiploma;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequerimentoDiplomaRepository extends JpaRepository<RequerimentoDiploma, Long> {

  boolean existsByVinculoAcademicoId(Long vinculoAcademicoId);

  List<RequerimentoDiploma> findByPessoaId(Long pessoaId);
}
