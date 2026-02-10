WITH programas AS (
  SELECT id, codigo FROM programa_pos
)
INSERT INTO disciplina_pos (programa_id, codigo, nome, carga_horaria)
SELECT p.id, d.codigo, d.nome, d.carga_horaria
FROM programas p
JOIN (
  VALUES
    ('PPGCC', 'PCC1001', 'Fundamentos de Pesquisa', 60),
    ('PPGCC', 'PCC1002', 'Metodos Avancados de Software', 60),
    ('PPGCC', 'PCC1003', 'Analise de Dados', 60),
    ('PPGAG', 'PAG2001', 'Economia do Agronegocio', 60),
    ('PPGAG', 'PAG2002', 'Gestao de Cadeias', 60),
    ('PPGAG', 'PAG2003', 'Sustentabilidade Rural', 60),
    ('PPGEC', 'PEC3001', 'Macroeconomia Avancada', 60),
    ('PPGEC', 'PEC3002', 'Econometria Aplicada', 60),
    ('PPGEC', 'PEC3003', 'Financas Publicas', 60)
) AS d(programa_codigo, codigo, nome, carga_horaria)
  ON d.programa_codigo = p.codigo;
