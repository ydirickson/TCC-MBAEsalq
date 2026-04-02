package br.com.tcc.diplomas.kafka;

import br.com.tcc.diplomas.domain.model.DocumentoDiploma;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("no-kafka")
public class KafkaNoOpConfig {

  @Bean
  DiplomasKafkaProducer diplomasKafkaProducer() {
    return new DiplomasKafkaProducer(null) {
      @Override
      public void publicarDocumentoDiploma(DocumentoDiploma documento) { }
    };
  }
}
