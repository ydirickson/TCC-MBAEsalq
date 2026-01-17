package br.com.tcc.diplomas.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "base_emissao_diploma")
public class BaseEmissaoDiploma {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "requerimento_id", nullable = false, unique = true)
  private RequerimentoDiploma requerimento;

  @Column(name = "pessoa_id", nullable = false)
  private Long pessoaId;

  @Column(name = "pessoa_nome", nullable = false, length = 150)
  private String pessoaNome;

  @Column(name = "pessoa_nome_social", length = 150)
  private String pessoaNomeSocial;

  @Column(name = "pessoa_data_nascimento", nullable = false)
  private LocalDate pessoaDataNascimento;

  @Column(name = "curso_codigo", nullable = false, length = 50)
  private String cursoCodigo;

  @Column(name = "curso_nome", nullable = false, length = 150)
  private String cursoNome;

  @Enumerated(EnumType.STRING)
  @Column(name = "curso_tipo", nullable = false, length = 30)
  private TipoCursoPrograma cursoTipo;

  @Column(name = "data_conclusao", nullable = false)
  private LocalDate dataConclusao;

  @Column(name = "data_colacao_grau")
  private LocalDate dataColacaoGrau;

  public BaseEmissaoDiploma() {
    // JPA
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public RequerimentoDiploma getRequerimento() {
    return requerimento;
  }

  public void setRequerimento(RequerimentoDiploma requerimento) {
    this.requerimento = requerimento;
  }

  public Long getPessoaId() {
    return pessoaId;
  }

  public void setPessoaId(Long pessoaId) {
    this.pessoaId = pessoaId;
  }

  public String getPessoaNome() {
    return pessoaNome;
  }

  public void setPessoaNome(String pessoaNome) {
    this.pessoaNome = pessoaNome;
  }

  public String getPessoaNomeSocial() {
    return pessoaNomeSocial;
  }

  public void setPessoaNomeSocial(String pessoaNomeSocial) {
    this.pessoaNomeSocial = pessoaNomeSocial;
  }

  public LocalDate getPessoaDataNascimento() {
    return pessoaDataNascimento;
  }

  public void setPessoaDataNascimento(LocalDate pessoaDataNascimento) {
    this.pessoaDataNascimento = pessoaDataNascimento;
  }

  public String getCursoCodigo() {
    return cursoCodigo;
  }

  public void setCursoCodigo(String cursoCodigo) {
    this.cursoCodigo = cursoCodigo;
  }

  public String getCursoNome() {
    return cursoNome;
  }

  public void setCursoNome(String cursoNome) {
    this.cursoNome = cursoNome;
  }

  public TipoCursoPrograma getCursoTipo() {
    return cursoTipo;
  }

  public void setCursoTipo(TipoCursoPrograma cursoTipo) {
    this.cursoTipo = cursoTipo;
  }

  public LocalDate getDataConclusao() {
    return dataConclusao;
  }

  public void setDataConclusao(LocalDate dataConclusao) {
    this.dataConclusao = dataConclusao;
  }

  public LocalDate getDataColacaoGrau() {
    return dataColacaoGrau;
  }

  public void setDataColacaoGrau(LocalDate dataColacaoGrau) {
    this.dataColacaoGrau = dataColacaoGrau;
  }
}
