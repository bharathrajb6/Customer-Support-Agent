# Customer Support Agent

AI-powered customer support system with human approval workflow.

## Services

- `email-listener-service` (Spring Boot)
  - Reads incoming mail via IMAP IDLE
  - Extracts subject/body/sender
  - Publishes events to Kafka topic `incoming-emails`
- `email-processing-service` (Spring Boot)
  - Consumes Kafka events
  - Runs AI pipeline (intent -> tools -> guardrail -> draft)
  - Persists `emails` and `drafts` in MySQL
  - Exposes approval/edit/reject APIs
  - Sends approved replies via SMTP
- `frontend` (React + Vite)
  - Email list with pagination
  - Draft workspace + email details
  - SSE + polling refresh

## Architecture

`IMAP -> Listener -> Kafka -> Processing -> MySQL -> Frontend -> Human Approve -> SMTP Reply`

## Project Structure

```text
Customer Support Agent/
├── email-listener-service/
├── email-processing-service/
├── frontend/
├── infrastructure/
│   ├── docker-compose.yml
│   ├── Dockerfile.listener
│   ├── Dockerfile.processing
│   └── Dockerfile.frontend
├── scripts/
│   └── setup.sh
├── .env.example
└── README.md
```

## Environment

1. Copy example env:

```bash
cp .env.example .env
```

2. Update required values in `.env`:

- `EMAIL_USERNAME`, `EMAIL_PASSWORD` (Gmail app password)
- `LLM_API_KEY`
- Optional for Gemini (OpenAI-compatible endpoint):
  - `LLM_BASE_URL=https://generativelanguage.googleapis.com/v1beta/openai`
  - `LLM_MODEL=gemini-2.5-flash`

## Run (Docker - recommended)

From project root:

```bash
docker compose -f infrastructure/docker-compose.yml --env-file .env up --build -d
```

Open:

- Frontend: `http://localhost:5173`
- Processing API: `http://localhost:8081`
- Listener health: `http://localhost:8082/actuator/health`

Stop:

```bash
docker compose -f infrastructure/docker-compose.yml --env-file .env down
```

## API Endpoints

- `GET /emails`
- `GET /emails/{id}`
- `GET /drafts`
- `PUT /drafts/{id}`
- `POST /drafts/{id}/approve`
- `POST /drafts/{id}/reject`
- `GET /events/drafts` (SSE)

## Database Tables

### `emails`

- `id`
- `gmail_id`
- `thread_id`
- `sender`
- `subject`
- `body`
- `status`
- `created_at`

### `drafts`

- `id`
- `email_id`
- `content`
- `confidence`
- `status`
- `created_at`

## AI Prompt Files

- `email-processing-service/src/main/resources/ai/prompts/system_prompt.txt`
- `email-processing-service/src/main/resources/ai/prompts/intent_prompt.txt`
- `email-processing-service/src/main/resources/ai/prompts/draft_prompt.txt`
- `email-processing-service/src/main/resources/ai/prompts/guardrail_prompt.txt`

## How to Verify AI vs Fallback

Check processing logs:

```bash
docker compose -f infrastructure/docker-compose.yml --env-file .env logs -f email-processing-service
```

Look for:

- `Initializing LLM client: ... model=... baseUrl=... apiKey=...`
- `Draft creation finished ... source=AI_GENERATED`
- or `Draft creation finished ... source=FALLBACK_GENERATED`

## Troubleshooting

### 1) Emails not visible in UI

- Check listener logs for `Email published:`
- Check processing logs for `Received email event:`
- Verify frontend API URL `VITE_API_BASE_URL=http://localhost:8081`

### 2) Body shows `(No body content extracted)`

- Rebuild listener after parser/config changes:

```bash
docker compose -f infrastructure/docker-compose.yml --env-file .env up --build -d email-listener-service
```

### 3) Port already in use

- Stop local services using same ports or change exposed ports in compose.

### 4) Docker container name conflict

- Remove stale container and recreate service:

```bash
docker rm -f infrastructure-email-processing-service-1
```

then:

```bash
docker compose -f infrastructure/docker-compose.yml --env-file .env up -d email-processing-service
```

## Local Run (without Docker)

Start dependencies (MySQL, Kafka, Zookeeper) separately, then:

```bash
cd email-listener-service && ./mvnw spring-boot:run
```

```bash
cd email-processing-service && ./mvnw spring-boot:run
```

```bash
cd frontend && npm install && npm run dev
```
