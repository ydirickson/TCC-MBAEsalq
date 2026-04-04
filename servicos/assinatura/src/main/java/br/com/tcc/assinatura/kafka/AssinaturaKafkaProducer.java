package br.com.tcc.assinatura.kafka;

import br.com.tcc.assinatura.domain.model.DocumentoAssinavel;
import br.com.tcc.assinatura.domain.model.SolicitacaoAssinatura;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AssinaturaKafkaProducer {

  static final String TOPICO_SOLICITACAO_CONCLUIDA = "tcc.assinatura.solicitacao_concluida";

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public AssinaturaKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publicarSolicitacaoConcluida(SolicitacaoAssinatura solicitacao) {
    DocumentoAssinavel documentoAssinavel = solicitacao.getDocumentoAssinavel();
    if (documentoAssinavel == null || documentoAssinavel.getDocumentoDiploma() == null) {
      return;
    }
    SolicitacaoConcluidaEvent event = new SolicitacaoConcluidaEvent(
        documentoAssinavel.getDocumentoDiploma().getId(),
        solicitacao.getStatus().name(),
        solicitacao.getDataConclusao());
    kafkaTemplate.send(TOPICO_SOLICITACAO_CONCLUIDA, String.valueOf(solicitacao.getId()), event);
  }
}
