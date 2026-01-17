package br.com.tcc.graduacao.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "curso_graduacao")
public class CursoGraduacao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String codigo;

  @Column(nullable = false, length = 255)
  private String nome;

  @Column(nullable = false)
  private Integer cargaHoraria;

  public CursoGraduacao() {
    // JPA
  }

  public CursoGraduacao(String codigo, String nome, Integer cargaHoraria) {
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
