# 1. Contexto e objetivo do experimento
[← Voltar ao índice](./README.md)

Retomando o contexto de uma solução de replicação de dados corporativos baseado em uma arquitetura distribuída, será feito nesse repositório a migração do mecanismo de replicação de um modelo centralizado em um banco de dados corporativo único, que atua como ponto de coordenação e disseminação das atualizações para os demais sistemas, para um modelo baseado no Apache Kafka, garantindo a replicação de dados atual, com desempenho e confiabilidade, e aumentando a flexibilidade de comunicação entre os serviços.

Este projeto simula um ecossistema com quatro serviços acadêmicos independentes, cada um com seu próprio domínio e base de dados. O objetivo é comparar **três arquiteturas** de replicação em **estratégia faseada**:
1. **DB Based** (replicação via recursos nativos do banco)
2. **Kafka + CDC** (captura de mudanças no banco e distribuição via Kafka)
3. **EDA + Kafka** (eventos de domínio com outbox/inbox)

Serviços simulados:
1. **Graduação**
2. **Pós-graduação**
3. **Diplomas**
4. **Certificados e assinatura eletrônica**

## Documentação de módulos
Os documentos de serviço descrevem as regras e responsabilidades de cada domínio. O documento compartilhado consolida entidades e eventos comuns que precisam permanecer consistentes para viabilizar a replicação entre todos os serviços.

### Serviços
- [Serviço de Graduação](./modulos/graduacao.md)
- [Serviço de Pós-graduação](./modulos/pos-graduacao.md)
- [Serviço de Diplomas](./modulos/diplomas.md)
- [Serviço de Assinatura Eletrônica](./modulos/assinatura-eletronica.md)

### Compartilhado
- [Conceitos Compartilhados](./modulos/compartilhadas.md)
