package br.com.tcc.graduacao.kafka;

import br.com.tcc.graduacao.domain.model.Pessoa;
import br.com.tcc.graduacao.domain.repository.PessoaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GraduacaoKafkaConsumer {

  private static final Logger log = LoggerFactory.getLogger(GraduacaoKafkaConsumer.class);

  private final PessoaRepository pessoaRepository;

  public GraduacaoKafkaConsumer(PessoaRepository pessoaRepository) {
    this.pessoaRepository = pessoaRepository;
  }

  @KafkaListener(topics = "tcc.pos_graduacao.pessoa", groupId = "${spring.application.name}")
  @Transactional
  public void consumirPessoaPosGraduacao(PessoaEvent event) {
    log.debug("Recebendo Pessoa de pos-graduacao: id={}", event.id());
    Pessoa pessoa = pessoaRepository.findById(event.id()).orElseGet(Pessoa::new);
    pessoa.setId(event.id());
    pessoa.setNome(event.nome());
    pessoa.setDataNascimento(event.dataNascimento());
    pessoa.setNomeSocial(event.nomeSocial());
    pessoaRepository.save(pessoa);
  }
}
