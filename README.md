# AI-Powered Gmail Customer Support Agent with Human Approval Workflow

## Project Structure

```
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

## Features

- IMAP IDLE email listener
- Kafka event pipeline from listener to processing
- Intent analysis + entity extraction + tool calling with LangChain4j/OpenAI-compatible model
- Draft generation and guardrail validation
- MySQL persistence (`emails`, `drafts`)
- Human-in-the-loop workflow: approve / edit / reject
- Gmail reply send on approval with preserved `threadId`
- React dashboard for email and draft management

## Database Schema

### emails
- `id`
- `gmail_id`
- `thread_id`
- `sender`
- `subject`
- `body`
- `status`
- `created_at`

### drafts
- `id`
- `email_id`
- `content`
- `confidence`
- `status`
- `created_at`

## API Endpoints

- `GET /emails`
- `GET /emails/{id}`
- `GET /drafts`
- `POST /drafts/{id}/approve`
- `POST /drafts/{id}/reject`
- `PUT /drafts/{id}`

## Setup

1. Copy env file:

```bash
cp .env.example .env
```

2. Configure `.env`:

- Set `EMAIL_USERNAME` and `EMAIL_PASSWORD` for IMAP listener
- Set `LLM_API_KEY` (Gemini/OpenAI-compatible)
- Set `SMTP_HOST` and `SMTP_PORT` for SMTP send

3. Build services:

```bash
./scripts/setup.sh
```

4. Run all services:

```bash
cd infrastructure
docker compose --env-file ../.env up --build
```

5. Open UI:

- Frontend: http://localhost:5173
- Processing APIs: http://localhost:8081
- Listener health: http://localhost:8082/actuator/health

## Gmail OAuth Notes

- `email-listener-service` uses IMAP IDLE.
- `email-processing-service` sends approved replies via SMTP.

## Local Run (without Docker)

Terminal 1:

```bash
cd email-listener-service
./mvnw spring-boot:run
```

Terminal 2:

```bash
cd email-processing-service
./mvnw spring-boot:run
```

Terminal 3:

```bash
cd frontend
npm run dev
```

## AI Prompt Files

- `email-processing-service/src/main/resources/ai/prompts/system_prompt.txt`
- `email-processing-service/src/main/resources/ai/prompts/intent_prompt.txt`
- `email-processing-service/src/main/resources/ai/prompts/draft_prompt.txt`
- `email-processing-service/src/main/resources/ai/prompts/guardrail_prompt.txt`
