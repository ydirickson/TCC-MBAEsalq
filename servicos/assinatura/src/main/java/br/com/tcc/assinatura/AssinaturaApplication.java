package br.com.tcc.assinatura;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.KeyValues;
import java.util.Optional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.http.server.observation.ServerRequestObservationConvention;

@SpringBootApplication
public class AssinaturaApplication {

  public static void main(String[] args) {
    SpringApplication.run(AssinaturaApplication.class, args);
  }
}

@Configuration
class AssinaturaConfiguration {

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
