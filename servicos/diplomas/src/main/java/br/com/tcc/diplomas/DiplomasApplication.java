package br.com.tcc.diplomas;

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
public class DiplomasApplication {

  public static void main(String[] args) {
    SpringApplication.run(DiplomasApplication.class, args);
  }
}

@Configuration
class DiplomasConfiguration {

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
      String runId = Optional.ofNullable(context.getCarrier().getHeader("X-Run-Id")).orElse("NONE");
      String scenario =
          Optional.ofNullable(context.getCarrier().getHeader("X-Scenario")).orElse("NONE");
      keyValues = keyValues.and("run.id", runId);
      keyValues = keyValues.and("scenario", scenario);
      return keyValues;
    }
  }
}
