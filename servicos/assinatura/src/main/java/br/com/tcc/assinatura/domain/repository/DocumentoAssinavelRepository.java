package br.com.tcc.assinatura.domain.repository;

import br.com.tcc.assinatura.domain.model.DocumentoAssinavel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoAssinavelRepository extends JpaRepository<DocumentoAssinavel, Long> {

  Optional<DocumentoAssinavel> findByDocumentoDiplomaId(Long documentoDiplomaId);
}
