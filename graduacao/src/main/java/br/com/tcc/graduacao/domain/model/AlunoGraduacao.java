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
import jakarta.persistence.Table;

@Entity
@Table(name = "aluno_graduacao")
public class AlunoGraduacao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "turma_graduacao_id", nullable = false)
  private TurmaGraduacao turma;

  @ManyToOne(optional = false)
  @JoinColumn(name = "pessoa_id", nullable = false)
  private Pessoa pessoa;

  @Column(name = "data_matricula", nullable = false)
  private LocalDate dataMatricula;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private SituacaoAcademica status;

  public AlunoGraduacao() {
    // JPA
  }

  public AlunoGraduacao(Pessoa pessoa, TurmaGraduacao turma, LocalDate dataMatricula, SituacaoAcademica status) {
    this.pessoa = pessoa;
    this.turma = turma;
    this.dataMatricula = dataMatricula;
    this.status = status;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public TurmaGraduacao getTurma() {
    return turma;
  }

  public void setTurma(TurmaGraduacao turma) {
    this.turma = turma;
  }

  public Pessoa getPessoa() {
    return pessoa;
  }

  public void setPessoa(Pessoa pessoa) {
    this.pessoa = pessoa;
  }

  public CursoGraduacao getCurso() {
    return turma != null ? turma.getCurso() : null;
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
