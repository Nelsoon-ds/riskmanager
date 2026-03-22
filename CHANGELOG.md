# Changelog

## 2026-03-22 — Weaviate semantic search integration

### Overview
Added document ingestion and semantic search capabilities using Weaviate as a vector store and Ollama for local embeddings.

### Dependencies added (`pom.xml`)
- `spring-ai-starter-model-ollama` — provides the `EmbeddingModel` bean backed by Ollama
- `spring-ai-weaviate-store` — Spring AI's Weaviate vector store integration

### Configuration (`application.properties`)
```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.embedding.options.model=nomic-embed-text

spring.ai.vectorstore.weaviate.scheme=http
spring.ai.vectorstore.weaviate.host=localhost:8080
spring.ai.vectorstore.weaviate.object-class=Document
```

### New files
- `src/main/java/com/nelson/riskmanager/config/WeaviateConfig.java`
  - Defines `WeaviateClient` bean (connects to Weaviate over HTTP)
  - Defines `WeaviateVectorStore` bean (wires client + Ollama embedding model)

- `src/main/java/com/nelson/riskmanager/service/DocumentIngestionService.java`
  - `ingestFile(Path)` — reads a file's text content and stores it as a `Document` in Weaviate
  - `ingestDirectory(Path)` — walks a directory and ingests all files
  - `search(String query, int topK)` — runs a similarity search and returns the closest documents

### Modified files
- `src/main/java/com/nelson/riskmanager/controller/RiskManagerController.java`
  - Injected `DocumentIngestionService`
  - `POST /ingest?path=<file-or-directory>` — ingests a file or all files in a directory
  - `GET /search?query=<text>&topK=<n>` — returns the top N most semantically similar document texts

### Prerequisites
1. Run `docker compose up -d` from the Weaviate project directory
2. Pull the embedding model: `docker exec <ollama-container> ollama pull nomic-embed-text`
