package br.com.tcc.diplomas.domain.repository;

import br.com.tcc.diplomas.domain.model.StatusEmissao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusEmissaoRepository extends JpaRepository<StatusEmissao, Long> {
}
