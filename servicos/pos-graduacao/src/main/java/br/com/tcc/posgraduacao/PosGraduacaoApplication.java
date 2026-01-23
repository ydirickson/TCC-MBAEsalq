package br.com.tcc.posgraduacao;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.KeyValues;
import java.util.Optional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.http.server.observation.ServerRequestObservationConvention;

@SpringBootApplication(scanBasePackages = "br.com.tcc")
@EntityScan(basePackages = "br.com.tcc")
@EnableJpaRepositories(basePackages = "br.com.tcc")
public class PosGraduacaoApplication {

  public static void main(String[] args) {
    SpringApplication.run(PosGraduacaoApplication.class, args);
  }
}

@Configuration
class PosGraduacaoConfiguration {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper().findAndRegisterModules();
  }

  @Bean
  public ServerRequestObservationConvention serverRequestObservationConvention() {
    return new SimulationServerRequestObservationConvention();
  }

  static class SimulationServerRequestObservationConvention
      extends DefaultServerRequestObservationConvention {

    @Override
    public KeyValues getLowCardinalityKeyValues(ServerRequestObservationContext context) {
      KeyValues keyValues = super.getLowCardinalityKeyValues(context);
      String scenario =
          Optional.ofNullable(context.getCarrier().getHeader("X-Scenario")).orElse("NONE");
      keyValues = keyValues.and("scenario", scenario);
      return keyValues;
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(ServerRequestObservationContext context) {
      KeyValues keyValues = super.getHighCardinalityKeyValues(context);
      String runId = Optional.ofNullable(context.getCarrier().getHeader("X-Run-Id")).orElse("NONE");
      return keyValues.and("run.id", runId);
    }
  }
}
