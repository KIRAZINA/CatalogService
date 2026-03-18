# Catalog Service

This is the catalog-service module for the Smart Content Marketplace project. It provides CRUD operations and basic filtering for digital content in the catalog.

## Prerequisites

Install one of the following:

1. **Local PostgreSQL** for direct host run.
2. **Docker Desktop / Docker Engine** for containerized run.

## Setup and Run

1. Clone the repository.
2. Build locally: `./gradlew build`.
3. Run with PostgreSQL on host: `./gradlew bootRun`.
4. Run with in-memory H2 profile: `./gradlew bootRun --args='--spring.profiles.active=local'`.
5. Run with Docker: `docker compose up --build`.
6. Access API at `http://localhost:8081/catalog`.

## Authentication

- The API uses HTTP Basic authentication.
- Read operations (`GET /catalog/**`) are available to `USER` and `ADMIN`.
- Write operations (`POST`, `PUT`, `DELETE`) are available only to `ADMIN`.
- Default credentials:
  - `catalog_user / userpass`
  - `catalog_admin / adminpass`

## Docker

- `docker-compose.yml` starts `postgres` and `catalog-service` together.
- Docker run uses Spring profile `container`, so the service works against PostgreSQL instead of the local H2 profile.
- The application container reads DB and security settings from environment variables.
- Default Docker database credentials:
  - `postgres / postgres`
- Stop containers with `docker compose down`.
- Stop containers and remove database volume with `docker compose down -v`.

## Configuration

- Database config is driven by `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
- Server port is driven by `SERVER_PORT`.
- Hibernate DDL mode is driven by `DDL_AUTO`.
- Security users are configured by `APP_SECURITY_*` variables.

## Tests

- Unit and integration tests: `./gradlew test`.
- Tests use the in-memory H2 database from `src/test/resources/application-test.yml`.

## Manual Testing Checklist

- [ ] Start the app locally or via Docker.
- [ ] Call `GET /catalog` with `catalog_user` and confirm success.
- [ ] Call `POST /catalog` with `catalog_user` and confirm HTTP 403.
- [ ] Call `POST /catalog` with `catalog_admin` and confirm success.
- [ ] GET /catalog/{id}: Retrieve product.
- [ ] PUT /catalog/{id}: Update product as admin.
- [ ] DELETE /catalog/{id}: Delete product as admin.
- [ ] GET /catalog?query=keyword: Search products.
- [ ] GET /catalog?type=EBOOK: Filter by type.
- [ ] GET /catalog?minPrice=0&maxPrice=1000: Filter by price range.
- [ ] GET /catalog?category=genre: Filter by category.
- [ ] Send invalid payloads and confirm the API returns HTTP 400.
- [ ] Monitor container logs for errors.
