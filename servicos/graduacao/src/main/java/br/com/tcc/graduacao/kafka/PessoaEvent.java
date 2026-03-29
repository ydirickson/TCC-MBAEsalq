package br.com.tcc.graduacao.kafka;

import java.time.LocalDate;

public record PessoaEvent(Long id, String nome, LocalDate dataNascimento, String nomeSocial) {
}
