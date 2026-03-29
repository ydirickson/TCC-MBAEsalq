# Pendências do Projeto TCC-MBAEsalq

Este arquivo descreve as tarefas pendentes de implementação, organizadas por prioridade.
Use-o como guia para o Claude Code: cada tarefa tem contexto suficiente para ser implementada
de forma autônoma, referenciando os arquivos existentes como modelo.

---

## Contexto Geral

O projeto compara 3 arquiteturas de replicação de dados em 4 cenários (C1–C4).
O escopo atual cobre: **C1 (baseline)**, **C2A1**, **C2A2** e **C2A3**.

- **C1** e **C2A1**: implementados e com resultados coletados.
- **C2A2**: implementado, mas com bug crítico (ver Prioridade 2).
- **C2A3**: não implementado (ver Prioridade 1).

Stack: Java 21, Spring Boot 3.x, PostgreSQL 18, Apache Kafka 4.1.1 (KRaft), Debezium 3.4,
Docker Compose, Grafana K6 + Prometheus + Grafana.

---

## Prioridade 1 — C2A3 (EDA com Apache Kafka) — BLOQUEADOR

O cenário C2A3 é a última arquitetura a ser implementada e testada.
Nenhum arquivo existe ainda para este cenário. Use C2A2 como modelo para a infraestrutura
e os serviços de `graduacao` e `pos-graduacao` como modelo para o código Kafka.

### 1.1 — Código Kafka nos serviços (A3)

Todos os 4 serviços precisam de produtores e consumidores Kafka para os eventos de domínio.
A lógica é: após persistir no banco local, publicar evento no tópico Kafka correspondente.
O consumidor recebe o evento e faz upsert idempotente no schema local (usando `id` como chave).

**Serviço `graduacao` (`servicos/graduacao/`):**
- Produtor: publicar `PessoaCriada` / `PessoaAtualizada` no tópico `tcc.graduacao.pessoa`
  após salvar em `GraduacaoService` (ou similar).
- Produtor: publicar `VinculoAcademicoCriado` / `VinculoAcademicoAtualizado` no tópico
  `tcc.graduacao.vinculo_academico`.
- Consumidor: consumir `tcc.pos_graduacao.pessoa` e fazer upsert em `graduacao.pessoa`
  (para replicação cruzada de Pessoa).

**Serviço `pos-graduacao` (`servicos/pos-graduacao/`):**
- Produtor: publicar `PessoaCriada` / `PessoaAtualizada` no tópico `tcc.pos_graduacao.pessoa`.
- Produtor: publicar `VinculoAcademicoCriado` / `VinculoAcademicoAtualizado` no tópico
  `tcc.pos_graduacao.vinculo_academico`.
- Consumidor: consumir `tcc.graduacao.pessoa` e fazer upsert em `pos_graduacao.pessoa`.

**Serviço `diplomas` (`servicos/diplomas/`):**
- Este serviço não tem Kafka. Adicionar dependência `spring-kafka` no `pom.xml`
  (modelo: verificar `servicos/pom.xml` do parent).
- Consumidor: consumir `tcc.graduacao.pessoa` e `tcc.pos_graduacao.pessoa`,
  fazer upsert em `diplomas.pessoa`.
- Consumidor: consumir `tcc.graduacao.vinculo_academico` e `tcc.pos_graduacao.vinculo_academico`,
  fazer upsert em `diplomas.vinculo_academico`.
- Consumidor: consumir evento de `ConclusaoPublicada` (de graduacao ou pos-graduacao)
  e criar automaticamente um `RequerimentoDiploma`.

**Serviço `assinatura` (`servicos/assinatura/`):**
- Este serviço não tem Kafka. Adicionar dependência `spring-kafka` no `pom.xml`.
- Consumidor: consumir `tcc.graduacao.pessoa` e `tcc.pos_graduacao.pessoa`,
  fazer upsert em `assinatura.pessoa`.
- Consumidor: consumir `tcc.graduacao.vinculo_academico` e `tcc.pos_graduacao.vinculo_academico`,
  fazer upsert em `assinatura.vinculo_academico`.
- Atenção: o modelo `DocumentoAssinavel` em `assinatura` atualmente só referencia
  `DocumentoDiploma` (ver `domain/model/DocumentoAssinavel.java`). Para A3, adicionar
  suporte a `documento_oficial` (graduação e pós) também, criando campo polimórfico
  ou nova entidade `DocumentoOficialReferencia`.

