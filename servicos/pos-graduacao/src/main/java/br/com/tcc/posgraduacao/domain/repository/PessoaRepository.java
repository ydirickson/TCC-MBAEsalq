package br.com.tcc.posgraduacao.domain.repository;

import br.com.tcc.posgraduacao.domain.model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
}
