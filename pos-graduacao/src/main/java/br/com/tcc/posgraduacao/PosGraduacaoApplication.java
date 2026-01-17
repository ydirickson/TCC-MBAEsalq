package br.com.tcc.posgraduacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "br.com.tcc")
@EntityScan(basePackages = "br.com.tcc")
@EnableJpaRepositories(basePackages = "br.com.tcc")
public class PosGraduacaoApplication {

  public static void main(String[] args) {
    SpringApplication.run(PosGraduacaoApplication.class, args);
  }
}
