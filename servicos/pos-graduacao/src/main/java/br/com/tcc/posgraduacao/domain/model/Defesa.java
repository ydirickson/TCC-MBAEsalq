package br.com.tcc.posgraduacao.domain.model;

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
import java.math.BigDecimal;

@Entity
@Table(name = "defesa_pos")
public class Defesa {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "aluno_id", nullable = false)
  private AlunoPosGraduacao aluno;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private TipoDefesa tipo;

  @Column(precision = 4, scale = 2)
  private BigDecimal nota;

  public Defesa() {
    // JPA
  }

  public Defesa(AlunoPosGraduacao aluno, TipoDefesa tipo, BigDecimal nota) {
    this.aluno = aluno;
    this.tipo = tipo;
    this.nota = nota;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public AlunoPosGraduacao getAluno() {
    return aluno;
  }

  public void setAluno(AlunoPosGraduacao aluno) {
    this.aluno = aluno;
  }

  public TipoDefesa getTipo() {
    return tipo;
  }

  public void setTipo(TipoDefesa tipo) {
    this.tipo = tipo;
  }

  public BigDecimal getNota() {
    return nota;
  }

  public void setNota(BigDecimal nota) {
    this.nota = nota;
  }
}
