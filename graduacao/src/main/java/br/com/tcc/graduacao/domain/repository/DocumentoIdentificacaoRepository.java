package br.com.tcc.graduacao.domain.repository;

import br.com.tcc.graduacao.domain.model.DocumentoIdentificacao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoIdentificacaoRepository extends JpaRepository<DocumentoIdentificacao, Long> {
  Optional<DocumentoIdentificacao> findByPessoaId(Long pessoaId);
}
