package br.com.tcc.diplomas.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "requerimento_diploma")
public class RequerimentoDiploma {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "pessoa_id", nullable = false)
  private Pessoa pessoa;

  @ManyToOne(optional = false)
  @JoinColumn(name = "vinculo_id", nullable = false)
  private VinculoAcademico vinculoAcademico;

  @Column(name = "data_solicitacao", nullable = false)
  private LocalDate dataSolicitacao;

  @OneToOne(mappedBy = "requerimento")
  private BaseEmissaoDiploma baseEmissao;

  @OneToOne(mappedBy = "requerimento")
  private StatusEmissao statusEmissao;

  @OneToOne(mappedBy = "requerimento")
  private Diploma diploma;

  public RequerimentoDiploma() {
    // JPA
  }

  public RequerimentoDiploma(Pessoa pessoa, VinculoAcademico vinculoAcademico, LocalDate dataSolicitacao) {
    this.pessoa = pessoa;
    this.vinculoAcademico = vinculoAcademico;
    this.dataSolicitacao = dataSolicitacao;
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

  public VinculoAcademico getVinculoAcademico() {
    return vinculoAcademico;
  }

  public void setVinculoAcademico(VinculoAcademico vinculoAcademico) {
    this.vinculoAcademico = vinculoAcademico;
  }

  public LocalDate getDataSolicitacao() {
    return dataSolicitacao;
  }

  public void setDataSolicitacao(LocalDate dataSolicitacao) {
    this.dataSolicitacao = dataSolicitacao;
  }

  public BaseEmissaoDiploma getBaseEmissao() {
    return baseEmissao;
  }

  public void setBaseEmissao(BaseEmissaoDiploma baseEmissao) {
    this.baseEmissao = baseEmissao;
  }

  public StatusEmissao getStatusEmissao() {
    return statusEmissao;
  }

  public void setStatusEmissao(StatusEmissao statusEmissao) {
    this.statusEmissao = statusEmissao;
  }

  public Diploma getDiploma() {
    return diploma;
  }

  public void setDiploma(Diploma diploma) {
    this.diploma = diploma;
  }
}
