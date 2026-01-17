package br.com.tcc.diplomas.domain.repository;

import br.com.tcc.diplomas.domain.model.RequerimentoDiploma;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequerimentoDiplomaRepository extends JpaRepository<RequerimentoDiploma, Long> {
}
