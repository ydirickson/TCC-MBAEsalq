package br.com.tcc.posgraduacao.domain.repository;

import br.com.tcc.posgraduacao.domain.model.Pessoa;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

  @Modifying
  @Query(nativeQuery = true, value = """
      INSERT INTO pessoa (id, nome, data_nascimento, nome_social)
      VALUES (:id, :nome, :dataNascimento, :nomeSocial)
      ON CONFLICT (id) DO UPDATE SET
        nome = EXCLUDED.nome,
        data_nascimento = EXCLUDED.data_nascimento,
        nome_social = EXCLUDED.nome_social
      """)
  void upsert(
      @Param("id") Long id,
      @Param("nome") String nome,
      @Param("dataNascimento") LocalDate dataNascimento,
      @Param("nomeSocial") String nomeSocial);
}
