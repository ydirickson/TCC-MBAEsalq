package br.com.tcc.graduacao.domain.repository;

import br.com.tcc.graduacao.domain.model.AvaliacaoAluno;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvaliacaoAlunoRepository extends JpaRepository<AvaliacaoAluno, Long> {
  List<AvaliacaoAluno> findByMatriculaId(Long matriculaId);
  Optional<AvaliacaoAluno> findByIdAndMatriculaId(Long id, Long matriculaId);
}
