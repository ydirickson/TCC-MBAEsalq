package br.com.tcc.graduacao.domain.repository;

import br.com.tcc.graduacao.domain.model.DocumentoOficialGraduacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoOficialGraduacaoRepository extends JpaRepository<DocumentoOficialGraduacao, Long> {
}
