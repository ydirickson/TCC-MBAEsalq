WITH pessoas_aluno AS (
  SELECT id, row_number() OVER (ORDER BY id) AS rn
  FROM pessoa
  ORDER BY id
  LIMIT 30
),
turmas AS (
  SELECT id, row_number() OVER (ORDER BY id) AS rn
  FROM turma_graduacao
  ORDER BY id
)
INSERT INTO aluno_graduacao (turma_graduacao_id, pessoa_id, data_matricula, status)
SELECT t.id, p.id, DATE '2024-02-01' + (p.rn * 5)::int, 'ATIVO'
FROM pessoas_aluno p
JOIN turmas t ON t.rn = p.rn;

WITH pessoas_professor AS (
  SELECT id, row_number() OVER (ORDER BY id) AS rn
  FROM pessoa
  ORDER BY id
  OFFSET 30
  LIMIT 10
),
cursos AS (
  SELECT id, row_number() OVER (ORDER BY id) AS rn
  FROM curso_graduacao
)
INSERT INTO professor_graduacao (curso_id, pessoa_id, data_ingresso, nivel_docente, status)
SELECT c.id,
       p.id,
       DATE '2015-02-01' + (p.rn * 30)::int,
       CASE (p.rn % 4)
         WHEN 1 THEN 'DOUTOR'
         WHEN 2 THEN 'ASSOCIADO'
         WHEN 3 THEN 'TITULAR'
         ELSE 'TEMPORARIO'
       END,
       'ATIVO'
FROM pessoas_professor p
JOIN cursos c ON c.rn = ((p.rn - 1) % (SELECT COUNT(*) FROM cursos)) + 1;

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
SELECT p.id,
       c.id,
       c.codigo,
       c.nome,
       'GRADUACAO',
       'GRADUACAO',
       DATE '2024-02-01',
       NULL,
       'ATIVO'
FROM aluno_graduacao a
JOIN pessoa p ON p.id = a.pessoa_id
JOIN turma_graduacao t ON t.id = a.turma_graduacao_id
JOIN curso_graduacao c ON c.id = t.curso_id;

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
SELECT p.id,
       c.id,
       c.codigo,
       c.nome,
       'GRADUACAO',
       'PROFESSOR',
       DATE '2010-01-15',
       NULL,
       'ATIVO'
FROM professor_graduacao pr
JOIN pessoa p ON p.id = pr.pessoa_id
JOIN curso_graduacao c ON c.id = pr.curso_id;

WITH disciplinas_por_curso AS (
  SELECT d.id,
         d.curso_id,
         row_number() OVER (PARTITION BY d.curso_id ORDER BY d.id) AS rn
  FROM disciplina_graduacao d
),
professores AS (
  SELECT id, curso_id
  FROM professor_graduacao
)
INSERT INTO oferta_disciplina (disciplina_id, professor_id, ano, semestre)
SELECT d.id, p.id, 2024, 1
FROM professores p
JOIN disciplinas_por_curso d
  ON d.curso_id = p.curso_id AND d.rn <= 2;

WITH alunos AS (
  SELECT a.id AS aluno_id,
         t.curso_id,
         row_number() OVER (ORDER BY a.id) AS rn
  FROM aluno_graduacao a
  JOIN turma_graduacao t ON t.id = a.turma_graduacao_id
),
ofertas AS (
  SELECT o.id AS oferta_id,
         d.curso_id,
         row_number() OVER (PARTITION BY d.curso_id ORDER BY o.id) AS rn
  FROM oferta_disciplina o
  JOIN disciplina_graduacao d ON d.id = o.disciplina_id
)
INSERT INTO matricula_disciplina (
  aluno_id,
  oferta_disciplina_id,
  data_matricula,
  status,
  nota
)
SELECT a.aluno_id,
       o.oferta_id,
       DATE '2024-02-15' + (a.rn * 2)::int,
       'MATRICULADO',
       NULL
FROM alunos a
JOIN ofertas o
  ON o.curso_id = a.curso_id AND o.rn <= 2;

INSERT INTO avaliacao_oferta_disciplina (oferta_disciplina_id, nome, peso)
SELECT id, 'P1', 50 FROM oferta_disciplina
UNION ALL
SELECT id, 'P2', 50 FROM oferta_disciplina;

INSERT INTO avaliacao_aluno (matricula_id, avaliacao_id, nota)
SELECT m.id, a.id, 7.50
FROM matricula_disciplina m
JOIN oferta_disciplina o ON o.id = m.oferta_disciplina_id
JOIN avaliacao_oferta_disciplina a ON a.oferta_disciplina_id = o.id;
