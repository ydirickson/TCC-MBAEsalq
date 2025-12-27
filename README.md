# TCC-MBA Esalq — Simulação de Replicação vs Kafka

Repositório do meu Trabalho de Conclusão de Curso (TCC) em Engenharia da Computação em 2025 e 2026. O tema deste TCC é propor uma solução de replicação de dados corporativos baseados em uma arquitetura distribuída e _event driven_, utilizando o Apache Kafka como peça central, possibilitando uma flexibildiade maior na comunicação e replicação deste dados com os diversos serviços que os consomem.

## Introdução e Contexto Resumidos

Vindo de um ambiente altamento acoplado de diversos sistemas/serviços corporativos, uma das estratégias mais clássicas para replicação de dados é utilizando algum mecanismo próprios dos Sistemas Gerenciadores de Banco de Dados (SGBDs) que permita que os dados presentes nesses bancos sejam replicados para outros locais de mesmo banco, seja por motivo de redundância, seja por motivos de compartilhamento desses dados.

A partir desse contexto, a ideia é propor uma solução que retire a dependência nas soluções proprietárias e/ou centralizadas desses SGBDs para uma arquitetura distribuída, garantindo assim que os dados sejam replicados da mesma forma e com a mesma segurança e desempenho, ganhando também maior flexibilidade em como os diversos sistemas irão consumir ou utilizar esses dados.

## Visão geral
- Objetivo: medir latência, throughput e impacto arquitetural ao migrar de replicação baseada em banco para replicação por eventos (Kafka).
- Domínio: serviços acadêmicos com entidades compartilhadas (Pessoa, VinculoAcademico) e fluxos de diploma/assinatura.
- Fases: 1) acoplado por replicação de banco; 2) desacoplado por eventos Kafka.

## Estrutura do repositório
- `planejamento/` — documentos de contexto, premissas e regras (ver índice em `planejamento/README.md`).
- `planejamento/modulos/` — visão por serviço e conceitos compartilhados.
- `docs/` — reservado para diagramas/artefatos gerados (a criar).
- `src/` — código da simulação (a criar: serviços grad/pós/diplomas/assinatura).

## Planejamento e regras
- Contexto e decisões estão documentados em `planejamento/README.md`.
- Regras de replicação e eventos canônicos: `planejamento/regras-replicacao.md`.
- Entidades e intersecções: `planejamento/entidades-interseccoes.md`.
- Módulos por serviço: `planejamento/modulos/`.

## Como executar (rascunho)
1. Pré-requisitos esperados: Docker/Docker Compose, JDK 17+, Node 18+ (ajustar quando os serviços forem implementados).
2. Infra inicial (PostgreSQL + pgAdmin): copie `.env.example` para `.env`, ajuste senhas se quiser e rode `docker compose up -d postgres pgadmin`.
3. Postgres ficará em `localhost:${POSTGRES_PORT:-5432}`; pgAdmin em `http://localhost:${PGADMIN_PORT:-8080}` (login padrão em `.env`). pgAdmin já sobe com o servidor `TCC Postgres` cadastrado a partir de `pgadmin/servers.json` (requer volume `pgadmin_data` limpo na primeira vez).
4. Rodar serviços locais (grad/pós/diplomas/assinatura): comandos serão detalhados por serviço.
5. Coleta de métricas: scripts serão adicionados em `scripts/` (a criar).

## Convenções de implementação
- Identificadores numéricos (long) para Pessoa e VinculoAcademico; evitar chaves compostas.
- Eventos em português, em snake_case ou CamelCase consistente com os mapeamentos das libs (definir por linguagem).
- Interfaces REST simples; contratos de evento priorizam id + versão/timestamp para idempotência.

## Guia de contribuição
- Issues/boards: registrar tarefas em Issues; cada PR deve referenciar pelo menos uma Issue.
- Branches: criar branches de feature/hotfix a partir da `main` usando prefixos claros (`feature/`, `fix/`, `doc/`) e nomes descritivos curtos.
- Pull requests:
  - Abrir PR somente após os testes locais passarem.
  - Descrever objetivo, impacto esperado e mudanças de contrato (REST/eventos).
  - Incluir checklist de verificação (testes executados, migrações necessárias).
  - Não fazer squash local; deixar o squash na fusão se aplicável.
- Revisão: pelo menos uma revisão cruzada antes de merge; para ajustes pequenos, comentário explicando por que não há testes.
- Código: comentários sucintos apenas para regras não óbvias; seguir lints/formatters padrão de cada stack.
- Testes: cobrir fluxos de replicação e idempotência; validar happy path e cenários de atraso/duplicidade.
- Commits: mensagens em português, no imperativo (“Adiciona…”, “Ajusta…”); commits pequenos e focados.

## Conduta e organização
- Evitar reverter mudanças de planejamento sem alinhamento prévio.
- Registrar decisões arquiteturais em `planejamento/pontos-abertos.md` ou ADRs futuros.
- Em dúvidas sobre ownership de dados ou contratos de evento, alinhar primeiro no planejamento antes de codificar.

## Plano inicial de tarefas
- Infraestrutura: montar `docker-compose` com bancos (por serviço) e Kafka/ZooKeeper; criar scripts de bootstrap de tópicos.
- Modelagem: definir schema inicial de Pessoa e VinculoAcademico em SQL; tabelas de histórico e pedidos de diploma/assinatura.
- Contratos de eventos: descrever payload mínimo de cada evento em `planejamento/regras-replicacao.md` (id, versão/timestamp, origem).
- Serviços: scaffolds para Graduação, Pós, Diplomas e Assinatura com APIs REST básicas e produtores/consumidores Kafka.
- Replicação acoplada: implementar triggers/jobs para replicar Pessoa/Vínculo/PedidoDiploma/Assinatura na fase 1.
- Replicação desacoplada: implementar produtores/consumidores Kafka para os mesmos fluxos na fase 2.
- Métricas e testes: scripts de carga e medição (latência, throughput); testes de idempotência e de atraso/duplicidade de eventos.
- Topologia de bancos (sugestão): iniciar com **um PostgreSQL e schemas por serviço** para montar rápido triggers/ETL; numa segunda fase, evoluir para **múltiplos PostgreSQL (um por serviço)** para medir latência de rede, isolação e custos operacionais de replicação.
