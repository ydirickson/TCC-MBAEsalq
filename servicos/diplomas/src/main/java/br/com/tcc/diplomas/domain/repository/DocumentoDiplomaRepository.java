package br.com.tcc.diplomas.domain.repository;

import br.com.tcc.diplomas.domain.model.DocumentoDiploma;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoDiplomaRepository extends JpaRepository<DocumentoDiploma, Long> {

  boolean existsByDiplomaIdAndVersao(Long diplomaId, Integer versao);

  List<DocumentoDiploma> findAllByDiplomaIdOrderByVersaoDesc(Long diplomaId);

  Optional<DocumentoDiploma> findByIdAndDiplomaId(Long id, Long diplomaId);

  boolean existsByIdAndDiplomaId(Long id, Long diplomaId);
}
