package br.com.tcc.diplomas.domain.repository;

import br.com.tcc.diplomas.domain.model.BaseEmissaoDiploma;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseEmissaoDiplomaRepository extends JpaRepository<BaseEmissaoDiploma, Long> {
}
