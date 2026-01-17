package br.com.tcc.diplomas.domain.repository;

import br.com.tcc.diplomas.domain.model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
}
