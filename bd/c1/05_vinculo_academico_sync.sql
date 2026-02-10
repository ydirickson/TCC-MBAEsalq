-- Sincroniza vinculo_academico via triggers (cenario 1)
-- DESABILITADO: VinculoAcademico agora é gerenciado via Java nos serviços
-- As triggers de geração foram removidas. A lógica está nos Services:
-- - AlunoGraduacaoService
-- - ProfessorGraduacaoService
-- - AlunoPosGraduacaoService
-- - ProfessorPosGraduacaoService

-- Mantém apenas a constraint de unicidade
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'uq_vinculo_academico_pessoa_curso_tipo'
  ) THEN
    ALTER TABLE vinculo_academico
      ADD CONSTRAINT uq_vinculo_academico_pessoa_curso_tipo
      UNIQUE (pessoa_id, curso_id, tipo_vinculo);
  END IF;
END $$;
