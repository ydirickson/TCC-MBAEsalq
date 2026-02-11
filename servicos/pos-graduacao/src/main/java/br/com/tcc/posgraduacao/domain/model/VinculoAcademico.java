package br.com.tcc.posgraduacao.domain.model;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
@Table(name = "vinculo_academico")
public class VinculoAcademico {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "pessoa_id", nullable = false)
  private Pessoa pessoa;

  @Embedded
  private CursoProgramaReferencia curso;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_vinculo", nullable = false, length = 30)
  private TipoVinculo tipoVinculo;

  @Column(name = "data_ingresso", nullable = false)
  private LocalDate dataIngresso;

  @Column(name = "data_conclusao")
  private LocalDate dataConclusao;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private SituacaoAcademica situacao;

  public VinculoAcademico() {
    // JPA
  }

  public VinculoAcademico(Pessoa pessoa, CursoProgramaReferencia curso, TipoVinculo tipoVinculo, LocalDate dataIngresso,
      SituacaoAcademica situacao) {
    this.pessoa = pessoa;
    this.curso = curso;
    this.tipoVinculo = tipoVinculo;
    this.dataIngresso = dataIngresso;
    this.situacao = situacao;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Pessoa getPessoa() {
    return pessoa;
  }

  public void setPessoa(Pessoa pessoa) {
    this.pessoa = pessoa;
  }

  public CursoProgramaReferencia getCurso() {
    return curso;
  }

  public void setCurso(CursoProgramaReferencia curso) {
    this.curso = curso;
  }
  
  public void setDataIngresso(LocalDate dataIngresso) {
      this.dataIngresso = dataIngresso;
  }

  public void setSituacao(SituacaoAcademica situacao) {
      this.situacao = situacao;
  }

  public TipoVinculo getTipoVinculo() {
    return tipoVinculo;
  }

  public void setTipoVinculo(TipoVinculo tipoVinculo) {
    this.tipoVinculo = tipoVinculo;
  }

  public LocalDate getDataIngresso() {
    return dataIngresso;
  }

  public void setDataIngresso(LocalDate dataIngresso) {
    this.dataIngresso = dataIngresso;
  }

  public LocalDate getDataConclusao() {
    return dataConclusao;
  }

  public void setDataConclusao(LocalDate dataConclusao) {
    this.dataConclusao = dataConclusao;
  }

  public SituacaoAcademica getSituacao() {
    return situacao;
  }

  public void setSituacao(SituacaoAcademica situacao) {
    this.situacao = situacao;
  }
}
