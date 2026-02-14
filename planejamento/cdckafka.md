# 4.2 CDC + Kafka com Connect Sink (foco na tabela `pessoa`)
[← Voltar ao indice](./README.md)

## 1. Introducao

Este documento descreve uma estrategia de replicacao de dados usando **CDC + Kafka** sem alterar os servicos de negocio.

A ideia principal e:

1. Capturar mudancas na tabela `pessoa` direto do PostgreSQL (WAL logical decoding) com Debezium.
2. Publicar eventos tecnicos no Kafka.
3. Aplicar os eventos nos bancos de destino com Kafka Connect JDBC Sink.

Fluxo resumido:

```text
PostgreSQL origem -> Debezium Source -> Topico Kafka -> JDBC Sink -> PostgreSQL destino
```

Esse modelo reduz acoplamento entre servicos e banco, mantendo consistencia eventual para read models locais.

---

## 2. Quando usar

- Quando o objetivo e replicacao tecnica de tabelas (insert/update/delete).
- Quando se quer evitar mudanca de codigo nos servicos.
- Quando existe mais de um consumidor para o mesmo dado (`pessoa` em diplomas, assinatura, etc.).

Nao e o melhor caminho quando a replicacao exige regra de negocio complexa por evento (nesse caso, consumidor no servico tende a ser melhor).

---

## 3. Premissas para `pessoa`

- Definir ownership inicial de `pessoa` (idealmente uma origem principal nesta fase).
- Garantir chave primaria canonica (`id` numerico long) em origem e destino.
- Definir campos minimos replicados (evitar excesso de PII/LGPD).
- Definir comportamento de delete (delete fisico vs soft delete).
- Definir regra de conflito se houver mais de uma origem editando `pessoa`.

---

## 4. Passo a passo de implementacao

1. **Mapear origem e destinos**
   - Ex.: origem `graduacao.public.pessoa`; destinos `diplomas.public.pessoa` e `assinatura.public.pessoa`.

2. **Preparar schema no destino**
   - Criar tabela `pessoa` com PK e tipos compativeis.
   - Evitar diferencas de tipo/nullable entre origem e destino.

3. **Configurar PostgreSQL de origem para CDC**
   - `wal_level=logical`
   - Ajustar `max_replication_slots` e `max_wal_senders`
   - Criar usuario de replicacao com permissoes adequadas
   - Ajustar `pg_hba.conf` para acesso do Connect

4. **Subir Kafka + Kafka Connect + plugins**
   - Debezium PostgreSQL Connector
   - JDBC Sink Connector

5. **Criar Debezium Source da tabela `pessoa`**
   - Filtrar apenas `public.pessoa`
   - Definir `topic.prefix`, `slot.name`, `publication.name`
   - Definir `snapshot.mode=initial` para carga inicial

6. **Aplicar transformacao do payload (unwrap)**
   - Converter envelope Debezium em registro plano para o sink

7. **Criar um JDBC Sink por banco de destino**
   - `insert.mode=upsert`
   - `pk.mode=record_key`
   - `pk.fields=id`
   - `delete.enabled=true` (se replicar delete)

8. **Executar validacao funcional**
   - Testar insert/update/delete na origem
   - Validar convergencia no destino

9. **Configurar observabilidade**
   - Status de connector/task
   - Lag de consumidor
   - Erros de escrita no sink
   - Crescimento de WAL/slot

10. **Definir rotina operacional**
    - Reprocessamento
    - Pausa/restart de conectores
    - Procedimento para evolucao de schema

---

## 5. Configuracoes minimas necessarias

### 5.1 PostgreSQL origem

- `wal_level=logical`
- `max_wal_senders` >= numero de conectores CDC
- `max_replication_slots` >= numero de conectores CDC
- Usuario com:
  - `LOGIN`
  - `REPLICATION`
  - `SELECT` na tabela `public.pessoa`
- `pg_hba.conf` permitindo conexao do Connect

### 5.2 Debezium Source (minimo)

```json
{
  "name": "src-pessoa-graduacao",
  "config": {
    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
    "tasks.max": "1",
    "database.hostname": "postgres",
    "database.port": "5432",
    "database.user": "debezium",
    "database.password": "***",
    "database.dbname": "graduacao",

    "topic.prefix": "tcc.graduacao",
    "plugin.name": "pgoutput",
    "slot.name": "dbz_slot_pessoa_grad",
    "slot.drop.on.stop": "false",

    "publication.name": "dbz_pub_pessoa_grad",
    "publication.autocreate.mode": "filtered",
    "table.include.list": "public.pessoa",

    "snapshot.mode": "initial",

    "transforms": "unwrap",
    "transforms.unwrap.type": "io.debezium.transforms.ExtractNewRecordState"
  }
}
```

### 5.3 JDBC Sink (minimo)

```json
{
  "name": "sink-pessoa-diplomas",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
    "tasks.max": "1",
    "topics": "tcc.graduacao.public.pessoa",

    "connection.url": "jdbc:postgresql://postgres:5432/diplomas",
    "connection.user": "tcc",
    "connection.password": "***",

    "insert.mode": "upsert",
    "pk.mode": "record_key",
    "pk.fields": "id",
    "delete.enabled": "true",

    "table.name.format": "public.pessoa",
    "auto.create": "false",
    "auto.evolve": "false"
  }
}
```

> Para mais de um destino, criar um sink por destino (ex.: `sink-pessoa-assinatura`).

---

## 6. Cuidados importantes

- **Evitar loop de replicacao:** nao capturar no CDC tabelas que o sink escreve no mesmo fluxo.
- **Idempotencia:** garantir upsert por PK `id`.
- **Ordenacao por chave:** manter key por `id` para estabilidade por entidade.
- **DDL nao vem no CDC:** mudancas de schema devem ser tratadas por migration.
- **Conflito multi-writer:** se mais de uma origem atualiza `pessoa`, formalizar regra de precedencia.

---

## 7. Checklist rapido

- [ ] Ownership de `pessoa` definido
- [ ] Campos replicados definidos
- [ ] Postgres com logical replication habilitado
- [ ] Debezium Source criado e saudavel
- [ ] JDBC Sink criado e saudavel
- [ ] CRUD validado ponta-a-ponta
- [ ] Monitoramento de lag/erros ativo
- [ ] Rotina de reprocessamento documentada
