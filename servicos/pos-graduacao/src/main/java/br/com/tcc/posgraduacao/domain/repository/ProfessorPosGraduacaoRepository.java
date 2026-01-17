package br.com.tcc.posgraduacao.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.tcc.posgraduacao.domain.model.ProfessorPosGraduacao;

public interface ProfessorPosGraduacaoRepository extends JpaRepository<ProfessorPosGraduacao, Long> {
}
