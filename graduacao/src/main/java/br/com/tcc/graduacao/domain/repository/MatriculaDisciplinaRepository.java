package br.com.tcc.graduacao.domain.repository;

import br.com.tcc.graduacao.domain.model.MatriculaDisciplina;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatriculaDisciplinaRepository extends JpaRepository<MatriculaDisciplina, Long> {
  List<MatriculaDisciplina> findByAlunoId(Long alunoId);
  List<MatriculaDisciplina> findByOfertaDisciplinaId(Long ofertaDisciplinaId);
}
