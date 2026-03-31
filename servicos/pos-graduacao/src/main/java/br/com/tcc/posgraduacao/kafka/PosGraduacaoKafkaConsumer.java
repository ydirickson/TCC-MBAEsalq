package br.com.tcc.posgraduacao.kafka;

import br.com.tcc.posgraduacao.domain.model.Pessoa;
import br.com.tcc.posgraduacao.domain.repository.PessoaRepository;
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

  public PosGraduacaoKafkaConsumer(PessoaRepository pessoaRepository) {
    this.pessoaRepository = pessoaRepository;
  }

  @KafkaListener(topics = "tcc.graduacao.pessoa", groupId = "${spring.application.name}")
  @Transactional
  public void consumirPessoaGraduacao(PessoaEvent event) {
    log.debug("Recebendo Pessoa de graduacao: id={}", event.id());
    Pessoa pessoa = pessoaRepository.findById(event.id()).orElseGet(Pessoa::new);
    pessoa.setId(event.id());
    pessoa.setNome(event.nome());
    pessoa.setDataNascimento(event.dataNascimento());
    pessoa.setNomeSocial(event.nomeSocial());
    pessoaRepository.save(pessoa);
  }
}
