# Replicação por cenários de banco
[← Voltar ao índice](./README.md)

Este documento descreve sugestões de replicação de dados para o VínculoAcadêmico e demais entidades comuns em quatro cenários de infraestrutura.

## Objetivo da simulação
Comparar, em **estratégia faseada**, três arquiteturas de replicação: **DB Based**, **Kafka + CDC** e **EDA + Kafka**. A simulação busca demonstrar que as abordagens baseadas em eventos podem manter desempenho e confiabilidade sem perda, ao mesmo tempo em que oferecem maior flexibilidade e desacoplamento do modelo de dados.

## Eixo arquitetural aplicado aos cenários
Além do eixo de infraestrutura (cenários 1-4), este documento usa o eixo arquitetural definido em [`arquiteturas.md`](./arquiteturas.md):
- **DB Based:** integração via recursos nativos do banco.
- **CDC+Kafka:** captura de mudanças do banco e publicação em tópicos.
- **EDA com Kafka:** publicação de eventos de domínio pela aplicação (outbox/inbox).

A comparação formal deve sempre ocorrer dentro do mesmo cenário, variando apenas a arquitetura.
**Observação do experimento:** o cenário 1 (C1) é usado apenas como baseline de construção (C1A1). As combinações C1A2 e C1A3 serão omitidas.

## 1) Simples (mesmo BD e mesmos schemas)
**Contexto:** todos os serviços compartilham o mesmo banco e os mesmos schemas.
- **Padrão sugerido:** modelo único e tabelas comuns únicas (Pessoa, VínculoAcadêmico, Contato, Endereço, DocumentoIdentificação).
- **Replicação:** não há replicação física; há apenas **ownership lógico** (Graduação/Pós produzem; Diplomas/Assinatura consomem).
- **Integração:** leitura direta por FK e consultas compartilhadas; use visões para separar leitura por serviço, se necessário.
- **Risco:** acoplamento forte e concorrência; exigir contratos claros de escrita (somente produtores oficiais atualizam).
- **Uso no experimento:** baseline de construção (C1A1). C1A2/C1A3 não serão executados.

## 2) Schema (mesmo BD, schemas distintos por serviço)
**Contexto:** um banco único com schemas separados por serviço.
- **Padrão sugerido:** tabelas comuns duplicadas em cada schema com o mesmo modelo.
- **Replicação:** eventos ou jobs de sincronização para copiar dados dos schemas produtores para schemas consumidores.
- **Mecanismo:** 
  - **Evento** (preferencial): publicar `PessoaCriada/Atualizada`, `VinculoAcademicoCriado/Atualizado` e aplicar no schema alvo.
  - **CDC/stream** (opcional): replicar mudanças do schema produtor para consumidores.
- **Risco:** divergência entre schemas; usar idempotência por `id` e `timestamp/versao`.

## 3) Databases (bancos distintos no mesmo servidor)
**Contexto:** cada serviço possui seu próprio database no mesmo servidor Postgres.
- **Padrão sugerido:** mesmo modelo comum em cada database, com ownership por serviço.
- **Replicação:** eventos assíncronos (Kafka, filas, ou jobs) para manter read models alinhados.
- **Mecanismo:**
  - **Outbox** no producer e **inbox** no consumer para garantir entrega e idempotência.
  - **Batch** para reconciliação diária em caso de falhas.
- **Risco:** latência e consistência eventual; monitorar atraso e aplicar reprocessamento.

## 4) Servers (bancos em servidores diferentes)
**Contexto:** cada serviço tem seu database em servidores diferentes.
- **Padrão sugerido:** mesma modelagem comum, com replicação assíncrona e robusta.
- **Replicação:** eventos assíncronos obrigatórios; considerar CDC + fila se houver alto volume.
- **Mecanismo:**
  - **Outbox + broker** (Kafka/Rabbit) e contratos de eventos versionados.
  - **Reprocessamento** por offset/versao para recuperação.
- **Risco:** falhas de rede e inconsistências temporárias; exigir observabilidade (lag, retries, DLQ).

## Regras comuns para todos os cenários
- **Ownership:** Graduação/Pós produzem Pessoa e VínculoAcadêmico; Diplomas/Assinatura consomem.
- **Idempotência:** aplicar por `id` + `versao`/`timestamp`.
- **Auditoria:** manter histórico de VínculoAcadêmico quando houver mudança de status/curso.

## Fluxo de diploma (simplificado com entidades existentes)
Este fluxo considera **apenas** as entidades já existentes nos serviços (sem matrícula).

