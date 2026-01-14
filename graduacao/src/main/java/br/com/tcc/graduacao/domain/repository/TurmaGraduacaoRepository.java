package br.com.tcc.graduacao.domain.repository;

import br.com.tcc.graduacao.domain.model.TurmaGraduacao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TurmaGraduacaoRepository extends JpaRepository<TurmaGraduacao, String> {
  List<TurmaGraduacao> findByCursoId(Long cursoId);

  Optional<TurmaGraduacao> findByIdAndCursoId(String id, Long cursoId);
}
