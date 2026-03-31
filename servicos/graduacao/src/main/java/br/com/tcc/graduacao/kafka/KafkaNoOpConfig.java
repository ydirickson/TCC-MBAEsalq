package br.com.tcc.graduacao.kafka;

import br.com.tcc.graduacao.domain.model.Pessoa;
import br.com.tcc.graduacao.domain.model.VinculoAcademico;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("no-kafka")
public class KafkaNoOpConfig {

  @Bean
  GraduacaoKafkaProducer graduacaoKafkaProducer() {
    return new GraduacaoKafkaProducer(null) {
      @Override
      public void publicarPessoa(Pessoa pessoa) { }

      @Override
      public void publicarVinculo(VinculoAcademico vinculo) { }
    };
  }
}
