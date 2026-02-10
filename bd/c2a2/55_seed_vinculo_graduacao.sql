SET search_path TO graduacao;

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
  t.curso_id,
  c.codigo,
  c.nome,
  'GRADUACAO',
  'ALUNO',
  a.data_matricula,
  a.data_conclusao,
  a.status
FROM aluno_graduacao a
JOIN turma_graduacao t ON t.id = a.turma_graduacao_id
JOIN curso_graduacao c ON c.id = t.curso_id
WHERE NOT EXISTS (
  SELECT 1
  FROM vinculo_academico v
  WHERE v.pessoa_id = a.pessoa_id
    AND v.tipo_vinculo = 'ALUNO'
    AND v.curso_id = t.curso_id
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
  p.curso_id,
  c.codigo,
  c.nome,
  'GRADUACAO',
  'PROFESSOR',
  p.data_ingresso,
  NULL,
  p.status
FROM professor_graduacao p
JOIN curso_graduacao c ON c.id = p.curso_id
WHERE NOT EXISTS (
  SELECT 1
  FROM vinculo_academico v
  WHERE v.pessoa_id = p.pessoa_id
    AND v.tipo_vinculo = 'PROFESSOR'
    AND v.curso_id = p.curso_id
);
