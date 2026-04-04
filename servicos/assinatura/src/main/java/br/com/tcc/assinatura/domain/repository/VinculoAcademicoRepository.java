package br.com.tcc.assinatura.domain.repository;

import br.com.tcc.assinatura.domain.model.VinculoAcademico;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VinculoAcademicoRepository extends JpaRepository<VinculoAcademico, Long> {

  List<VinculoAcademico> findByPessoaId(Long pessoaId);

  @Modifying
  @Query(nativeQuery = true, value = """
      INSERT INTO vinculo_academico (id, pessoa_id, curso_id, curso_codigo, curso_nome, curso_tipo, tipo_vinculo, data_ingresso, data_conclusao, situacao)
      VALUES (:id, :pessoaId, :cursoId, :cursoCodigo, :cursoNome, :cursoTipo, :tipoVinculo, :dataIngresso, :dataConclusao, :situacao)
      ON CONFLICT (id) DO UPDATE SET
        pessoa_id = EXCLUDED.pessoa_id,
        curso_id = EXCLUDED.curso_id,
        curso_codigo = EXCLUDED.curso_codigo,
        curso_nome = EXCLUDED.curso_nome,
        curso_tipo = EXCLUDED.curso_tipo,
        tipo_vinculo = EXCLUDED.tipo_vinculo,
        data_ingresso = EXCLUDED.data_ingresso,
        data_conclusao = EXCLUDED.data_conclusao,
        situacao = EXCLUDED.situacao
      """)
  void upsert(@Param("id") Long id, @Param("pessoaId") Long pessoaId,
              @Param("cursoId") Long cursoId, @Param("cursoCodigo") String cursoCodigo,
              @Param("cursoNome") String cursoNome, @Param("cursoTipo") String cursoTipo,
              @Param("tipoVinculo") String tipoVinculo, @Param("dataIngresso") LocalDate dataIngresso,
              @Param("dataConclusao") LocalDate dataConclusao, @Param("situacao") String situacao);
}
