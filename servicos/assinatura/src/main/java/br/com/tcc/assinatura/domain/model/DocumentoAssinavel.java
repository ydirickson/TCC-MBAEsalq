package br.com.tcc.assinatura.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "documento_assinavel")
public class DocumentoAssinavel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "documento_diploma_id", nullable = false)
  private DocumentoDiploma documentoDiploma;

  @Column(nullable = false, length = 255)
  private String descricao;

  @Column(name = "data_criacao", nullable = false)
  private LocalDateTime dataCriacao;

  public DocumentoAssinavel() {
    // JPA
  }

  public DocumentoAssinavel(DocumentoDiploma documentoDiploma, String descricao, LocalDateTime dataCriacao) {
    this.documentoDiploma = documentoDiploma;
    this.descricao = descricao;
    this.dataCriacao = dataCriacao;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public DocumentoDiploma getDocumentoDiploma() {
    return documentoDiploma;
  }

  public void setDocumentoDiploma(DocumentoDiploma documentoDiploma) {
    this.documentoDiploma = documentoDiploma;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }

  public LocalDateTime getDataCriacao() {
    return dataCriacao;
  }

  public void setDataCriacao(LocalDateTime dataCriacao) {
    this.dataCriacao = dataCriacao;
  }
}
