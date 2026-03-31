package br.com.tcc.posgraduacao.kafka;

import br.com.tcc.posgraduacao.domain.model.Pessoa;
import br.com.tcc.posgraduacao.domain.model.VinculoAcademico;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("no-kafka")
public class KafkaNoOpConfig {

  @Bean
  PosGraduacaoKafkaProducer posGraduacaoKafkaProducer() {
    return new PosGraduacaoKafkaProducer(null) {
      @Override
      public void publicarPessoa(Pessoa pessoa) { }

      @Override
      public void publicarVinculo(VinculoAcademico vinculo) { }
    };
  }
}
