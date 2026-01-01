package com.tcc.graduacao.domain.model;

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
import java.time.LocalDate;

@Entity
@Table(name = "aluno_graduacao")
public class AlunoGraduacao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long pessoaId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "curso_id", nullable = false)
  private CursoGraduacao curso;

  @Column(nullable = false)
  private LocalDate dataIngresso;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private SituacaoAcademica status;

  public AlunoGraduacao() {
    // JPA
  }

  public AlunoGraduacao(Long pessoaId, CursoGraduacao curso, LocalDate dataIngresso, SituacaoAcademica status) {
    this.pessoaId = pessoaId;
    this.curso = curso;
    this.dataIngresso = dataIngresso;
    this.status = status;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getPessoaId() {
    return pessoaId;
  }

  public void setPessoaId(Long pessoaId) {
    this.pessoaId = pessoaId;
  }

  public CursoGraduacao getCurso() {
    return curso;
  }

  public void setCurso(CursoGraduacao curso) {
    this.curso = curso;
  }

  public LocalDate getDataIngresso() {
    return dataIngresso;
  }

  public void setDataIngresso(LocalDate dataIngresso) {
    this.dataIngresso = dataIngresso;
  }

  public SituacaoAcademica getStatus() {
    return status;
  }

  public void setStatus(SituacaoAcademica status) {
    this.status = status;
  }
}
