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
import java.time.LocalDateTime;

@Entity
@Table(name = "status_emissao")
public class StatusEmissao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "requerimento_id", nullable = false, unique = true)
  private RequerimentoDiploma requerimento;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private StatusEmissaoTipo status;

  @Column(name = "data_atualizacao", nullable = false)
  private LocalDateTime dataAtualizacao;

  public StatusEmissao() {
    // JPA
  }

  public StatusEmissao(RequerimentoDiploma requerimento, StatusEmissaoTipo status, LocalDateTime dataAtualizacao) {
    this.requerimento = requerimento;
    this.status = status;
    this.dataAtualizacao = dataAtualizacao;
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

  public StatusEmissaoTipo getStatus() {
    return status;
  }

  public void setStatus(StatusEmissaoTipo status) {
    this.status = status;
  }

  public LocalDateTime getDataAtualizacao() {
    return dataAtualizacao;
  }

  public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
    this.dataAtualizacao = dataAtualizacao;
  }
}
