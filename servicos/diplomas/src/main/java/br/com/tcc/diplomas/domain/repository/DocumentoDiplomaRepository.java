package br.com.tcc.diplomas.domain.repository;

import br.com.tcc.diplomas.domain.model.DocumentoDiploma;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoDiplomaRepository extends JpaRepository<DocumentoDiploma, Long> {
}
