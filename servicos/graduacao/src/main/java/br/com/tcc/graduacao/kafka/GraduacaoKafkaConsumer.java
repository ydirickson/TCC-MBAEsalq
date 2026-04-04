package br.com.tcc.graduacao.kafka;

import br.com.tcc.graduacao.domain.model.Pessoa;
import br.com.tcc.graduacao.domain.repository.PessoaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!no-kafka")
public class GraduacaoKafkaConsumer {

  private static final Logger log = LoggerFactory.getLogger(GraduacaoKafkaConsumer.class);

  private final PessoaRepository pessoaRepository;
  private final ObjectMapper objectMapper;

  public GraduacaoKafkaConsumer(PessoaRepository pessoaRepository, ObjectMapper objectMapper) {
    this.pessoaRepository = pessoaRepository;
    this.objectMapper = objectMapper;
  }

  @KafkaListener(topics = "tcc.pos_graduacao.pessoa", groupId = "${spring.application.name}")
  @Transactional
  public void consumirPessoaPosGraduacao(String payload) throws Exception {
    PessoaEvent event = objectMapper.readValue(payload, PessoaEvent.class);
    log.debug("Recebendo Pessoa de pos-graduacao: id={}", event.id());
    pessoaRepository.upsert(event.id(), event.nome(), event.dataNascimento(), event.nomeSocial());
  }
}
