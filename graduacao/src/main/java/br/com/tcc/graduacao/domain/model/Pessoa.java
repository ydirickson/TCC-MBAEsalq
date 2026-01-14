package br.com.tcc.graduacao.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "pessoa")
public class Pessoa {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 150)
  private String nome;

  @Column(name = "data_nascimento", nullable = false)
  private LocalDate dataNascimento;

  @Column(name = "nome_social", length = 150)
  private String nomeSocial;

  @OneToOne(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true)
  private DocumentoIdentificacao documentoIdentificacao;

  @OneToMany(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Contato> contatos = new LinkedHashSet<>();

  @OneToMany(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Endereco> enderecos = new LinkedHashSet<>();

  public Pessoa() {
    // JPA
  }

  public Pessoa(String nome, LocalDate dataNascimento, String nomeSocial) {
    this.nome = nome;
    this.dataNascimento = dataNascimento;
    this.nomeSocial = nomeSocial;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public LocalDate getDataNascimento() {
    return dataNascimento;
  }

  public void setDataNascimento(LocalDate dataNascimento) {
    this.dataNascimento = dataNascimento;
  }

  public String getNomeSocial() {
    return nomeSocial;
  }

  public void setNomeSocial(String nomeSocial) {
    this.nomeSocial = nomeSocial;
  }

  public DocumentoIdentificacao getDocumentoIdentificacao() {
    return documentoIdentificacao;
  }

  public void setDocumentoIdentificacao(DocumentoIdentificacao documentoIdentificacao) {
    this.documentoIdentificacao = documentoIdentificacao;
  }

  public Set<Contato> getContatos() {
    return contatos;
  }

  public void setContatos(Set<Contato> contatos) {
    this.contatos = contatos;
  }

  public Set<Endereco> getEnderecos() {
    return enderecos;
  }

  public void setEnderecos(Set<Endereco> enderecos) {
    this.enderecos = enderecos;
  }
}
