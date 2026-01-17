package br.com.tcc.posgraduacao.domain.repository;

import br.com.tcc.posgraduacao.domain.model.OfertaDisciplina;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfertaDisciplinaRepository extends JpaRepository<OfertaDisciplina, Long> {
  List<OfertaDisciplina> findByDisciplinaId(Long disciplinaId);
  List<OfertaDisciplina> findByProfessorId(Long professorId);
}
