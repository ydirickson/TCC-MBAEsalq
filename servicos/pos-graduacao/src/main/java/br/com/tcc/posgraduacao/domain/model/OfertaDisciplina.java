package br.com.tcc.posgraduacao.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "oferta_disciplina_pos")
public class OfertaDisciplina {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "disciplina_id", nullable = false)
  private DisciplinaPos disciplina;

  @ManyToOne(optional = false)
  @JoinColumn(name = "professor_id", nullable = false)
  private ProfessorPosGraduacao professor;

  @Column(nullable = false)
  private Integer ano;

  @Column(nullable = false)
  private Integer semestre;

  public OfertaDisciplina() {
    // JPA
  }

  public OfertaDisciplina(DisciplinaPos disciplina, ProfessorPosGraduacao professor, Integer ano, Integer semestre) {
    this.disciplina = disciplina;
    this.professor = professor;
    this.ano = ano;
    this.semestre = semestre;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public DisciplinaPos getDisciplina() {
    return disciplina;
  }

  public void setDisciplina(DisciplinaPos disciplina) {
    this.disciplina = disciplina;
  }

  public ProfessorPosGraduacao getProfessor() {
    return professor;
  }

  public void setProfessor(ProfessorPosGraduacao professor) {
    this.professor = professor;
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
}
