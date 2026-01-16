package br.com.tcc.graduacao.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.tcc.graduacao.domain.model.ProfessorGraduacao;

public interface ProfessorGraduacaoRepository extends JpaRepository<ProfessorGraduacao, Long> {
}
