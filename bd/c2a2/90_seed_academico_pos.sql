SET search_path TO pos_graduacao;

WITH pessoas_professor AS (
  SELECT id, row_number() OVER (ORDER BY id) AS rn
  FROM pessoa
  ORDER BY id
  OFFSET 10
  LIMIT 6
),
programas AS (
  SELECT id, row_number() OVER (ORDER BY id) AS rn
  FROM programa_pos
)
INSERT INTO professor_pos (programa_id, pessoa_id, data_ingresso, nivel_docente, status)
SELECT pr.id,
       p.id,
       DATE '2014-01-10' + (p.rn * 30)::int,
       CASE (p.rn % 4)
         WHEN 1 THEN 'DOUTOR'
         WHEN 2 THEN 'ASSOCIADO'
         WHEN 3 THEN 'TITULAR'
         ELSE 'TEMPORARIO'
       END,
       'ATIVO'
FROM pessoas_professor p
JOIN programas pr ON pr.rn = ((p.rn - 1) % (SELECT COUNT(*) FROM programas)) + 1;

WITH pessoas_aluno AS (
  SELECT id, row_number() OVER (ORDER BY id) AS rn
  FROM pessoa
  ORDER BY id DESC
  LIMIT 8
),
programas AS (
  SELECT id, row_number() OVER (ORDER BY id) AS rn
  FROM programa_pos
),
orientadores AS (
  SELECT id AS professor_id,
         programa_id,
         row_number() OVER (PARTITION BY programa_id ORDER BY id) AS rn
  FROM professor_pos
)
INSERT INTO aluno_pos_graduacao (programa_id, pessoa_id, orientador_id, data_matricula, status)
SELECT pr.id,
       p.id,
       o.professor_id,
       DATE '2024-03-01' + (p.rn * 7)::int,
       'ATIVO'
FROM pessoas_aluno p
JOIN programas pr ON pr.rn = ((p.rn - 1) % (SELECT COUNT(*) FROM programas)) + 1
LEFT JOIN orientadores o ON o.programa_id = pr.id AND o.rn = 1;

WITH disciplinas_por_programa AS (
  SELECT d.id,
         d.programa_id,
         row_number() OVER (PARTITION BY d.programa_id ORDER BY d.id) AS rn
  FROM disciplina_pos d
),
professores AS (
  SELECT id, programa_id
  FROM professor_pos
)
INSERT INTO oferta_disciplina_pos (disciplina_id, professor_id, ano, semestre)
SELECT d.id, p.id, 2024, 1
FROM professores p
JOIN disciplinas_por_programa d
  ON d.programa_id = p.programa_id AND d.rn <= 2;

WITH alunos AS (
  SELECT a.id AS aluno_id,
         a.programa_id,
         row_number() OVER (ORDER BY a.id) AS rn
  FROM aluno_pos_graduacao a
),
ofertas AS (
  SELECT o.id AS oferta_id,
         d.programa_id,
         row_number() OVER (PARTITION BY d.programa_id ORDER BY o.id) AS rn
  FROM oferta_disciplina_pos o
  JOIN disciplina_pos d ON d.id = o.disciplina_id
)
INSERT INTO matricula_disciplina_pos (
  aluno_id,
  oferta_disciplina_id,
  data_matricula,
  status,
  nota
)
SELECT a.aluno_id,
       o.oferta_id,
       DATE '2024-03-10' + (a.rn * 3)::int,
       'MATRICULADO',
       NULL
FROM alunos a
JOIN ofertas o
  ON o.programa_id = a.programa_id AND o.rn <= 2;

WITH alunos_defesa AS (
  SELECT a.id AS aluno_id,
         a.programa_id,
         row_number() OVER (ORDER BY a.id) AS rn
  FROM aluno_pos_graduacao a
  LIMIT 6
)
INSERT INTO defesa_pos (aluno_id, tipo, nota)
SELECT a.aluno_id,
       CASE WHEN a.rn % 2 = 0 THEN 'QUALIFICACAO' ELSE 'DEFESA_FINAL' END,
       8.00 + (a.rn * 0.2)
FROM alunos_defesa a;

WITH membros AS (
  SELECT d.id AS defesa_id,
         a.programa_id,
         row_number() OVER (PARTITION BY d.id ORDER BY p.id) AS rn,
         p.id AS professor_id
  FROM defesa_pos d
  JOIN aluno_pos_graduacao a ON a.id = d.aluno_id
  JOIN professor_pos p ON p.programa_id = a.programa_id
)
INSERT INTO defesa_membro (defesa_id, professor_id, nota, presidente)
SELECT m.defesa_id,
       m.professor_id,
       8.50 + (m.rn * 0.1),
       CASE WHEN m.rn = 1 THEN true ELSE false END
FROM membros m
WHERE m.rn <= 2;
