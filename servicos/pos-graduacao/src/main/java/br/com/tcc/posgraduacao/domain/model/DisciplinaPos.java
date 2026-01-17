package br.com.tcc.posgraduacao.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "disciplina_pos")
public class DisciplinaPos {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "programa_id", nullable = false)
  private ProgramaPos programa;

  @Column(nullable = false, length = 20)
  private String codigo;

  @Column(nullable = false, length = 255)
  private String nome;

  @Column(nullable = false)
  private Integer cargaHoraria;

  public DisciplinaPos() {
    // JPA
  }

  public DisciplinaPos(ProgramaPos programa, String codigo, String nome, Integer cargaHoraria) {
    this.programa = programa;
    this.codigo = codigo;
    this.nome = nome;
    this.cargaHoraria = cargaHoraria;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ProgramaPos getPrograma() {
    return programa;
  }

  public void setPrograma(ProgramaPos programa) {
    this.programa = programa;
  }

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public Integer getCargaHoraria() {
    return cargaHoraria;
  }

  public void setCargaHoraria(Integer cargaHoraria) {
    this.cargaHoraria = cargaHoraria;
  }
}