### Entidades envolvidas (já existentes)
- **Graduação/Pós (produtores):** `Pessoa`, `DocumentoIdentificacao`, `Contato`, `Endereco`, `VinculoAcademico` (com `CursoProgramaReferencia`, `TipoVinculo`, `SituacaoAcademica`), `DocumentoOficialGraduacao` / `DocumentoOficialPos`.
- **Diplomas:** `RequerimentoDiploma`, `BaseEmissaoDiploma`, `StatusEmissao`, `Diploma`, `DocumentoDiploma`.
- **Assinatura:** `DocumentoDiploma` (espelho com `diploma_id`), `DocumentoOficial` (espelho dos documentos oficiais), `DocumentoAssinavel`, `SolicitacaoAssinatura`, `Assinatura`, `ManifestoAssinatura`.

### Disparo do requerimento (Grad/Pós → Diplomas)
- **Gatilho:** `VinculoAcademico.situacao = CONCLUIDO` com `dataConclusao` preenchida.
- **Ação:** criação de `RequerimentoDiploma` com `pessoa_id` e `vinculo_id`.

### Emissão e assinatura (Diplomas → Assinatura)
- `RequerimentoDiploma` gera `BaseEmissaoDiploma` (snapshot de pessoa/curso/datas).
- `StatusEmissao` inicia em `SOLICITADO`, evolui para `EMITIDO` e `ASSINADO`.
- Ao emitir, `Diploma` e `DocumentoDiploma` (versão inicial) são gerados automaticamente.
- Assinatura consome `DocumentoDiploma` e cria `DocumentoAssinavel`.
- Criação de `SolicitacaoAssinatura` ocorre somente se **não houver** solicitação ativa/concluída para o mesmo documento.
- Ao criar a solicitação, uma `Assinatura` é gerada em `PENDENTE`; ao assinar, gera `ManifestoAssinatura`.
- `SolicitacaoAssinatura` concluída atualiza o `StatusEmissao` para `ASSINADO`; rejeição cancela o fluxo e permite nova solicitação.

### Documentos oficiais (Grad/Pós → Assinatura)
- Grad/Pós criam `DocumentoOficialGraduacao` / `DocumentoOficialPos` (ex.: histórico escolar, atas, atestados).
- O documento é espelhado em `DocumentoOficial` e gera `DocumentoAssinavel`.
- É aberta `SolicitacaoAssinatura` quando não existe solicitação ativa/concluída para o mesmo documento.
- A solicitação cria uma `Assinatura` em `PENDENTE`; ao assinar, gera `ManifestoAssinatura`.
- Solicitações `CANCELADA`/`REJEITADA` permitem nova solicitação; `CONCLUIDA` bloqueia novas.

### Disponibilização no Grad/Pós (leitura)
- Grad/Pós considera o diploma “disponível” quando:
  - existe `Diploma` para o `RequerimentoDiploma`, **e**
  - `StatusEmissao = ASSINADO`, **e**
  - há ao menos um `DocumentoDiploma` associado.
- A leitura pode ser direta (cenário 1), por replicação (cenários 2–4) ou por view/materialização.

## Fluxos por cenário (DB Based, CDC+Kafka e EDA+Kafka)
**Legenda rápida:** DB Based = recursos nativos do PostgreSQL; CDC+Kafka = captura de mudanças do banco; EDA+Kafka = eventos de domínio publicados pela aplicação.

### 1) Simples (mesmo BD e mesmos schemas)
- **DB Based (baseline C1A1):**
  1. Escrita direta no mesmo schema (sem replicação física).
  2. Se necessário, sincronização interna local (ex.: procedure/view) para consolidar `vinculo_academico`.
- **CDC+Kafka (C1A2):** não executado no experimento.
- **EDA+Kafka (C1A3):** não executado no experimento.

### 2) Schema (mesmo BD, schemas distintos)
- **DB Based (triggers/procedures cross-schema):**
  1. INSERT/UPDATE no schema produtor.
  2. Trigger grava no `schema_consumidor.vinculo_academico`.
  3. Leitura no schema consumidor.
- **CDC+Kafka (mesmo DB):**
  1. CDC captura mudanças no schema produtor.
  2. Kafka distribui eventos técnicos para consumidores.
  3. Consumer aplica no schema consumidor com inbox para idempotência.
- **EDA+Kafka (outbox + Kafka, mesmo DB):**
  1. Aplicação grava dados + outbox no schema produtor.
  2. Worker publica no Kafka.
  3. Consumer aplica no schema consumidor com inbox para idempotência.

### 3) Databases (bancos distintos no mesmo servidor)
- **DB Based (logical replication):**
  1. Publisher cria publication com tabelas fonte.
  2. Subscriber aplica mudanças no DB consumidor.
  3. Ajustes de schema/DDL e sequencias feitos manualmente.
- **CDC+Kafka (DBs distintos):**
  1. CDC captura mudanças no DB produtor.
  2. Kafka distribui eventos técnicos.
  3. Consumer aplica no DB consumidor com inbox e retry.
