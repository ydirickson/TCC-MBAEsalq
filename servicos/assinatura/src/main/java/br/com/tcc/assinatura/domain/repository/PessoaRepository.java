package br.com.tcc.assinatura.domain.repository;

import br.com.tcc.assinatura.domain.model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
}
