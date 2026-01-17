package br.com.tcc.posgraduacao.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.LocalDate;

@Entity
@Table(name = "aluno_pos_graduacao")
public class AlunoPosGraduacao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "programa_id", nullable = false)
  private ProgramaPos programa;

  @ManyToOne(optional = false)
  @JoinColumn(name = "pessoa_id", nullable = false)
  private Pessoa pessoa;

  @ManyToOne
  @JoinColumn(name = "orientador_id")
  private ProfessorPosGraduacao orientador;

  @Column(name = "data_matricula", nullable = false)
  private LocalDate dataMatricula;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private SituacaoAcademica status;

  public AlunoPosGraduacao() {
    // JPA
  }

  public AlunoPosGraduacao(
      Pessoa pessoa,
      ProgramaPos programa,
      ProfessorPosGraduacao orientador,
      LocalDate dataMatricula,
      SituacaoAcademica status) {
    this.pessoa = pessoa;
    this.programa = programa;
    this.orientador = orientador;
    this.dataMatricula = dataMatricula;
    this.status = status;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ProgramaPos getPrograma() {
    return programa;
  }

  public void setPrograma(ProgramaPos programa) {
    this.programa = programa;
  }

  public Pessoa getPessoa() {
    return pessoa;
  }

  public void setPessoa(Pessoa pessoa) {
    this.pessoa = pessoa;
  }

  public ProfessorPosGraduacao getOrientador() {
    return orientador;
  }

  public void setOrientador(ProfessorPosGraduacao orientador) {
    this.orientador = orientador;
  }

  public LocalDate getDataMatricula() {
    return dataMatricula;
  }

  public void setDataMatricula(LocalDate dataMatricula) {
    this.dataMatricula = dataMatricula;
  }

  public SituacaoAcademica getStatus() {
    return status;
  }

  public void setStatus(SituacaoAcademica status) {
    this.status = status;
  }

  public Long getPessoaId() {
    Pessoa pessoa = getPessoa();
    return pessoa != null ? pessoa.getId() : null;
  }
}
