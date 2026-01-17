package br.com.tcc.graduacao.domain.model;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "professor_graduacao")
public class ProfessorGraduacao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "curso_id", nullable = false)
  private CursoGraduacao curso;

  @OneToOne(optional = false)
  @JoinColumn(name = "pessoa_id", nullable = false)
  private Pessoa pessoa;

  @Column(name = "data_ingresso", nullable = false)
  private LocalDate dataIngresso;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private NivelDocente nivelDocente;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private SituacaoFuncional status;

  public ProfessorGraduacao() {
    // JPA
  }

  public ProfessorGraduacao(Pessoa pessoa, CursoGraduacao curso, LocalDate dataIngresso, NivelDocente nivelDocente, SituacaoFuncional status) {
    this.pessoa = pessoa;
    this.curso = curso;
    this.dataIngresso = dataIngresso;
    this.nivelDocente = nivelDocente;
    this.status = status;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public CursoGraduacao getCurso() {
    return curso;
  }

  public void setCurso(CursoGraduacao curso) {
    this.curso = curso;
  }

  public Pessoa getPessoa() {
    return pessoa;
  }

  public void setPessoa(Pessoa pessoa) {
    this.pessoa = pessoa;
  }

  public LocalDate getDataIngresso() {
    return dataIngresso;
  }

  public void setDataIngresso(LocalDate dataIngresso) {
    this.dataIngresso = dataIngresso;
  }

  public NivelDocente getNivelDocente() {
    return nivelDocente;
  }

  public void setNivelDocente(NivelDocente nivelDocente) {
    this.nivelDocente = nivelDocente;
  }

  public SituacaoFuncional getStatus() {
    return status;
  }

  public void setStatus(SituacaoFuncional status) {
    this.status = status;
  }

  public Long getPessoaId() {
    Pessoa p = getPessoa();
    return p != null ? p.getId() : null;
  }
}
