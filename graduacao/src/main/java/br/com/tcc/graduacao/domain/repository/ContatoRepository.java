package br.com.tcc.graduacao.domain.repository;

import br.com.tcc.graduacao.domain.model.Contato;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContatoRepository extends JpaRepository<Contato, Long> {
  List<Contato> findByPessoaId(Long pessoaId);
}
