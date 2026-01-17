package br.com.tcc.graduacao.domain.repository;

import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoGraduacaoRepository extends JpaRepository<CursoGraduacao, Long> {

  Optional<CursoGraduacao> findByCodigo(String codigo);
}
