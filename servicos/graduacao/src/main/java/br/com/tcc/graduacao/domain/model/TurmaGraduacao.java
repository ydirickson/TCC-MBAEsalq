package br.com.tcc.graduacao.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "turma_graduacao")
public class TurmaGraduacao {

  @Id
  @Column(length = 20)
  private String id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "curso_id", nullable = false)
  private CursoGraduacao curso;

  @Column(nullable = false)
  private Integer ano;

  @Column(nullable = false)
  private Integer semestre;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private StatusTurma status;

  public TurmaGraduacao() {
    // JPA
  }

  public TurmaGraduacao(CursoGraduacao curso, Integer ano, Integer semestre, StatusTurma status) {
    this.curso = curso;
    this.ano = ano;
    this.semestre = semestre;
    this.status = status;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public CursoGraduacao getCurso() {
    return curso;
  }

  public void setCurso(CursoGraduacao curso) {
    this.curso = curso;
  }

  public Integer getAno() {
    return ano;
  }

  public void setAno(Integer ano) {
    this.ano = ano;
  }

  public Integer getSemestre() {
    return semestre;
  }

  public void setSemestre(Integer semestre) {
    this.semestre = semestre;
  }

  public StatusTurma getStatus() {
    return status;
  }

  public void setStatus(StatusTurma status) {
    this.status = status;
  }

  public enum StatusTurma {
    ATIVA,
    TRANCADA,
    CONCLUIDA
  }
}
