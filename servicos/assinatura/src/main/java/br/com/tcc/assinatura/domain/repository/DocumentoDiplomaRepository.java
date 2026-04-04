package br.com.tcc.assinatura.domain.repository;

import br.com.tcc.assinatura.domain.model.DocumentoDiploma;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentoDiplomaRepository extends JpaRepository<DocumentoDiploma, Long> {

  @Modifying
  @Query(nativeQuery = true, value = """
      INSERT INTO documento_diploma (id, diploma_id, versao, data_geracao, url_arquivo, hash_documento)
      VALUES (:id, :diplomaId, :versao, :dataGeracao, :urlArquivo, :hashDocumento)
      ON CONFLICT (id) DO UPDATE SET
        diploma_id = EXCLUDED.diploma_id,
        versao = EXCLUDED.versao,
        data_geracao = EXCLUDED.data_geracao,
        url_arquivo = EXCLUDED.url_arquivo,
        hash_documento = EXCLUDED.hash_documento
      """)
  void upsert(@Param("id") Long id, @Param("diplomaId") Long diplomaId,
              @Param("versao") Integer versao, @Param("dataGeracao") LocalDate dataGeracao,
              @Param("urlArquivo") String urlArquivo, @Param("hashDocumento") String hashDocumento);
}
