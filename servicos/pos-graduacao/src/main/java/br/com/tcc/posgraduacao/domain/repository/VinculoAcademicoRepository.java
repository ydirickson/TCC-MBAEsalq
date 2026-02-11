package br.com.tcc.posgraduacao.domain.repository;

import br.com.tcc.posgraduacao.domain.model.Pessoa;
import br.com.tcc.posgraduacao.domain.model.TipoVinculo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VinculoAcademicoRepository extends JpaRepository<VinculoAcademico, Long> {
  Optional<VinculoAcademico> findByPessoaAndCurso_IdAndTipoVinculo(Pessoa pessoa, Long cursoId, TipoVinculo tipoVinculo);
}
