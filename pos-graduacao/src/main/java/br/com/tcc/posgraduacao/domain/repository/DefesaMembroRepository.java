package br.com.tcc.posgraduacao.domain.repository;

import br.com.tcc.posgraduacao.domain.model.DefesaMembro;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefesaMembroRepository extends JpaRepository<DefesaMembro, Long> {
  List<DefesaMembro> findByDefesaId(Long defesaId);

  Optional<DefesaMembro> findByDefesaIdAndId(Long defesaId, Long id);
}
