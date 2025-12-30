package com.tcc.graduacao.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "aluno_graduacao")
public class AlunoGraduacao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long pessoaId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "curso_id", nullable = false)
  private CursoGraduacao curso;

  @Column(nullable = false)
  private LocalDate dataIngresso;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private SituacaoAcademica status;

  public AlunoGraduacao(Long pessoaId, CursoGraduacao curso, LocalDate dataIngresso, SituacaoAcademica status) {
    this.pessoaId = pessoaId;
    this.curso = curso;
    this.dataIngresso = dataIngresso;
    this.status = status;
  }
}
