package br.com.tcc.assinatura.kafka;

import br.com.tcc.assinatura.domain.model.SolicitacaoAssinatura;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("no-kafka")
public class KafkaNoOpConfig {

  @Bean
  AssinaturaKafkaProducer assinaturaKafkaProducer() {
    return new AssinaturaKafkaProducer(null) {
      @Override
      public void publicarSolicitacaoConcluida(SolicitacaoAssinatura solicitacao) { }
    };
  }
}
