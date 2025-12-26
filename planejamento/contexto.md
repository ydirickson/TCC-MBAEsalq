# 1. Contexto e objetivo do experimento
[← Voltar ao índice](./README.md)

Retomando o contexto de uma solução de replicação de dados corporativos baseado em uma arquitetura distribuída, será feito nesse repositório a migração do mecanismo de replicação de um modelo centralizado (*TODO EXPLICAR QUAL*) para um baseado no Apache Kafka, garantindo a replicação de dados atual, com desempenho e confiabilidade, e aumentando a flexibilidade de comunicação entre os serviços. 

Este projeto simula um ecossistema com quatro serviços acadêmicos independentes, cada um com seu próprio domínio e base de dados. Na primeira etapa, esses serviços estarão **acoplados via replicação baseada em banco de dados** (ex.: triggers, procedures, jobs internos, views materializadas, etc.). Na segunda etapa, o acoplamento será reduzido por meio de **replicação assíncrona via Apache Kafka**, visando medir diferenças de desempenho, escalabilidade e impacto arquitetural (latência, consistência, custo operacional, etc.).

Serviços simulados:
1. **Graduação**
2. **Pós-graduação**
3. **Diplomas**
4. **Certificados e assinatura eletrônica**

## Documentos de módulos
- [Conceitos Compartilhados](./modulos/compartilhadas.md)
- [Serviço de Graduação](./modulos/graduacao.md)
- [Serviço de Pós-graduação](./modulos/pos-graduacao.md)
- [Serviço de Diplomas](./modulos/diplomas.md)
- [Serviço de Assinatura Eletrônica](./modulos/assinatura-eletronica.md)
