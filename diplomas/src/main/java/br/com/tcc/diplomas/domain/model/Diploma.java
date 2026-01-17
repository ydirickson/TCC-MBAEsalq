package br.com.tcc.diplomas.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "diploma")
public class Diploma {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "requerimento_id", nullable = false, unique = true)
  private RequerimentoDiploma requerimento;

  @OneToOne(optional = false)
  @JoinColumn(name = "base_emissao_id", nullable = false)
  private BaseEmissaoDiploma baseEmissao;

  @Column(name = "numero_registro", nullable = false, length = 50)
  private String numeroRegistro;

  @Column(name = "data_emissao", nullable = false)
  private LocalDate dataEmissao;

  public Diploma() {
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

  public BaseEmissaoDiploma getBaseEmissao() {
    return baseEmissao;
  }

  public void setBaseEmissao(BaseEmissaoDiploma baseEmissao) {
    this.baseEmissao = baseEmissao;
  }

  public String getNumeroRegistro() {
    return numeroRegistro;
  }

  public void setNumeroRegistro(String numeroRegistro) {
    this.numeroRegistro = numeroRegistro;
  }

  public LocalDate getDataEmissao() {
    return dataEmissao;
  }

  public void setDataEmissao(LocalDate dataEmissao) {
    this.dataEmissao = dataEmissao;
  }
}
