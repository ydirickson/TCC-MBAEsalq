package br.com.tcc.posgraduacao.domain.repository;

import br.com.tcc.posgraduacao.domain.model.ProgramaPos;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramaPosRepository extends JpaRepository<ProgramaPos, Long> {

  Optional<ProgramaPos> findByCodigo(String codigo);
}
