package br.com.tcc.posgraduacao.kafka;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@Configuration
@Profile("no-kafka")
public class KafkaNoOpConfig {

  @Bean
  KafkaTemplate<String, Object> kafkaTemplate() {
    DefaultKafkaProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(
        Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName(),
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()
        ));
    return new KafkaTemplate<>(factory) {
      @Override
      public CompletableFuture<SendResult<String, Object>> send(String topic, String key, Object data) {
        return CompletableFuture.completedFuture(null);
      }
    };
  }
}