- **EDA+Kafka (outbox + Kafka, DBs distintos):**
  1. Aplicação grava dados + outbox no DB produtor.
  2. Worker publica no Kafka.
  3. Consumer aplica no DB consumidor com inbox e retry.

### 4) Servers (bancos em servidores diferentes)
- **DB Based (logical replication):**
  1. Publisher e subscriber em servidores distintos.
  2. Replicação lógica aplica mudanças no destino.
  3. Observabilidade para lag e falhas de rede.
- **CDC+Kafka (servidores diferentes):**
  1. CDC captura mudanças no produtor.
  2. Kafka distribui eventos técnicos para consumidores remotos.
  3. Consumer aplica no destino com DLQ e reprocessamento.
- **EDA+Kafka (outbox + Kafka, servidores diferentes):**
  1. Aplicação grava dados + outbox no produtor.
  2. Kafka distribui para consumidores.
  3. Consumer aplica no destino com DLQ e reprocessamento.

## Matriz comparativa (por cenario)
| Cenario | DB Based (PostgreSQL) | CDC+Kafka | EDA+Kafka | Riscos principais |
| --- | --- | --- | --- | --- |
| 1) Simples | **Baseline (C1A1)**: escrita direta no mesmo schema (sem replicação física) | **N/A** (C1A2 omitido) | **N/A** (C1A3 omitido) | Acoplamento vs overhead operacional |
| 2) Schema | Triggers cross-schema | CDC no schema produtor + consumer no schema alvo | Outbox no produtor + consumer no schema alvo | Divergência de schemas; idempotência |
| 3) Databases | Logical replication entre DBs | CDC no DB produtor + consumer no DB alvo | Outbox + Kafka + consumer no DB alvo | Latência e consistência eventual |
| 4) Servers | Logical replication entre servidores | CDC remoto + Kafka + consumer remoto | Outbox + Kafka + consumer remoto | Falhas de rede; observabilidade |

## Técnicas de preenchimento do VínculoAcadêmico por cenário
**Gatilhos comuns:** sempre que houver INSERT/UPDATE em `AlunoGraduacao`, `ProfessorGraduacao`, `AlunoPosGraduacao`, `ProfessorPosGraduacao`.

### 1) Simples (mesmo BD e mesmos schemas)
- **Observação:** no C1 o preenchimento é local (mesmo schema) e não configura replicação entre serviços.
- **Trigger SQL:** triggers em tabelas de aluno/professor que fazem UPSERT direto em `vinculo_academico`.
- **Stored procedure:** procedure única `sync_vinculo_academico(...)` chamada por triggers ou pela aplicação.
- **Batch leve:** job periódico para reconciliar inconsistências (recalcular vínculos a partir das tabelas fonte).

### 2) Schema (mesmo BD, schemas distintos por serviço)
- **Trigger + cross-schema:** trigger no schema produtor escreve em tabela de vinculação do schema consumidor (via `schema_alvo.vinculo_academico`).
- **View + materialização:** view no schema consumidor sobre as tabelas do produtor e job para materializar em `vinculo_academico`.
- **Eventos internos:** tabela outbox no schema produtor e job que aplica no schema consumidor.

### 3) Databases (bancos distintos no mesmo servidor)
- **Outbox/InBox:** outbox no DB do produtor com eventos `VinculoAcademicoCriado/Atualizado`; consumer aplica no seu DB.
- **CDC por DB:** capture de mudanças nas tabelas de aluno/professor e replicação para `vinculo_academico` no DB alvo.
- **Batch cross-DB:** job que consulta o DB produtor e faz UPSERT no DB consumidor (via conexao federada/app).

### 4) Servers (bancos em servidores diferentes)
- **Eventos assíncronos:** outbox + broker (Kafka/Rabbit) para propagar mudanças de aluno/professor.
- **CDC + streaming:** CDC no produtor publica mudanças, consumidor consolida `vinculo_academico`.
- **Reprocessamento:** fila de retry e DLQ para garantir entrega em falhas de rede.

## CDC no PostgreSQL
**Onde o CDC se encaixa:** ideal para cenários 2, 3 e 4 quando você quer capturar mudanças nas tabelas de aluno/professor sem depender de triggers na aplicação. No cenário 1 (mesmo schema), CDC costuma ser desnecessário.

**CDC recomendado (padrão de mercado):**
- **Debezium + PostgreSQL logical decoding** (Kafka Connect): boa observabilidade, esquema de eventos bem definido, suporte amplo e comunidade forte.

**Alternativas validas:**
- **pgoutput + consumidor próprio**: baixo nível, exige app para consumir o WAL.
- **wal2json**: simples para protótipo, mas menos robusto para escala/operação.

**Sugestão prática:**
- Habilitar logical replication no Postgres (WAL em `logical`).
- Criar publicação para as tabelas fonte (Aluno/Professor).
- Consumidor transforma eventos em UPSERT de `vinculo_academico` no destino.
