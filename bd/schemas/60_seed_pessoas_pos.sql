SET search_path TO pos_graduacao;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Adriana Tavares', '1983-09-11', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC100001' FROM pessoa
),
contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'adriana.tavares@exemplo.com', '11 97100-1101' FROM pessoa
),
enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Tavares 201', 'Sao Paulo', 'SP', '11001-301' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Bruno Mesquita', '1984-10-12', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC100002' FROM pessoa
),
contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'bruno.mesquita@exemplo.com', '11 97100-1102' FROM pessoa
),
enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Mesquita 202', 'Rio de Janeiro', 'RJ', '11002-302' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Carolina Salles', '1985-11-13', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC100003' FROM pessoa
),
contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'carolina.salles@exemplo.com', '11 97100-1103' FROM pessoa
),
enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Salles 203', 'Belo Horizonte', 'MG', '11003-303' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Diego Ramos', '1986-12-14', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC100004' FROM pessoa
),
contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'diego.ramos@exemplo.com', '11 97100-1104' FROM pessoa
),
enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Ramos 204', 'Curitiba', 'PR', '11004-304' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Evelyn Matos', '1987-01-15', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC100005' FROM pessoa
),
contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'evelyn.matos@exemplo.com', '11 97100-1105' FROM pessoa
),
enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Matos 205', 'Porto Alegre', 'RS', '11005-305' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Felipe Brito', '1988-02-16', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC100006' FROM pessoa
),
contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'felipe.brito@exemplo.com', '11 97100-1106' FROM pessoa
),
enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Brito 206', 'Recife', 'PE', '11006-306' FROM pessoa
)
SELECT 1;