**Padrão de implementação:**
- Usar `KafkaTemplate<String, Object>` com serialização JSON.
- `@KafkaListener` para os consumidores, com `groupId` por serviço.
- Idempotência: usar `ON CONFLICT (id) DO UPDATE SET ...` via `@Upsert` ou query nativa.
- Publicar eventos dentro da transação do serviço (sem Outbox — limitação documentada no TCC).
- Configurar Kafka via `application.yml` com variáveis de ambiente:
  ```yaml
  spring:
    kafka:
      bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
      producer:
        key-serializer: org.apache.kafka.common.serialization.StringSerializer
        value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      consumer:
        group-id: ${spring.application.name}
        key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
        value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
        properties:
          spring.json.trusted.packages: "*"
  ```

### 1.2 — Scripts de BD para C2A3

Criar pasta `bd/c2a3/` com scripts SQL.
Modelo: copiar de `bd/c2a2/` e remover quaisquer triggers/procedures de sincronização —
no A3, a replicação é feita pela aplicação, não pelo banco.

Arquivos necessários (mesma numeração do c2a2):
- `00_init.sql` — criação dos schemas: `graduacao`, `pos_graduacao`, `diplomas`, `assinatura`
- `01_graduacao.sql` — tabelas do schema `graduacao` (sem triggers de sync)
- `02_pos_graduacao.sql` — tabelas do schema `pos_graduacao` (sem triggers de sync)
- `03_diplomas.sql` — tabelas do schema `diplomas` (sem triggers de sync)
- `04_assinatura.sql` — tabelas do schema `assinatura` (sem triggers de sync)
- Seeds: copiar todos os `*_seed_*.sql` de `bd/c2a2/` sem alteração

### 1.3 — Docker Compose para C2A3

Criar `docker-compose-c2a3.yml` na raiz do projeto.
Modelo: copiar `docker-compose-c2a2.yml` e:
- Remover os serviços `kafka-connect` e `connect-init` (não há conectores Debezium no A3).
- Manter o broker Kafka (`kafka`) — os serviços Spring se conectam diretamente a ele.
- Ajustar a variável `BD_CENARIO=c2a3` no `.env` correspondente.
- Os 4 serviços Spring precisam receber a variável `KAFKA_BOOTSTRAP_SERVERS=kafka:9092`.
- Criar `.env.c2a3` baseado em `.env.c2a2`, ajustando `BD_CENARIO` e removendo
  variáveis de Kafka Connect.

Adicionar suporte ao cenário `c2a3` no script `simulacao.sh` (seguir o padrão dos outros
cenários já implementados no arquivo).

### 1.4 — Script k6 para C2A3 e coleta de resultados

O script `monitoramento/k6/scripts/replication-batch-latency.js` já existe e deve funcionar
para C2A3 sem modificações (ele é agnóstico à arquitetura — só chama a API REST e verifica
a replicação consultando os destinos).

Após rodar o teste:
- Criar `monitoramento/resultados/replicacao-batch-c2a3.md` seguindo o template em
  `monitoramento/resultados/replicacao-batch-template.md`.
- Atualizar `monitoramento/resultados/analise-comparativa-replicacao-batch-c1-c2a1-c2a2.md`
  para incluir C2A3 na comparação (renomear para `...-c2a3.md` ou criar novo arquivo).

---

## Prioridade 2 — Bug no C2A2: FK violation no sink de `vinculo_academico`

**Problema documentado em:**
`monitoramento/resultados/analise-comparativa-replicacao-batch-c1-c2a1-c2a2.md`
(seção "Consideracao Especifica: Problema de Vinculo no C2A2")

**Causa:** o sink JDBC de `vinculo_academico` tenta inserir o registro antes de `pessoa`
existir no schema destino, violando a FK `vinculo_academico.pessoa_id_fkey`.

**Arquivos afetados:**
- `infra/kafka-connect/connectors/sink-vinculo-graduacao-assinatura.json`
- `infra/kafka-connect/connectors/sink-vinculo-graduacao-diplomas.json`
- `infra/kafka-connect/connectors/sink-vinculo-pos-assinatura.json`
- `infra/kafka-connect/connectors/sink-vinculo-pos-diplomas.json`

