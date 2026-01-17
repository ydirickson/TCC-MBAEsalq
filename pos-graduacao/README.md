# Serviço de Pós-graduação (REST)

CRUD simples para Pos-graduacao seguindo MVC com camada de servico, Spring Boot 4.0.1, Java 21, Spring Data JPA e banco PostgreSQL (rodando via Docker). Nao ha camada de seguranca neste servico por nao fazer parte do escopo do teste.

## Como rodar
Pré-requisitos: JDK 21 e Maven.

```bash
mvn spring-boot:run
```

Por padrao o servico sobe em `http://localhost:8082` e usa o PostgreSQL definido em `.env`/`docker-compose.yml` (`POSTGRES_USER=tcc`, `POSTGRES_PASSWORD=tcc123`, `POSTGRES_DB=tccdb`, porta `5432`). Antes de rodar a aplicacao, suba o banco via `docker compose up -d postgres` na raiz do repositorio.

## Documentacao OpenAPI (Swagger)
Com a aplicacao em execucao:
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`

## Endpoints principais
### Programas (`/programas`)
- `POST /programas` — cria programa
  ```json
  {"codigo": "PPG", "nome": "Ciencia de Dados", "cargaHoraria": 360}
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
