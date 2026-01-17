package br.com.tcc.graduacao.domain.repository;

import br.com.tcc.graduacao.domain.model.Endereco;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {
  List<Endereco> findByPessoaId(Long pessoaId);
}
