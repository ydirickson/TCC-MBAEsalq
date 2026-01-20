# Replicacao por cenarios de banco
[← Voltar ao indice](./README.md)

Este documento descreve sugestoes de replicacao de dados para o VinculoAcademico e demais entidades comuns em quatro cenarios de infraestrutura.

## Objetivo da simulacao
Comparar replicacao tradicional (recursos nativos do PostgreSQL) com replicacao baseada em eventos via Kafka. A simulacao busca demonstrar que a abordagem por eventos pode manter desempenho e confiabilidade sem perda, ao mesmo tempo em que oferece maior flexibilidade e desacoplamento do modelo de dados.

## 1) Simples (mesmo BD e mesmos schemas)
**Contexto:** todos os servicos compartilham o mesmo banco e os mesmos schemas.
- **Padrao sugerido:** modelo unico e tabelas comuns unicas (Pessoa, VinculoAcademico, Contato, Endereco, DocumentoIdentificacao).
- **Replicacao:** nao ha replicacao fisica; ha apenas **ownership logico** (Graduacao/Pos produzem; Diplomas/Assinatura consomem).
- **Integracao:** leitura direta por FK e consultas compartilhadas; use visoes para separar leitura por servico, se necessario.
- **Risco:** acoplamento forte e concorrencia; exigir contratos claros de escrita (somente produtores oficiais atualizam).

## 2) Schema (mesmo BD, schemas distintos por servico)
**Contexto:** um banco unico com schemas separados por servico.
- **Padrao sugerido:** tabelas comuns duplicadas em cada schema com o mesmo modelo.
- **Replicacao:** eventos ou jobs de sincronizacao para copiar dados dos schemas produtores para schemas consumidores.
- **Mecanismo:** 
  - **Evento** (preferencial): publicar `PessoaCriada/Atualizada`, `VinculoAcademicoCriado/Atualizado` e aplicar no schema alvo.
  - **CDC/stream** (opcional): replicar mudancas do schema produtor para consumidores.
- **Risco:** divergencia entre schemas; usar idempotencia por `id` e `timestamp/versao`.

## 3) Databases (bancos distintos no mesmo servidor)
**Contexto:** cada servico possui seu proprio database no mesmo servidor Postgres.
- **Padrao sugerido:** mesmo modelo comum em cada database, com ownership por servico.
- **Replicacao:** eventos assincronos (Kafka, filas, ou jobs) para manter read models alinhados.
- **Mecanismo:**
  - **Outbox** no producer e **inbox** no consumer para garantir entrega e idempotencia.
  - **Batch** para reconciliacao diaria em caso de falhas.
- **Risco:** latencia e consistencia eventual; monitorar atraso e aplicar reprocessamento.

## 4) Servers (bancos em servidores diferentes)
**Contexto:** cada servico tem seu database em servidores diferentes.
- **Padrao sugerido:** mesma modelagem comum, com replicacao assincromna e robusta.
- **Replicacao:** eventos assincronos obrigatorios; considerar CDC + fila se houver alto volume.
- **Mecanismo:**
  - **Outbox + broker** (Kafka/Rabbit) e contratos de eventos versionados.
  - **Reprocessamento** por offset/versao para recuperacao.
- **Risco:** falhas de rede e inconsistencias temporarias; exigir observabilidade (lag, retries, DLQ).

## Regras comuns para todos os cenarios
- **Ownership:** Graduacao/Pos produzem Pessoa e VinculoAcademico; Diplomas/Assinatura consomem.
- **Idempotencia:** aplicar por `id` + `versao`/`timestamp`.
- **Auditoria:** manter historico de VinculoAcademico quando houver mudanca de status/curso.

## Fluxos por cenario (tradicional vs eventos)
**Legenda rapida:** "Tradicional" = recursos nativos do PostgreSQL; "Eventos" = Kafka + outbox/inbox.

### 1) Simples (mesmo BD e mesmos schemas)
- **Tradicional (triggers/procedures):**
  1. INSERT/UPDATE nas tabelas de aluno/professor.
  2. Trigger chama `sync_vinculo_academico(...)`.
  3. UPSERT direto em `vinculo_academico`.
- **Eventos (outbox + Kafka, mesmo DB):**
  1. Aplicacao grava dados + evento na `outbox_eventos` na mesma transacao.
  2. Worker publica no Kafka.
  3. Consumer aplica UPSERT em `vinculo_academico` (mesmo schema).

### 2) Schema (mesmo BD, schemas distintos)
- **Tradicional (triggers/procedures cross-schema):**
  1. INSERT/UPDATE no schema produtor.
  2. Trigger grava no `schema_consumidor.vinculo_academico`.
  3. Leitura no schema consumidor.
