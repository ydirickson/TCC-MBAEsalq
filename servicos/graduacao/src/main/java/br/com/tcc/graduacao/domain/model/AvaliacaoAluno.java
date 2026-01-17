package br.com.tcc.graduacao.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "avaliacao_aluno")
public class AvaliacaoAluno {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "matricula_id", nullable = false)
  private MatriculaDisciplina matricula;

  @ManyToOne(optional = false)
  @JoinColumn(name = "avaliacao_id", nullable = false)
  private AvaliacaoOfertaDisciplina avaliacao;

  @Column(nullable = false, precision = 4, scale = 2)
  private BigDecimal nota;

  public AvaliacaoAluno() {
    // JPA
  }

  public AvaliacaoAluno(MatriculaDisciplina matricula, AvaliacaoOfertaDisciplina avaliacao, BigDecimal nota) {
    this.matricula = matricula;
    this.avaliacao = avaliacao;
    this.nota = nota;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public MatriculaDisciplina getMatricula() {
    return matricula;
  }

  public void setMatricula(MatriculaDisciplina matricula) {
    this.matricula = matricula;
  }

  public AvaliacaoOfertaDisciplina getAvaliacao() {
    return avaliacao;
  }

  public void setAvaliacao(AvaliacaoOfertaDisciplina avaliacao) {
    this.avaliacao = avaliacao;
  }

  public BigDecimal getNota() {
    return nota;
  }

  public void setNota(BigDecimal nota) {
    this.nota = nota;
  }
}
