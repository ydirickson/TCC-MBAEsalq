package br.com.tcc.graduacao.domain.repository;

import br.com.tcc.graduacao.domain.model.DisciplinaGraduacao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisciplinaRepository extends JpaRepository<DisciplinaGraduacao, Long> {
  List<DisciplinaGraduacao> findByCursoId(Long cursoId);

  Optional<DisciplinaGraduacao> findByIdAndCursoId(Long id, Long cursoId);
}
