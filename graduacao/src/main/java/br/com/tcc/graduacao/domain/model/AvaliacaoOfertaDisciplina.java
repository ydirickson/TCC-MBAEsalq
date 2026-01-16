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
@Table(name = "avaliacao_oferta_disciplina")
public class AvaliacaoOfertaDisciplina {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "oferta_disciplina_id", nullable = false)
  private OfertaDisciplina ofertaDisciplina;

  @Column(nullable = false, length = 100)
  private String nome;

  @Column(nullable = false)
  private Short peso;

  public AvaliacaoOfertaDisciplina() {
    // JPA
  }

  public AvaliacaoOfertaDisciplina(OfertaDisciplina ofertaDisciplina, String nome, Short peso) {
    this.ofertaDisciplina = ofertaDisciplina;
    this.nome = nome;
    this.peso = peso;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public OfertaDisciplina getOfertaDisciplina() {
    return ofertaDisciplina;
  }

  public void setOfertaDisciplina(OfertaDisciplina ofertaDisciplina) {
    this.ofertaDisciplina = ofertaDisciplina;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public Short getPeso() {
    return peso;
  }

  public void setPeso(Short peso) {
    this.peso = peso;
  }
}
