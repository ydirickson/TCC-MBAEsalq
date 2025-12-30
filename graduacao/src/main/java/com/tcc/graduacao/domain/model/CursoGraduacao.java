package com.tcc.graduacao.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

  public CursoGraduacao(String codigo, String nome, Integer cargaHoraria) {
    this.codigo = codigo;
    this.nome = nome;
    this.cargaHoraria = cargaHoraria;
  }
}
