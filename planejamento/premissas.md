# 2. Premissas de modelagem
[← Voltar ao índice](./README.md)

## 2.1. Autonomia por serviço
- Cada serviço possui **banco de dados próprio** e é responsável pelas regras do seu domínio.
- Alguns conceitos aparecem em mais de um serviço (ex.: Pessoa), porém com **propriedade (ownership)** definida e com **cópias locais** (read models) nos demais serviços quando necessário.
- No cenário “acoplado”, essas cópias podem ser mantidas por mecanismos de banco (trigger/ETL interno).
- No cenário “desacoplado”, as cópias serão mantidas por **eventos e tópicos Kafka**, via consumidores que atualizam o modelo local.
- Interfaces seguirão o padrão **RESTful** sempre que possível, com recursos claros e verbos HTTP padrão.

## 2.2. Identificadores e chaves de intersecção
- Identificadores canônicos serão **numéricos (long)**, gerados por sequência/identity do banco; **não usar UUID**. Com isso conseguimos replicar estruturas legadas mais clássicas de banco de dados.
- Devemos **evitar chaves compostas**, preferindo chaves substitutas simples. Não causa impacto na simulação em si e simplifica a construção dos serviços.
- Identificador estável para a entidade **Pessoa**:
  - **PersonId (long)** como chave global.
- Vínculos acadêmicos devem ter identificadores próprios:
  - **AcademicLinkId (long)** para representar um vínculo (ex.: vínculo na graduação, vínculo na pós).
- Cada serviço pode ter seus IDs internos, mas deve persistir também as referências canônicas:
  - Ex.: Diploma guarda `personId` e `academicLinkId` usados como referência.

## 2.3. Mutabilidade e Responsabilidade de Criação
- Nenhuma entidade será estritamente read-only; dados replicados podem sofrer atualização conforme regras específicas.
- Exemplo: **Pessoa** pode ser alterada em mais de um serviço; definiremos regras de conflito/precedência posteriormente.
