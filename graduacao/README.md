# Serviço de Graduação (REST)

CRUD simples para Graduação seguindo MVC com camada de serviço, Spring Boot 4.0.1, Java 21, Spring Data JPA e banco PostgreSQL (rodando via Docker). Não há camada de segurança neste serviço por não fazer parte do escopo do teste.

## Como rodar
Pré-requisitos: JDK 21 e Maven.

```bash
mvn spring-boot:run
```

Por padrão o serviço sobe em `http://localhost:8081` e usa o PostgreSQL definido em `.env`/`docker-compose.yml` (`POSTGRES_USER=tcc`, `POSTGRES_PASSWORD=tcc123`, `POSTGRES_DB=tccdb`, porta `5432`). Antes de rodar a aplicação, suba o banco via `docker compose up -d postgres` na raiz do repositório.

## Documentacao OpenAPI (Swagger)
Com a aplicacao em execucao:
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`

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
- `POST /alunos` — cria aluno (requer uma turma já cadastrada)  
  ```json
  {
    "pessoaId": 1001,
    "turmaId": 1,
    "dataMatricula": "2024-03-01",
    "status": "ATIVO"
  }
- `GET /alunos` — lista alunos
- `GET /alunos/{id}` — busca aluno
- `PUT /alunos/{id}` — atualiza aluno
- `DELETE /alunos/{id}` — remove aluno
