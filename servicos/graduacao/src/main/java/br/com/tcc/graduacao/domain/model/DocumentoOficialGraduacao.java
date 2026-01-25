package br.com.tcc.graduacao.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "documento_oficial_graduacao")
public class DocumentoOficialGraduacao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = true)
  @JoinColumn(name = "pessoa_id")
  private Pessoa pessoa;

  @Column(name = "tipo_documento", nullable = false, length = 50)
  private String tipoDocumento;

  @Column(name = "data_emissao", nullable = false)
  private LocalDate dataEmissao;

  @Column(nullable = false)
  private Integer versao;

  @Column(name = "url_arquivo", length = 255)
  private String urlArquivo;

  @Column(name = "hash_documento", length = 255)
  private String hashDocumento;

  public DocumentoOficialGraduacao() {
    // JPA
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Pessoa getPessoa() {
    return pessoa;
  }

  public void setPessoa(Pessoa pessoa) {
    this.pessoa = pessoa;
  }

  public String getTipoDocumento() {
    return tipoDocumento;
  }

  public void setTipoDocumento(String tipoDocumento) {
    this.tipoDocumento = tipoDocumento;
  }

  public LocalDate getDataEmissao() {
    return dataEmissao;
  }

  public void setDataEmissao(LocalDate dataEmissao) {
    this.dataEmissao = dataEmissao;
  }

  public Integer getVersao() {
    return versao;
  }

  public void setVersao(Integer versao) {
    this.versao = versao;
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

  public Long getPessoaId() {
    Pessoa pessoa = getPessoa();
    return pessoa != null ? pessoa.getId() : null;
  }
}
