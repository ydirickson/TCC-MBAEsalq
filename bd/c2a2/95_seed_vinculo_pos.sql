SET search_path TO pos_graduacao;

INSERT INTO vinculo_academico (
  pessoa_id,
  curso_id,
  curso_codigo,
  curso_nome,
  curso_tipo,
  tipo_vinculo,
  data_ingresso,
  data_conclusao,
  situacao
)
SELECT
  a.pessoa_id,
  a.programa_id,
  pr.codigo,
  pr.nome,
  'POS_GRADUACAO',
  'ALUNO',
  a.data_matricula,
  a.data_conclusao,
  a.status
FROM aluno_pos_graduacao a
JOIN programa_pos pr ON pr.id = a.programa_id
WHERE NOT EXISTS (
  SELECT 1
  FROM vinculo_academico v
  WHERE v.pessoa_id = a.pessoa_id
    AND v.tipo_vinculo = 'ALUNO'
    AND v.curso_id = a.programa_id
);

INSERT INTO vinculo_academico (
  pessoa_id,
  curso_id,
  curso_codigo,
  curso_nome,
  curso_tipo,
  tipo_vinculo,
  data_ingresso,
  data_conclusao,
  situacao
)
SELECT
  p.pessoa_id,
  p.programa_id,
  pr.codigo,
  pr.nome,
  'POS_GRADUACAO',
  'PROFESSOR',
  p.data_ingresso,
  NULL,
  p.status
FROM professor_pos p
JOIN programa_pos pr ON pr.id = p.programa_id
WHERE NOT EXISTS (
  SELECT 1
  FROM vinculo_academico v
  WHERE v.pessoa_id = p.pessoa_id
    AND v.tipo_vinculo = 'PROFESSOR'
    AND v.curso_id = p.programa_id
);
