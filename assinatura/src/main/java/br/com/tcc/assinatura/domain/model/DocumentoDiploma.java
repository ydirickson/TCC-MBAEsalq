package br.com.tcc.assinatura.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "documento_diploma")
public class DocumentoDiploma {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "diploma_id", nullable = false)
  private Long diplomaId;

  @Column(nullable = false)
  private Integer versao;

  @Column(name = "data_geracao", nullable = false)
  private LocalDate dataGeracao;

  @Column(name = "url_arquivo", length = 255)
  private String urlArquivo;

  @Column(name = "hash_documento", length = 255)
  private String hashDocumento;

  public DocumentoDiploma() {
    // JPA
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getDiplomaId() {
    return diplomaId;
  }

  public void setDiplomaId(Long diplomaId) {
    this.diplomaId = diplomaId;
  }

  public Integer getVersao() {
    return versao;
  }

  public void setVersao(Integer versao) {
    this.versao = versao;
  }

  public LocalDate getDataGeracao() {
    return dataGeracao;
  }

  public void setDataGeracao(LocalDate dataGeracao) {
    this.dataGeracao = dataGeracao;
  }

  public String getUrlArquivo() {
    return urlArquivo;
  }

  public void setUrlArquivo(String urlArquivo) {
    this.urlArquivo = urlArquivo;
  }

  public String getHashDocumento() {
    return hashDocumento;
  }

  public void setHashDocumento(String hashDocumento) {
    this.hashDocumento = hashDocumento;
  }
}
