package br.com.tcc.diplomas.domain.repository;

import br.com.tcc.diplomas.domain.model.StatusEmissao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StatusEmissaoRepository extends JpaRepository<StatusEmissao, Long> {

  @Query("SELECT se FROM StatusEmissao se WHERE se.requerimento.id = (SELECT doc.diploma.requerimento.id FROM DocumentoDiploma doc WHERE doc.id = :documentoDiplomaId)")
  Optional<StatusEmissao> findByDocumentoDiplomaId(@Param("documentoDiplomaId") Long documentoDiplomaId);
}
