# Serviço de Graduação (REST)

CRUD simples para Graduação seguindo MVC com camada de serviço, Spring Boot 3.2.3, Java 21, Spring Data JPA e banco H2 em memória. Não há camada de segurança neste serviço por não fazer parte do escopo do teste.

> Observação: Spring Boot 4 ainda não possui release estável; o scaffold usa 3.2.3 (já compatível com Java 21) e pode ser atualizado assim que a versão 4 estiver disponível.

## Como rodar
Pré-requisitos: JDK 21 e Maven.

```bash
mvn spring-boot:run
```

Por padrão o serviço sobe em `http://localhost:8081` e expõe o console do H2 em `/h2-console` (JDBC URL: `jdbc:h2:mem:graduacao`, usuário `sa`, senha `password`).

## Endpoints principais
### Cursos (`/cursos`)
- `POST /cursos` — cria curso  
  ```json
  {"codigo": "BCC", "nome": "Ciencia da Computacao", "cargaHoraria": 3200}
  ```
- `GET /cursos` — lista cursos
- `GET /cursos/{id}` — busca curso
- `PUT /cursos/{id}` — atualiza curso
- `DELETE /cursos/{id}` — remove curso

### Alunos (`/alunos`)
- `POST /alunos` — cria aluno (requer um curso já cadastrado)  
  ```json
  {
    "pessoaId": 1001,
    "cursoId": 1,
    "dataIngresso": "2024-03-01",
    "status": "ATIVO"
  }
  ```
- `GET /alunos` — lista alunos
- `GET /alunos/{id}` — busca aluno
- `PUT /alunos/{id}` — atualiza aluno
- `DELETE /alunos/{id}` — remove aluno
