package br.com.tcc.posgraduacao.domain.repository;

import br.com.tcc.posgraduacao.domain.model.DisciplinaPos;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisciplinaPosRepository extends JpaRepository<DisciplinaPos, Long> {
  List<DisciplinaPos> findByProgramaId(Long programaId);

  Optional<DisciplinaPos> findByIdAndProgramaId(Long id, Long programaId);
}
