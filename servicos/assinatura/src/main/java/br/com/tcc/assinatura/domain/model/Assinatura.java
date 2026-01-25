package br.com.tcc.assinatura.domain.model;

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
import java.time.LocalDateTime;

@Entity
@Table(name = "assinatura")
public class Assinatura {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "solicitacao_id", nullable = false)
  private SolicitacaoAssinatura solicitacao;

  @ManyToOne(optional = true)
  @JoinColumn(name = "usuario_assinante_id")
  private UsuarioAssinante usuarioAssinante;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private StatusAssinatura status;

  @Column(name = "data_assinatura")
  private LocalDateTime dataAssinatura;

  @Column(name = "motivo_recusa", length = 255)
  private String motivoRecusa;

  public Assinatura() {
    // JPA
  }

  public Assinatura(SolicitacaoAssinatura solicitacao, UsuarioAssinante usuarioAssinante, StatusAssinatura status,
      LocalDateTime dataAssinatura, String motivoRecusa) {
    this.solicitacao = solicitacao;
    this.usuarioAssinante = usuarioAssinante;
    this.status = status;
    this.dataAssinatura = dataAssinatura;
    this.motivoRecusa = motivoRecusa;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public SolicitacaoAssinatura getSolicitacao() {
    return solicitacao;
  }

  public void setSolicitacao(SolicitacaoAssinatura solicitacao) {
    this.solicitacao = solicitacao;
  }

  public UsuarioAssinante getUsuarioAssinante() {
    return usuarioAssinante;
  }

  public void setUsuarioAssinante(UsuarioAssinante usuarioAssinante) {
    this.usuarioAssinante = usuarioAssinante;
  }

  public StatusAssinatura getStatus() {
    return status;
  }

  public void setStatus(StatusAssinatura status) {
    this.status = status;
  }

  public LocalDateTime getDataAssinatura() {
    return dataAssinatura;
  }

  public void setDataAssinatura(LocalDateTime dataAssinatura) {
    this.dataAssinatura = dataAssinatura;
  }

  public String getMotivoRecusa() {
    return motivoRecusa;
  }

  public void setMotivoRecusa(String motivoRecusa) {
    this.motivoRecusa = motivoRecusa;
  }
}
