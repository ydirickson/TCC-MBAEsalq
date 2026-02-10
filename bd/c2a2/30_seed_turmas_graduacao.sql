SET search_path TO graduacao;

INSERT INTO turma_graduacao (id, curso_id, ano, semestre, status)
SELECT
  to_char(ano, 'FM0000') || lpad(semestre::text, 2, '0') || c.codigo,
  c.id,
  ano,
  semestre,
  'ATIVA'
FROM curso_graduacao c
CROSS JOIN generate_series(2024, 2026) AS ano
CROSS JOIN (SELECT 1 AS semestre) s;
