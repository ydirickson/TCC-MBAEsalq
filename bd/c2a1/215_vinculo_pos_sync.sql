-- Sincroniza vinculo_academico a partir da pos_graduacao para read models (cenario c2a1)
-- Atualizado: VinculoAcademico agora é gerenciado via Java nos serviços
-- As triggers de GERAÇÃO foram removidas. A lógica de criação está nos Services.
-- Mantém apenas as triggers de REPLICAÇÃO para diplomas e assinatura.

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'uq_pos_vinculo_pessoa_curso_tipo'
  ) THEN
    ALTER TABLE pos_graduacao.vinculo_academico
      ADD CONSTRAINT uq_pos_vinculo_pessoa_curso_tipo
      UNIQUE (pessoa_id, curso_id, tipo_vinculo);
  END IF;
END $$;

-- Função para replicar vínculos de pos_graduacao para diplomas e assinatura
CREATE OR REPLACE FUNCTION public.fn_replicate_vinculo_pos()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  -- Replicar para diplomas
  INSERT INTO diplomas.vinculo_academico (
    id,
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
  VALUES (
    NEW.id,
    NEW.pessoa_id,
    NEW.curso_id,
    NEW.curso_codigo,
    NEW.curso_nome,
    NEW.curso_tipo,
    NEW.tipo_vinculo,
    NEW.data_ingresso,
    NEW.data_conclusao,
    NEW.situacao
  )
  ON CONFLICT ON CONSTRAINT uq_diplomas_vinculo_pessoa_curso_tipo
  DO UPDATE SET
    id = EXCLUDED.id,
    curso_codigo = EXCLUDED.curso_codigo,
    curso_nome = EXCLUDED.curso_nome,
    curso_tipo = EXCLUDED.curso_tipo,
    data_ingresso = EXCLUDED.data_ingresso,
    data_conclusao = EXCLUDED.data_conclusao,
    situacao = EXCLUDED.situacao;

  -- Replicar para assinatura
  INSERT INTO assinatura.vinculo_academico (
    id,
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
  VALUES (
    NEW.id,
    NEW.pessoa_id,
    NEW.curso_id,
    NEW.curso_codigo,
    NEW.curso_nome,
    NEW.curso_tipo,
    NEW.tipo_vinculo,
    NEW.data_ingresso,
    NEW.data_conclusao,
    NEW.situacao
  )
  ON CONFLICT ON CONSTRAINT uq_assinatura_vinculo_pessoa_curso_tipo
  DO UPDATE SET
    id = EXCLUDED.id,
    curso_codigo = EXCLUDED.curso_codigo,
    curso_nome = EXCLUDED.curso_nome,
    curso_tipo = EXCLUDED.curso_tipo,
    data_ingresso = EXCLUDED.data_ingresso,
    data_conclusao = EXCLUDED.data_conclusao,
    situacao = EXCLUDED.situacao;

  RETURN NEW;
END;
$$;

-- Trigger para replicação de vínculos de pós-graduação (mantém apenas a REPLICAÇÃO, não a geração)
DROP TRIGGER IF EXISTS trg_replicate_vinculo_pos ON pos_graduacao.vinculo_academico;
CREATE TRIGGER trg_replicate_vinculo_pos
AFTER INSERT OR UPDATE ON pos_graduacao.vinculo_academico
FOR EACH ROW
EXECUTE FUNCTION public.fn_replicate_vinculo_pos();
