package br.com.tcc.graduacao.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "matricula_disciplina")
public class MatriculaDisciplina {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "aluno_id", nullable = false)
  private AlunoGraduacao aluno;

  @ManyToOne(optional = false)
  @JoinColumn(name = "oferta_disciplina_id", nullable = false)
  private OfertaDisciplina ofertaDisciplina;

  @Column(name = "data_matricula", nullable = false)
  private LocalDate dataMatricula;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private StatusMatricula status;

  @Column(precision = 4, scale = 2)
  private BigDecimal nota;

  public MatriculaDisciplina() {
    // JPA
  }

  public MatriculaDisciplina(
      AlunoGraduacao aluno,
      OfertaDisciplina ofertaDisciplina,
      LocalDate dataMatricula,
      StatusMatricula status,
      BigDecimal nota) {
    this.aluno = aluno;
    this.ofertaDisciplina = ofertaDisciplina;
    this.dataMatricula = dataMatricula;
    this.status = status;
    this.nota = nota;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public AlunoGraduacao getAluno() {
    return aluno;
  }

  public void setAluno(AlunoGraduacao aluno) {
    this.aluno = aluno;
  }

  public OfertaDisciplina getOfertaDisciplina() {
    return ofertaDisciplina;
  }

  public void setOfertaDisciplina(OfertaDisciplina ofertaDisciplina) {
    this.ofertaDisciplina = ofertaDisciplina;
  }

  public LocalDate getDataMatricula() {
    return dataMatricula;
  }

  public void setDataMatricula(LocalDate dataMatricula) {
    this.dataMatricula = dataMatricula;
  }

  public StatusMatricula getStatus() {
    return status;
  }

  public void setStatus(StatusMatricula status) {
    this.status = status;
  }

  public BigDecimal getNota() {
    return nota;
  }

  public void setNota(BigDecimal nota) {
    this.nota = nota;
  }

  public enum StatusMatricula {
    MATRICULADO,
    APROVADO,
    REPROVADO,
    TRANCADO
  }
}
