package br.com.tcc.assinatura.domain.repository;

import br.com.tcc.assinatura.domain.model.UsuarioAssinante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioAssinanteRepository extends JpaRepository<UsuarioAssinante, Long> {
}
