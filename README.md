# Catalog Service

This is the catalog-service module for the Smart Content Marketplace project. It handles the catalog of digital content with full-text search via Elasticsearch, caching via Redis, and event publishing to Kafka for indexing synchronization.

## Prerequisites

Install the following locally (without Docker):

1. **PostgreSQL**:
    - Download and install from https://www.postgresql.org/.
    - Create a database: `createdb -U postgres catalog_db` (default user: postgres, password: password).
    - Update `application.yml` if credentials differ.

2. **Redis**:
    - Download from https://redis.io/download.
    - Run: `redis-server` (default port 6379).

3. **Kafka + Zookeeper**:
    - Download Kafka from https://kafka.apache.org/downloads.
    - Start Zookeeper: `bin/zookeeper-server-start.sh config/zookeeper.properties`.
    - Start Kafka: `bin/kafka-server-start.sh config/server.properties`.
    - Create topics if needed: `bin/kafka-topics.sh --create --topic topic.content.index --bootstrap-server localhost:9092`.

4. **Elasticsearch**:
    - Download from https://www.elastic.co/downloads/elasticsearch.
    - Run: `bin/elasticsearch` (default port 9200, single-node mode).

## Setup and Run

1. Clone the repository.
2. Configure `application.yml` with your local settings if needed.
3. Build: `./gradlew build`.
4. Run: `./gradlew bootRun`.
5. Access API at http://localhost:8081/catalog.

## Configuration

- Database: PostgreSQL at localhost:5432/catalog_db.
- Elasticsearch: http://localhost:9200.
- Redis: localhost:6379.
- Kafka: localhost:9092.
- For security, configure JWT secret in `application.yml`.

## Tests

- Unit tests: `./gradlew test`.
- Integration tests use Testcontainers (starts isolated instances of Postgres and Elasticsearch).

## Manual Testing Checklist

- [ ] Start all local services (Postgres, Redis, Kafka, Elasticsearch).
- [ ] Run the application.
- [ ] Authenticate as ADMIN (use JWT from auth-service or mock).
- [ ] POST /catalog: Create a new product (check DB and ES index).
- [ ] GET /catalog/{id}: Retrieve product (check cache hit on second call).
- [ ] PUT /catalog/{id}: Update product (check cache invalidation and event published).
- [ ] DELETE /catalog/{id}: Delete product (check removal from DB and ES).
- [ ] GET /catalog?query=keyword: Search products (verify full-text results).
- [ ] GET /catalog?type=EBOOK: Filter by type.
- [ ] GET /catalog?minPrice=0&maxPrice=1000: Filter by price range.
- [ ] GET /catalog?category=genre: Filter by category.
- [ ] Check Kafka topic for events (use consumer tool).
- [ ] As USER, attempt ADMIN endpoints (should fail).
- [ ] Monitor logs for errors.