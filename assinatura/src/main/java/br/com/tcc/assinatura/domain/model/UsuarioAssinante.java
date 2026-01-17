package br.com.tcc.assinatura.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "usuario_assinante")
public class UsuarioAssinante {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "pessoa_id", nullable = false, unique = true)
  private Pessoa pessoa;

  @Column(nullable = false, length = 255)
  private String email;

  @Column(nullable = false)
  private Boolean ativo;

  @Column(name = "data_cadastro", nullable = false)
  private LocalDate dataCadastro;

  public UsuarioAssinante() {
    // JPA
  }

  public UsuarioAssinante(Pessoa pessoa, String email, Boolean ativo, LocalDate dataCadastro) {
    this.pessoa = pessoa;
    this.email = email;
    this.ativo = ativo;
    this.dataCadastro = dataCadastro;
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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Boolean getAtivo() {
    return ativo;
  }

  public void setAtivo(Boolean ativo) {
    this.ativo = ativo;
  }

  public LocalDate getDataCadastro() {
    return dataCadastro;
  }

  public void setDataCadastro(LocalDate dataCadastro) {
    this.dataCadastro = dataCadastro;
  }
}
