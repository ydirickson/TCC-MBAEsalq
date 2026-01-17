package br.com.tcc.assinatura.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "manifesto_assinatura")
public class ManifestoAssinatura {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "solicitacao_id", nullable = false, unique = true)
  private SolicitacaoAssinatura solicitacao;

  @Column(nullable = false, length = 255)
  private String auditoria;

  @Column(name = "carimbo_tempo", nullable = false)
  private LocalDateTime carimboTempo;

  @Column(name = "hash_final", nullable = false, length = 255)
  private String hashFinal;

  public ManifestoAssinatura() {
    // JPA
  }

  public ManifestoAssinatura(SolicitacaoAssinatura solicitacao, String auditoria, LocalDateTime carimboTempo,
      String hashFinal) {
    this.solicitacao = solicitacao;
    this.auditoria = auditoria;
    this.carimboTempo = carimboTempo;
    this.hashFinal = hashFinal;
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

  public String getAuditoria() {
    return auditoria;
  }

  public void setAuditoria(String auditoria) {
    this.auditoria = auditoria;
  }

  public LocalDateTime getCarimboTempo() {
    return carimboTempo;
  }

  public void setCarimboTempo(LocalDateTime carimboTempo) {
    this.carimboTempo = carimboTempo;
  }

  public String getHashFinal() {
    return hashFinal;
  }

  public void setHashFinal(String hashFinal) {
    this.hashFinal = hashFinal;
  }
}
