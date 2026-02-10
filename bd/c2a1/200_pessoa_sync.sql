-- Sincroniza dados compartilhados de pessoa da graduacao para os demais schemas (cenario c2a1)

CREATE OR REPLACE FUNCTION fn_sync_pessoa_graduacao_targets()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  INSERT INTO pos_graduacao.pessoa (id, nome, data_nascimento, nome_social)
  VALUES (NEW.id, NEW.nome, NEW.data_nascimento, NEW.nome_social)
  ON CONFLICT (id) DO UPDATE SET
    nome = EXCLUDED.nome,
    data_nascimento = EXCLUDED.data_nascimento,
    nome_social = EXCLUDED.nome_social;

  INSERT INTO diplomas.pessoa (id, nome, data_nascimento, nome_social)
  VALUES (NEW.id, NEW.nome, NEW.data_nascimento, NEW.nome_social)
  ON CONFLICT (id) DO UPDATE SET
    nome = EXCLUDED.nome,
    data_nascimento = EXCLUDED.data_nascimento,
    nome_social = EXCLUDED.nome_social;

  INSERT INTO assinatura.pessoa (id, nome, data_nascimento, nome_social)
  VALUES (NEW.id, NEW.nome, NEW.data_nascimento, NEW.nome_social)
  ON CONFLICT (id) DO UPDATE SET
    nome = EXCLUDED.nome,
    data_nascimento = EXCLUDED.data_nascimento,
    nome_social = EXCLUDED.nome_social;

  RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION fn_sync_documento_graduacao_targets()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  INSERT INTO pos_graduacao.documento_identificacao (id, pessoa_id, tipo, numero)
  VALUES (NEW.id, NEW.pessoa_id, NEW.tipo, NEW.numero)
  ON CONFLICT (id) DO UPDATE SET
    pessoa_id = EXCLUDED.pessoa_id,
    tipo = EXCLUDED.tipo,
    numero = EXCLUDED.numero;

  INSERT INTO diplomas.documento_identificacao (id, pessoa_id, tipo, numero)
  VALUES (NEW.id, NEW.pessoa_id, NEW.tipo, NEW.numero)
  ON CONFLICT (id) DO UPDATE SET
    pessoa_id = EXCLUDED.pessoa_id,
    tipo = EXCLUDED.tipo,
    numero = EXCLUDED.numero;

  INSERT INTO assinatura.documento_identificacao (id, pessoa_id, tipo, numero)
  VALUES (NEW.id, NEW.pessoa_id, NEW.tipo, NEW.numero)
  ON CONFLICT (id) DO UPDATE SET
    pessoa_id = EXCLUDED.pessoa_id,
    tipo = EXCLUDED.tipo,
    numero = EXCLUDED.numero;

  RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION fn_sync_contato_graduacao_targets()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  INSERT INTO pos_graduacao.contato (id, pessoa_id, email, telefone)
  VALUES (NEW.id, NEW.pessoa_id, NEW.email, NEW.telefone)
  ON CONFLICT (id) DO UPDATE SET
    pessoa_id = EXCLUDED.pessoa_id,
    email = EXCLUDED.email,
    telefone = EXCLUDED.telefone;

  INSERT INTO diplomas.contato (id, pessoa_id, email, telefone)
  VALUES (NEW.id, NEW.pessoa_id, NEW.email, NEW.telefone)
  ON CONFLICT (id) DO UPDATE SET
    pessoa_id = EXCLUDED.pessoa_id,
    email = EXCLUDED.email,
    telefone = EXCLUDED.telefone;

  INSERT INTO assinatura.contato (id, pessoa_id, email, telefone)
  VALUES (NEW.id, NEW.pessoa_id, NEW.email, NEW.telefone)
  ON CONFLICT (id) DO UPDATE SET
    pessoa_id = EXCLUDED.pessoa_id,
    email = EXCLUDED.email,
    telefone = EXCLUDED.telefone;

  RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION fn_sync_endereco_graduacao_targets()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  INSERT INTO pos_graduacao.endereco (id, pessoa_id, logradouro, cidade, uf, cep)
  VALUES (NEW.id, NEW.pessoa_id, NEW.logradouro, NEW.cidade, NEW.uf, NEW.cep)
  ON CONFLICT (id) DO UPDATE SET
    pessoa_id = EXCLUDED.pessoa_id,
    logradouro = EXCLUDED.logradouro,
    cidade = EXCLUDED.cidade,
    uf = EXCLUDED.uf,
    cep = EXCLUDED.cep;

  INSERT INTO diplomas.endereco (id, pessoa_id, logradouro, cidade, uf, cep)
  VALUES (NEW.id, NEW.pessoa_id, NEW.logradouro, NEW.cidade, NEW.uf, NEW.cep)
  ON CONFLICT (id) DO UPDATE SET
    pessoa_id = EXCLUDED.pessoa_id,
    logradouro = EXCLUDED.logradouro,
    cidade = EXCLUDED.cidade,
    uf = EXCLUDED.uf,
    cep = EXCLUDED.cep;

  INSERT INTO assinatura.endereco (id, pessoa_id, logradouro, cidade, uf, cep)
  VALUES (NEW.id, NEW.pessoa_id, NEW.logradouro, NEW.cidade, NEW.uf, NEW.cep)
  ON CONFLICT (id) DO UPDATE SET
    pessoa_id = EXCLUDED.pessoa_id,
    logradouro = EXCLUDED.logradouro,
    cidade = EXCLUDED.cidade,
    uf = EXCLUDED.uf,
    cep = EXCLUDED.cep;

  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_sync_pessoa_graduacao_targets ON graduacao.pessoa;
CREATE TRIGGER trg_sync_pessoa_graduacao_targets
AFTER INSERT OR UPDATE ON graduacao.pessoa
FOR EACH ROW
EXECUTE FUNCTION fn_sync_pessoa_graduacao_targets();

DROP TRIGGER IF EXISTS trg_sync_documento_graduacao_targets ON graduacao.documento_identificacao;
CREATE TRIGGER trg_sync_documento_graduacao_targets
AFTER INSERT OR UPDATE ON graduacao.documento_identificacao
FOR EACH ROW
EXECUTE FUNCTION fn_sync_documento_graduacao_targets();

DROP TRIGGER IF EXISTS trg_sync_contato_graduacao_targets ON graduacao.contato;
CREATE TRIGGER trg_sync_contato_graduacao_targets
AFTER INSERT OR UPDATE ON graduacao.contato
FOR EACH ROW
EXECUTE FUNCTION fn_sync_contato_graduacao_targets();

DROP TRIGGER IF EXISTS trg_sync_endereco_graduacao_targets ON graduacao.endereco;
CREATE TRIGGER trg_sync_endereco_graduacao_targets
AFTER INSERT OR UPDATE ON graduacao.endereco
FOR EACH ROW
EXECUTE FUNCTION fn_sync_endereco_graduacao_targets();
