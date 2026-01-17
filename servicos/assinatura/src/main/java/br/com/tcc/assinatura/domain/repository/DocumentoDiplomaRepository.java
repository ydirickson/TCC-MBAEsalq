package br.com.tcc.assinatura.domain.repository;

import br.com.tcc.assinatura.domain.model.DocumentoDiploma;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoDiplomaRepository extends JpaRepository<DocumentoDiploma, Long> {
}
