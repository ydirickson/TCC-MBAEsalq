package br.com.tcc.posgraduacao.domain.repository;

import br.com.tcc.posgraduacao.domain.model.Contato;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContatoRepository extends JpaRepository<Contato, Long> {
  List<Contato> findByPessoaId(Long pessoaId);
}
