package br.com.tcc.graduacao.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "documento_identificacao")
public class DocumentoIdentificacao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "pessoa_id", nullable = false, unique = true)
  private Pessoa pessoa;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TipoDocumentoIdentificacao tipo;

  @Column(nullable = false, length = 50)
  private String numero;

  public DocumentoIdentificacao() {
    // JPA
  }

  public DocumentoIdentificacao(Pessoa pessoa, TipoDocumentoIdentificacao tipo, String numero) {
    this.pessoa = pessoa;
    this.tipo = tipo;
    this.numero = numero;
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

  public TipoDocumentoIdentificacao getTipo() {
    return tipo;
  }

  public void setTipo(TipoDocumentoIdentificacao tipo) {
    this.tipo = tipo;
  }

  public String getNumero() {
    return numero;
  }

  public void setNumero(String numero) {
    this.numero = numero;
  }
}
