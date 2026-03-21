# 🤖 Customer Support Agent

An AI-powered customer support system built with **Spring Boot microservices** and **Apache Kafka**. The system automatically listens for incoming customer emails via IMAP IDLE, processes them through a Kafka pipeline, and enables intelligent email processing (classification, auto-reply, ticketing, etc.).

---

## 📐 Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                     Customer Support Agent                          │
│                                                                     │
│  ┌──────────────────────┐          ┌──────────────────────────┐     │
│  │ email-listener-service│  Kafka   │ email-processing-service │     │
│  │       (port 8080)     │  Topic   │       (port 8081)        │     │
│  │                       │          │                          │     │
│  │  IMAP IDLE Listener   │          │   Kafka Consumer         │     │
│  │        ↓              │          │        ↓                 │     │
│  │  MimeMessage          │ ──────→  │   EmailMessage DTO       │     │
│  │        ↓              │"incoming │        ↓                 │     │
│  │  Extract → DTO        │ -emails" │   Process / Classify     │     │
│  │        ↓              │          │        ↓                 │     │
│  │  Kafka Producer       │          │   (AI / Ticketing / etc) │     │
│  └──────────────────────┘          └──────────────────────────┘     │
└─────────────────────────────────────────────────────────────────────┘
```

### Why Kafka?

`MimeMessage` (Java's email object) is **not serializable** — it cannot be sent directly between services. Instead, the listener service extracts the relevant fields into a serializable `EmailMessage` DTO, serializes it as **JSON**, and publishes it to a Kafka topic. The processing service consumes the JSON and deserializes it back into an `EmailMessage` DTO.

**Benefits:**
- 🔌 **Decoupled services** — each service can be developed, deployed, and scaled independently
- 💾 **Reliability** — Kafka persists messages on disk; if the processing service is down, no emails are lost
- 📈 **Scalability** — add multiple consumers (e.g., AI classifier, ticket creator, auto-responder) easily
- 🔄 **Replay** — Kafka retains messages, enabling re-processing if needed

---

## 🧩 Services

### 1. `email-listener-service` (Port 8080)

Listens for incoming emails via **IMAP IDLE** (push-based, real-time) and publishes them to Kafka.

| Component | Description |
|-----------|-------------|
| `ImapIntegrationConfig` | Spring Integration flow for IMAP IDLE connection |
| `EmailProcessingService` | Extracts `MimeMessage` → `EmailMessage` DTO |
| `EmailKafkaProducer` | Publishes `EmailMessage` to Kafka topic `incoming-emails` |
| `EmailMessage` (DTO) | Serializable POJO with `messageId`, `from`, `subject`, `body`, `receivedAt` |

### 2. `email-processing-service` (Port 8081)

Consumes emails from Kafka and processes them.

| Component | Description |
|-----------|-------------|
| `EmailKafkaConsumer` | Listens on Kafka topic `incoming-emails` |
| `EmailProcessingService` | Processes the `EmailMessage` DTO (add your AI logic here) |
| `EmailMessage` (DTO) | Same serializable POJO for deserialization |

---

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| **Java 17** | Language |
| **Spring Boot 4.0.3** | Application framework |
| **Spring Integration Mail** | IMAP IDLE email listener |
| **Apache Kafka** | Message broker between services |
| **Spring Kafka** | Kafka producer/consumer integration |
| **Jackson** | JSON serialization/deserialization |
| **Lombok** | Boilerplate reduction |
| **Maven** | Build tool |

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+**
- **Maven 3.8+** (or use the included `mvnw` wrapper)
- **Apache Kafka** running on `localhost:9092`
- A **Gmail account** with an [App Password](https://support.google.com/accounts/answer/185833) for IMAP access

### 1. Start Kafka

The easiest way is with Docker:

```bash
docker run -d --name kafka \
  -p 9092:9092 \
  -e KAFKA_CFG_NODE_ID=0 \
  -e KAFKA_CFG_PROCESS_ROLES=controller,broker \
  -e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@localhost:9093 \
  -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
  -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
  bitnami/kafka:latest
```

### 2. Configure Email Credentials

Create a `.env` file in the `email-listener-service/` directory:

```properties
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password
```

> ⚠️ **Important:** Use a [Gmail App Password](https://support.google.com/accounts/answer/185833), not your regular password. You must have 2FA enabled on your Google account to generate one.

### 3. Start the Email Listener Service

```bash
cd email-listener-service
./mvnw spring-boot:run
```

### 4. Start the Email Processing Service

In a separate terminal:

```bash
cd email-processing-service
./mvnw spring-boot:run
```

### 5. Test It

Send an email to the configured Gmail address. You should see:

**In the listener service logs:**
```
📧 Received New Email:
From: sender@example.com
Subject: Test Email
📤 Publishing email to Kafka topic 'incoming-emails': subject='Test Email'
✅ Email published to Kafka: partition=0, offset=0
```

**In the processing service logs:**
```
📥 Received email from Kafka: subject='Test Email'
📧 Processing Email:
From: sender@example.com
Subject: Test Email
Body: Hello, I need help with...
✅ Email processed successfully
```

---

## 📁 Project Structure

```
Customer Support Agent/
├── README.md
├── .gitignore
│
├── email-listener-service/              # Service 1: Email Listener
│   ├── .env                             # Email credentials (not committed)
│   ├── pom.xml
│   ├── mvnw
│   └── src/main/
│       ├── resources/
│       │   └── application.yaml         # IMAP + Kafka producer config
│       └── java/.../email_listener_service/
│           ├── EmailListenerServiceApplication.java
│           ├── config/
│           │   └── ImapIntegrationConfig.java     # IMAP IDLE flow
│           ├── dto/
│           │   └── EmailMessage.java              # Shared DTO
│           ├── kafka/
│           │   └── EmailKafkaProducer.java         # Kafka producer
│           └── service/
│               └── EmailProcessingService.java     # MimeMessage → DTO → Kafka
│
└── email-processing-service/            # Service 2: Email Processor
    ├── pom.xml
    ├── mvnw
    └── src/main/
        ├── resources/
        │   └── application.yaml         # Kafka consumer config
        └── java/.../email_processing_service/
            ├── EmailProcessingServiceApplication.java
            ├── dto/
            │   └── EmailMessage.java              # Shared DTO
            ├── kafka/
            │   └── EmailKafkaConsumer.java         # Kafka consumer
            └── service/
                └── EmailProcessingService.java     # Business logic
```

---

## 📨 EmailMessage DTO

The shared data contract between services:

```java
public class EmailMessage implements Serializable {
    private String messageId;   // Unique email message ID
    private String from;        // Sender address
    private String subject;     // Email subject line
    private String body;        // Email body content
    private Instant receivedAt; // Timestamp when received
}
```

---

## 🔮 Future Enhancements

- [ ] **AI-powered classification** — Categorize emails (billing, technical, general) using an LLM
- [ ] **Auto-reply generation** — Generate context-aware responses using RAG
- [ ] **Ticket creation** — Automatically create support tickets from emails
- [ ] **Sentiment analysis** — Detect urgency and customer sentiment
- [ ] **Knowledge base integration** — Search existing documentation for answers
- [ ] **Docker Compose** — Single-command startup for all services + Kafka
- [ ] **Shared DTO module** — Extract `EmailMessage` into a common Maven module

---

## 📄 License

This project is for educational and development purposes.