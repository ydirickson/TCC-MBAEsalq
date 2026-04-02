package br.com.tcc.diplomas.kafka;

import br.com.tcc.diplomas.domain.model.DocumentoDiploma;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!no-kafka")
public class DiplomasKafkaProducer {

  static final String TOPICO_DOCUMENTO_DIPLOMA = "tcc.diplomas.documento_diploma";

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public DiplomasKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publicarDocumentoDiploma(DocumentoDiploma documento) {
    DocumentoDiplomaEvent event = new DocumentoDiplomaEvent(
        documento.getId(),
        documento.getDiploma() != null ? documento.getDiploma().getId() : null,
        documento.getVersao(),
        documento.getDataGeracao(),
        documento.getUrlArquivo(),
        documento.getHashDocumento());
    kafkaTemplate.send(TOPICO_DOCUMENTO_DIPLOMA, String.valueOf(documento.getId()), event);
  }
}
