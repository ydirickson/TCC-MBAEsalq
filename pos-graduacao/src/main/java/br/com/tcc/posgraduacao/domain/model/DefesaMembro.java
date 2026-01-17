package br.com.tcc.posgraduacao.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "defesa_membro")
public class DefesaMembro {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "defesa_id", nullable = false)
  private Defesa defesa;

  @ManyToOne(optional = false)
  @JoinColumn(name = "professor_id", nullable = false)
  private ProfessorPosGraduacao professor;

  @Column(precision = 4, scale = 2)
  private BigDecimal nota;

  @Column(nullable = false)
  private boolean presidente;

  public DefesaMembro() {
    // JPA
  }

  public DefesaMembro(Defesa defesa, ProfessorPosGraduacao professor, BigDecimal nota, boolean presidente) {
    this.defesa = defesa;
    this.professor = professor;
    this.nota = nota;
    this.presidente = presidente;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Defesa getDefesa() {
    return defesa;
  }

  public void setDefesa(Defesa defesa) {
    this.defesa = defesa;
  }

  public ProfessorPosGraduacao getProfessor() {
    return professor;
  }

  public void setProfessor(ProfessorPosGraduacao professor) {
    this.professor = professor;
  }

  public BigDecimal getNota() {
    return nota;
  }

  public void setNota(BigDecimal nota) {
    this.nota = nota;
  }

  public boolean isPresidente() {
    return presidente;
  }

  public void setPresidente(boolean presidente) {
    this.presidente = presidente;
  }
}
