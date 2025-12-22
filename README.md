# TCC-MBAEsalq — Simulação de Replicação vs Kafka

Repositório do TCC que compara um acoplamento via replicação de banco com uma alternativa desacoplada via Kafka, em um cenário de quatro serviços acadêmicos (graduação, pós, diplomas, assinatura eletrônica).

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
2. Subir infraestrutura (dbs + Kafka): `docker compose up -d` (a definir).
3. Rodar serviços locais (grad/pós/diplomas/assinatura): comandos serão detalhados por serviço.
4. Coleta de métricas: scripts serão adicionados em `scripts/` (a criar).

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