**Correções a implementar nos 4 arquivos de sink de vinculo:**

```json
"errors.retry.timeout": "300000",
"errors.retry.delay.max.ms": "10000",
"errors.tolerance": "none",
"max.retries": "30",
"retry.backoff.ms": "5000"
```

Estes parâmetros fazem o sink tentar novamente por até 5 minutos com backoff de 5s,
dando tempo para o sink de `pessoa` processar antes. Esta é a correção de curto prazo.

Também validar se o script de inicialização em `infra/kafka-connect/scripts/` verifica
o status dos connectors e reinicia tasks com `FAILED` antes de declarar sucesso.

---

## Prioridade 3 — Pendências do C2A2 (A2 — Debezium CDC)

Estas pendências completam o A2, mas são secundárias ao C2A3.

### 3.1 — Replicação de `documento_identificacao`, `contato` e `endereco` no A2

Faltam connectors source e sink para essas tabelas em todos os schemas.
Modelo: `infra/kafka-connect/connectors/source-graduacao-pessoa.json`.

Criar:
- `source-graduacao-documento-identificacao.json` — captura `graduacao.documento_identificacao`
- `sink-documento-identificacao-pos-graduacao.json` — destino `pos_graduacao.documento_identificacao`
- `sink-documento-identificacao-diplomas.json`
- `sink-documento-identificacao-assinatura.json`
- Repetir para `contato` e `endereco`

### 3.2 — Sink de `pos_graduacao.pessoa` → `assinatura.pessoa`

Falta o arquivo `infra/kafka-connect/connectors/sink-pessoa-assinatura-pos.json`.
Modelo: `sink-pessoa-assinatura.json` (que já existe para graduacao → assinatura).
Ajustar `topics` para `tcc.pos_graduacao.pessoa` e `connection.url` para o schema `assinatura`.

### 3.3 — Suporte a `documento_oficial` no modelo de `DocumentoAssinavel` (serviço assinatura)

Arquivo: `servicos/assinatura/src/main/java/br/com/tcc/assinatura/domain/model/DocumentoAssinavel.java`

Atualmente o campo `documentoDiploma` é obrigatório (`nullable = false`). Para suportar
`documento_oficial` de graduação e pós-graduação, tornar o campo `documentoDiploma` opcional
e adicionar campos `documentoOficialGraduacaoId` e `documentoOficialPosId` (nullable),
com uma constraint CHECK garantindo que exatamente um deles seja preenchido.

Também atualizar `DocumentoAssinavelController`, `DocumentoAssinavelService`,
`DocumentoAssinavelRequest`, `DocumentoAssinavelResponse` e o script SQL de `04_assinatura.sql`
do `bd/c2a2/` (e `bd/c2a3/`).

---

## Prioridade 4 — Pendências do C2A1 (A1 — DB Based)

### 4.1 — Automação banco: `documento_oficial_pos` → assinatura

No C2A1, o fluxo de criação de `documento_oficial` na pós-graduação ainda não dispara
automaticamente a criação de `documento_assinavel` e `solicitacao_assinatura` no schema assinatura.

Modelo: ver `bd/c2a1/08_assinatura_sync.sql` (que já faz isso para `documento_diploma`).
Criar trigger/procedure análoga em `bd/c2a1/` para `documento_oficial_pos`.

### 4.2 — Automação banco: `requerimento_diploma` por conclusão (C2A1)

No C2A1, quando `vinculo_academico.situacao = 'CONCLUIDO'`, deveria ser criado automaticamente
um `requerimento_diploma` no schema `diplomas`.
Implementar procedure/trigger em `bd/c2a1/` (análoga ao que existe no C1).

---

## Notas de implementação

- **Commits**: mensagens em português, no imperativo ("Adiciona...", "Corrige...", "Implementa...").
- **Testes**: após cada mudança de infra, validar com `./simulacao.sh up <cenario>` e
  checar `docker compose ps` para confirmar todos os serviços `healthy`.
- **Idempotência Kafka (A3)**: usar `saveOrUpdate` baseado em `id` — nunca `save` puro,
  para suportar reprocessamento sem duplicatas.
- **Não implementar Outbox Pattern**: está fora do escopo do TCC (documentado como trabalho futuro).
- **Não implementar C3/C4**: também fora do escopo (servidores distintos = trabalho futuro).
