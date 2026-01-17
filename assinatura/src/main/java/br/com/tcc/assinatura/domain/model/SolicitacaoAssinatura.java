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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacao_assinatura")
public class SolicitacaoAssinatura {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "documento_assinavel_id", nullable = false)
  private DocumentoAssinavel documentoAssinavel;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private StatusSolicitacaoAssinatura status;

  @Column(name = "data_solicitacao", nullable = false)
  private LocalDateTime dataSolicitacao;

  @Column(name = "data_conclusao")
  private LocalDateTime dataConclusao;

  @OneToOne(mappedBy = "solicitacao")
  private ManifestoAssinatura manifestoAssinatura;

  public SolicitacaoAssinatura() {
    // JPA
  }

  public SolicitacaoAssinatura(DocumentoAssinavel documentoAssinavel, StatusSolicitacaoAssinatura status,
      LocalDateTime dataSolicitacao, LocalDateTime dataConclusao) {
    this.documentoAssinavel = documentoAssinavel;
    this.status = status;
    this.dataSolicitacao = dataSolicitacao;
    this.dataConclusao = dataConclusao;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public DocumentoAssinavel getDocumentoAssinavel() {
    return documentoAssinavel;
  }

  public void setDocumentoAssinavel(DocumentoAssinavel documentoAssinavel) {
    this.documentoAssinavel = documentoAssinavel;
  }

  public StatusSolicitacaoAssinatura getStatus() {
    return status;
  }

  public void setStatus(StatusSolicitacaoAssinatura status) {
    this.status = status;
  }

  public LocalDateTime getDataSolicitacao() {
    return dataSolicitacao;
  }

  public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
    this.dataSolicitacao = dataSolicitacao;
  }

  public LocalDateTime getDataConclusao() {
    return dataConclusao;
  }

  public void setDataConclusao(LocalDateTime dataConclusao) {
    this.dataConclusao = dataConclusao;
  }

  public ManifestoAssinatura getManifestoAssinatura() {
    return manifestoAssinatura;
  }

  public void setManifestoAssinatura(ManifestoAssinatura manifestoAssinatura) {
    this.manifestoAssinatura = manifestoAssinatura;
  }
}
