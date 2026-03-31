package br.com.tcc.assinatura.kafka;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("no-kafka")
public class KafkaNoOpConfig {
}
