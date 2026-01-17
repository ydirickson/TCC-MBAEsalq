package br.com.tcc.graduacao.domain.repository;

import br.com.tcc.graduacao.domain.model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
}
