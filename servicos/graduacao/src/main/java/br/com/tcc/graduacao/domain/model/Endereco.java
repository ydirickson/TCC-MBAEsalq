package br.com.tcc.graduacao.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "endereco")
public class Endereco {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "pessoa_id", nullable = false)
  private Pessoa pessoa;

  @Column(nullable = false, length = 255)
  private String logradouro;

  @Column(nullable = false, length = 120)
  private String cidade;

  @Column(nullable = false, length = 2)
  private String uf;

  @Column(nullable = false, length = 10)
  private String cep;

  public Endereco() {
    // JPA
  }

  public Endereco(Pessoa pessoa, String logradouro, String cidade, String uf, String cep) {
    this.pessoa = pessoa;
    this.logradouro = logradouro;
    this.cidade = cidade;
    this.uf = uf;
    this.cep = cep;
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

  public String getLogradouro() {
    return logradouro;
  }

  public void setLogradouro(String logradouro) {
    this.logradouro = logradouro;
  }

  public String getCidade() {
    return cidade;
  }

  public void setCidade(String cidade) {
    this.cidade = cidade;
  }

  public String getUf() {
    return uf;
  }

  public void setUf(String uf) {
    this.uf = uf;
  }

  public String getCep() {
    return cep;
  }

  public void setCep(String cep) {
    this.cep = cep;
  }
}
