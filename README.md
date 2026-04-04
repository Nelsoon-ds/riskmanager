# Risk Manager — Visual Risk Analysis Tool

AI-powered workplace safety tool that analyzes uploaded images for hazards, cross-references them against official safety standards and guidelines stored in a local vector database, and returns a structured risk assessment in Danish.

## How It Works

1. **Upload** an image through the web interface
2. A vision LLM (Claude) performs an **initial free-text analysis** — identifying hazards, severity levels, and bounding box coordinates
3. The initial analysis is used to **query a local vector store (Weaviate)** for semantically relevant safety standards and guidelines
4. A second **augmented LLM call** combines the initial analysis with the retrieved documents to produce a structured `RiskAssessment` — with standard citations and actionable recommendations
5. The frontend overlays color-coded bounding boxes on the image and displays the full assessment

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21 / Spring Boot |
| AI — Vision & Chat | Anthropic Claude (`claude-haiku-4-5`) via Spring AI |
| AI — Embeddings | Ollama (`nomic-embed-text`) running locally |
| Vector Store | Weaviate (self-hosted via Docker) |
| PDF parsing | Apache PDFBox |
| Frontend | Thymeleaf templates, vanilla CSS & JS |

## Project Structure

```
riskmanager/
├── docker-compose.yml                        # Weaviate + Ollama services
├── src/main/
│   ├── java/com/nelson/riskmanager/
│   │   ├── config/
│   │   │   └── WeaviateConfig.java           # Weaviate client + vector store beans
│   │   ├── controller/
│   │   │   ├── RiskManagerController.java    # /upload, /ingest, /search REST endpoints
│   │   │   └── ViewController.java           # /home, /analyze, /admin page controllers
│   │   ├── model/
│   │   │   ├── RiskAssessment.java           # Top-level structured output
│   │   │   ├── Hazard.java                   # Individual hazard with bbox + references
│   │   │   ├── StandardReference.java        # Cited standard with section + relevance
│   │   │   ├── DetectedHazard.java
│   │   │   └── InitialAnalysis.java
│   │   └── service/
│   │       ├── RiskManagerService.java       # Two-step RAG analysis pipeline
│   │       ├── DocumentIngestionService.java # File/PDF ingestion + vector search
│   │       └── FileStorageService.java       # Upload handling
│   └── resources/
│       ├── prompts/
│       │   ├── initial-analysis.st           # Vision prompt (free-text, Danish)
│       │   └── augmented-analysis.st         # RAG prompt with context + structured output
│       ├── static/
│       │   ├── css/                          # Stylesheets
│       │   ├── javascript/                   # Frontend JS
│       │   └── data/                         # Source documents for ingestion
│       └── templates/
│           ├── analyze.html                  # Main analysis page
│           └── admin.html                    # Admin / ingestion page
└── uploads/                                  # Saved uploaded images (auto-created)
```

## Endpoints

### Page routes

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/home` | Main analysis page |
| `POST` | `/analyze` | Upload image, run analysis, render results |
| `GET` | `/admin` | Admin page for document ingestion |

### REST API

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/upload` | Save a multipart image to `uploads/`, return path |
| `POST` | `/ingest?path=<path>` | Ingest a file or directory into the vector store |
| `GET` | `/search?query=<text>&topK=<n>` | Semantic search against ingested documents |

## Data Model

```
RiskAssessment
├── summary          (String)
├── overallSeverity  (String)
└── hazards[]
    ├── name
    ├── severity          (PRIMARY / SECONDARY / TERTIARY)
    ├── boundingBox       ([x_min, y_min, x_max, y_max] normalized 0–1)
    ├── description
    ├── recommendations[]
    └── standardReferences[]
        ├── standardName  (e.g. "BEK nr. 835 §3")
        ├── section
        └── relevance
```

## Running Locally

### Prerequisites

- Java 21
- Docker
- Ollama (for embeddings)
- An Anthropic API key

### 1. Start infrastructure

```bash
docker compose up -d
```

This starts Weaviate (port 8080) and Ollama (port 11434).

### 2. Pull the embedding model

```bash
docker exec <ollama-container-name> ollama pull nomic-embed-text
```

### 3. Configure the application

Set your Anthropic API key:

```bash
export CLAUDE_API_KEY=sk-ant-...
```

Or add it to `application.properties`. The app runs on **port 8081** to avoid conflict with Weaviate on 8080.

### 4. Run the application

```bash
./mvnw spring-boot:run
```

Open `http://localhost:8081/home`

### 5. Ingest safety documents

Call the ingest endpoint with the path to a file or directory:

```bash
curl -X POST "http://localhost:8081/ingest?path=src/main/resources/static/data"
```

Supported formats: `.txt`, `.pdf`

## Safety Knowledge Base

The `src/main/resources/static/data/` directory contains safety standards used for RAG retrieval:

- Danish occupational health regulations (Arbejdstilsynet)
- EU occupational safety framework directives
- IFC/World Bank telecom EHS guidelines
- OSHA telecom standards
- Data centre security standards
