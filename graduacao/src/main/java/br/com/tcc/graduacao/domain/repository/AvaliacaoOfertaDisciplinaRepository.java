package br.com.tcc.graduacao.domain.repository;

import br.com.tcc.graduacao.domain.model.AvaliacaoOfertaDisciplina;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvaliacaoOfertaDisciplinaRepository extends JpaRepository<AvaliacaoOfertaDisciplina, Long> {
  List<AvaliacaoOfertaDisciplina> findByOfertaDisciplinaId(Long ofertaDisciplinaId);
  Optional<AvaliacaoOfertaDisciplina> findByIdAndOfertaDisciplinaId(Long id, Long ofertaDisciplinaId);
}
