package br.com.tcc.posgraduacao.kafka;

import br.com.tcc.posgraduacao.domain.model.Pessoa;
import br.com.tcc.posgraduacao.domain.repository.PessoaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!no-kafka")
public class PosGraduacaoKafkaConsumer {

  private static final Logger log = LoggerFactory.getLogger(PosGraduacaoKafkaConsumer.class);

  private final PessoaRepository pessoaRepository;
  private final ObjectMapper objectMapper;

  public PosGraduacaoKafkaConsumer(PessoaRepository pessoaRepository, ObjectMapper objectMapper) {
    this.pessoaRepository = pessoaRepository;
    this.objectMapper = objectMapper;
  }

  @KafkaListener(topics = "tcc.graduacao.pessoa", groupId = "${spring.application.name}")
  @Transactional
  public void consumirPessoaGraduacao(String payload) throws Exception {
    PessoaEvent event = objectMapper.readValue(payload, PessoaEvent.class);
    log.debug("Recebendo Pessoa de graduacao: id={}", event.id());
    pessoaRepository.upsert(event.id(), event.nome(), event.dataNascimento(), event.nomeSocial());
  }
}
