package br.com.tcc.posgraduacao.domain.repository;

import br.com.tcc.posgraduacao.domain.model.Defesa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefesaRepository extends JpaRepository<Defesa, Long> {
  List<Defesa> findByAlunoId(Long alunoId);
}
