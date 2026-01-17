package br.com.tcc.diplomas.domain.repository;

import br.com.tcc.diplomas.domain.model.Diploma;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiplomaRepository extends JpaRepository<Diploma, Long> {
}
