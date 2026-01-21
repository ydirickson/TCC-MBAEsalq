# Serviço de Pós-graduação (REST)

CRUD simples para Pós-graduação seguindo MVC com camada de serviço, Spring Boot 4.0.1, Java 21, Spring Data JPA e banco PostgreSQL (rodando via Docker). Não há camada de segurança neste serviço por não fazer parte do escopo do teste.

## Como rodar
Pré-requisitos: JDK 21 e Maven.

```bash
mvn spring-boot:run
```

Por padrão o serviço sobe em `http://localhost:8082` e usa o PostgreSQL definido em `.env`/`docker-compose.yml` (`POSTGRES_USER=tcc`, `POSTGRES_PASSWORD=tcc123`, `POSTGRES_DB=tccdb`, porta `5432`). Antes de rodar a aplicação, suba o banco via `docker compose up -d postgres` na raiz do repositório.

## Documentação OpenAPI (Swagger)
Com a aplicação em execução:
- OpenAPI JSON: `http://localhost:8082/v3/api-docs`
- Swagger UI: `http://localhost:8082/swagger-ui/index.html`

## Endpoints principais
### Programas (`/programas`)
- `POST /programas` — cria programa
  ```json
  {"codigo": "PPG", "nome": "Ciência de Dados", "cargaHoraria": 360}
  ```
- `GET /programas` — lista programas
- `GET /programas/{id}` — busca programa
- `PUT /programas/{id}` — atualiza programa
- `DELETE /programas/{id}` — remove programa

### Alunos (`/alunos`)
- `POST /alunos` — cria aluno (requer programa)
  ```json
  {
    "pessoaId": 1001,
    "programaId": 1,
    "orientadorId": 12,
    "dataMatricula": "2024-03-01",
    "status": "ATIVO"
  }
  ```
- `GET /alunos` — lista alunos
- `GET /alunos/{id}` — busca aluno
- `PUT /alunos/{id}` — atualiza aluno
- `DELETE /alunos/{id}` — remove aluno