- **Eventos (outbox + Kafka, mesmo DB):**
  1. Aplicacao grava dados + outbox no schema produtor.
  2. Worker publica no Kafka.
  3. Consumer aplica no schema consumidor com inbox para idempotencia.

### 3) Databases (bancos distintos no mesmo servidor)
- **Tradicional (logical replication):**
  1. Publisher cria publication com tabelas fonte.
  2. Subscriber aplica mudancas no DB consumidor.
  3. Ajustes de schema/DDL e sequencias feitos manualmente.
- **Eventos (outbox + Kafka, DBs distintos):**
  1. Aplicacao grava dados + outbox no DB produtor.
  2. Worker publica no Kafka.
  3. Consumer aplica no DB consumidor com inbox e retry.

### 4) Servers (bancos em servidores diferentes)
- **Tradicional (logical replication):**
  1. Publisher e subscriber em servidores distintos.
  2. Replicacao logica aplica mudancas no destino.
  3. Observabilidade para lag e falhas de rede.
- **Eventos (outbox + Kafka, servidores diferentes):**
  1. Aplicacao grava dados + outbox no produtor.
  2. Kafka distribui para consumidores.
  3. Consumer aplica no destino com DLQ e reprocessamento.

## Matriz comparativa (por cenario)
| Cenario | Tradicional (PostgreSQL) | Eventos (Kafka) | Riscos principais |
| --- | --- | --- | --- |
| 1) Simples | Triggers + procedures no mesmo schema | Outbox + Kafka + consumer local | Acoplamento vs overhead de eventos |
| 2) Schema | Triggers cross-schema | Outbox no produtor + consumer no schema alvo | Divergencia de schemas; idempotencia |
| 3) Databases | Logical replication entre DBs | Outbox + Kafka + consumer no DB alvo | Latencia e consistencia eventual |
| 4) Servers | Logical replication entre servidores | Outbox + Kafka + consumer remoto | Falhas de rede; observabilidade |

## Tecnicas de preenchimento do VinculoAcademico por cenario
**Gatilhos comuns:** sempre que houver INSERT/UPDATE em `AlunoGraduacao`, `ProfessorGraduacao`, `AlunoPosGraduacao`, `ProfessorPosGraduacao`.

### 1) Simples (mesmo BD e mesmos schemas)
- **Trigger SQL:** triggers em tabelas de aluno/professor que fazem UPSERT direto em `vinculo_academico`.
- **Stored procedure:** procedure unica `sync_vinculo_academico(...)` chamada por triggers ou pela aplicacao.
- **Batch leve:** job periodico para reconciliar inconsistencias (recalcular vinculos a partir das tabelas fonte).

### 2) Schema (mesmo BD, schemas distintos por servico)
- **Trigger + cross-schema:** trigger no schema produtor escreve em tabela de vinculacao do schema consumidor (via `schema_alvo.vinculo_academico`).
- **View + materializacao:** view no schema consumidor sobre as tabelas do produtor e job para materializar em `vinculo_academico`.
- **Eventos internos:** tabela outbox no schema produtor e job que aplica no schema consumidor.

### 3) Databases (bancos distintos no mesmo servidor)
- **Outbox/InBox:** outbox no DB do produtor com eventos `VinculoAcademicoCriado/Atualizado`; consumer aplica no seu DB.
- **CDC por DB:** capture de mudancas nas tabelas de aluno/professor e replicacao para `vinculo_academico` no DB alvo.
- **Batch cross-DB:** job que consulta o DB produtor e faz UPSERT no DB consumidor (via conexao federada/app).

### 4) Servers (bancos em servidores diferentes)
- **Eventos assincronos:** outbox + broker (Kafka/Rabbit) para propagar mudancas de aluno/professor.
- **CDC + streaming:** CDC no produtor publica mudancas, consumidor consolida `vinculo_academico`.
- **Reprocessamento:** fila de retry e DLQ para garantir entrega em falhas de rede.

## CDC no PostgreSQL
**Onde o CDC se encaixa:** ideal para cenarios 2, 3 e 4 quando voce quer capturar mudancas nas tabelas de aluno/professor sem depender de triggers na aplicacao. No cenario 1 (mesmo schema), CDC costuma ser desnecessario.

**CDC recomendado (padrao de mercado):**
- **Debezium + PostgreSQL logical decoding** (Kafka Connect): boa observabilidade, esquema de eventos bem definido, suporte amplo e comunidade forte.

**Alternativas validas:**
- **pgoutput + consumidor proprio**: baixo nivel, exige app para consumir o WAL.
- **wal2json**: simples para prototipo, mas menos robusto para escala/operacao.

**Sugestao pratica:**
- Habilitar logical replication no Postgres (WAL em `logical`).
- Criar publicacao para as tabelas fonte (Aluno/Professor).
- Consumidor transforma eventos em UPSERT de `vinculo_academico` no destino.
