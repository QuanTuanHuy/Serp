# SERP Discuss Service

The **Discuss Service** is the real-time communication backbone of the SERP ERP system. It provides a robust platform for team collaboration, supporting channels, direct messaging, file sharing, and real-time presence features.

## 🚀 Overview

Built with **Java 21** and **Spring Boot 3.5**, this service implements **Clean Architecture** to ensure maintainability and scalability. It leverages **WebSocket (STOMP)** for instant message delivery and **Kafka** for asynchronous event processing.

### Key Features

- **💬 Messaging**: Rich text messages, threading, emoji reactions, and read receipts.
- **📢 Channels**:
  - **Direct**: 1-on-1 private conversations.
  - **Group**: Private or public groups for teams.
  - **Topic**: Context-aware channels linked to specific ERP entities (e.g., "Task #123", "Project Alpha").
- **⚡ Real-time**:
  - Instant message delivery via WebSocket.
  - User presence (Online/Offline) tracking.
  - Live typing indicators.
  - Unread message counters.
- **📎 Attachments**:
  - Secure file upload to S3-compatible storage (MinIO).
  - Automatic presigned URL generation for secure, temporary access.
  - Image thumbnail handling.
- **🔍 Search**: Full-text search capabilities for messages and files.

## 🛠️ Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.5
- **Database**: PostgreSQL (with Flyway migrations)
- **Caching**: Redis (Session storage, Presence, Pub/Sub)
- **Messaging**: Kafka (Event sourcing, Notifications)
- **Storage**: MinIO / S3 (File attachments)
- **WebSocket**: Spring WebSocket (STOMP)
- **Security**: OAuth2 / JWT (Keycloak integration)

## 🏗️ Architecture

The service follows strict **Clean Architecture** principles:

```mermaid
graph TD
    UI[UI Layer<br>(Controllers, WebSocket)] --> UseCase[UseCase Layer<br>(Application Logic)]
    UseCase --> Service[Service Layer<br>(Domain Logic)]
    Service --> Port[Port Layer<br>(Interfaces)]
    Port --> Infra[Infrastructure Layer<br>(Adapters)]
    
    Infra --> DB[(PostgreSQL)]
    Infra --> Cache[(Redis)]
    Infra --> MQ((Kafka))
    Infra --> S3((MinIO))
```

- **Domain**: Pure business entities and rules.
- **UseCase**: Orchestrates application flows (e.g., `MessageUseCase`, `ChannelUseCase`).
- **Infrastructure**: Implementations for database, storage, and external services.

## ⚙️ Configuration

The service uses `application.yaml` for configuration. Key environment variables include:

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Service port | `8085` |
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `REDIS_HOST` | Redis host | `localhost` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers | `localhost:9092` |
| `STORAGE_ENDPOINT` | MinIO/S3 API URL | `http://localhost:9000` |
| `STORAGE_BUCKET` | Bucket name | `discuss-attachments` |

## 🚀 Getting Started

### Prerequisites

- JDK 21
- Docker & Docker Compose (for infrastructure)
- Maven

### Running Locally

1. **Start Infrastructure**:
   Ensure PostgreSQL, Redis, Kafka, and MinIO are running (usually via the root `docker-compose.dev.yml`).

2. **Run the Service**:
   Use the helper script to load environment variables and start the app:
   ```bash
   ./run-dev.sh
   ```
   Or using Maven directly:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Verify**:
   - Health check: `http://localhost:8085/actuator/health`

## 🧪 Testing

The project maintains a high standard of test coverage using **JUnit 5** and **Mockito**.

- **Run all tests**:
  ```bash
  ./mvnw test
  ```

- **Run specific test class**:
  ```bash
  ./mvnw test -Dtest=MessageUseCaseTest
  ```

## 🔌 WebSocket API

The service exposes a STOMP endpoint at `/ws`.

**Subscribe Destinations:**
- `/topic/channel.{channelId}`: New messages in a channel.
- `/topic/user.{userId}`: Notifications, unread counts, direct messages.
- `/topic/presence`: Global or channel-specific online status updates.

**Send Destinations:**
- `/app/chat.sendMessage`: Send a message.
- `/app/chat.typing`: Send typing indicator.

## 📁 Project Structure

```
src/main/java/serp/project/discuss_service/
├── core/
│   ├── domain/        # Entities, Enums, Value Objects
│   ├── port/          # Interfaces (Repository, Client, Storage)
│   ├── service/       # Domain Services
│   └── usecase/       # Application Use Cases
├── infrastructure/
│   ├── client/        # Redis, Kafka, S3 Adapters
│   └── store/         # JPA Repositories, Entities, Mappers
├── kernel/            # Shared config, utils, exceptions
└── ui/                # REST Controllers, WebSocket Controllers
```

## 🤝 Contribution

