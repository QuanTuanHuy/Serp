# Notification Service

Real-time notification system with WebSocket delivery, Kafka event consumption, and user preference management.

**Port:** `8090` | **Go:** 1.25+ | **Framework:** Gin + Uber FX + Gorilla WebSocket

## Quick Start

```bash
# 1. Set up environment
cp .env.example .env  # Configure DB, Redis, Kafka, Keycloak

# 2. Run
./run-dev.sh

# Or manually
go run src/main.go
```

**Prerequisites:** Go 1.25+, PostgreSQL, Redis, Kafka, Keycloak

## Features

- **Real-Time WebSocket** - Instant push to all user devices (desktop, mobile, tablet)
- **Kafka Consumer** - Event-driven notifications from CRM, PTM, Account services
- **User Preferences** - Per-user channel settings, quiet hours
- **Multi-Tenancy** - Strict tenant isolation for all operations
- **Idempotency** - Duplicate event prevention via event ID tracking
- **Categorization** - Categories (SYSTEM, CRM, PTM), priorities (LOW→URGENT), types (INFO→ERROR)

## API Routes

Access via API Gateway: `http://localhost:8080/notification-service/api/v1`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/notifications` | List with filters (type, category, priority, status, isRead) |
| POST | `/notifications` | Create notification |
| GET | `/notifications/:id` | Get by ID |
| PATCH | `/notifications/:id` | Update (mark read, archive) |
| DELETE | `/notifications/:id` | Delete |
| PATCH | `/notifications/read-all` | Mark all as read |
| GET | `/notifications/unread-count` | Unread count |
| GET | `/preferences` | Get user preferences |
| PATCH | `/preferences` | Update preferences |
| GET | `/ws?token={jwt}` | WebSocket connection |
| GET | `/health` | Health check |

## Kafka Events

**Topic:** `serp.notification.user.events`

| Event Type | Description |
|------------|-------------|
| `notification.create.requested` | Create single notification |
| `notification.bulk_create.requested` | Bulk create for multiple users |

**Flow:** Kafka → Idempotency Check → Save to DB → WebSocket Broadcast

## WebSocket

**Connect:** `GET /notification-service/ws?token={jwt}`

**Message Types:**
- `NOTIFICATION_NEW` - New notification pushed
- `INITIAL_DATA` - Unread count on connect
- `PING/PONG` - Keep-alive

**Hub Capabilities:**
- `SendToUser` - All devices of a user
- `BroadcastToTenant` - All users in tenant
- `BroadcastToAll` - System announcements

## Project Structure

```
src/
├── main.go
├── cmd/bootstrap/all.go        # DI assembly
├── ui/
│   ├── controller/             # REST + WebSocket handlers
│   ├── kafka/                  # Event consumers
│   ├── middleware/             # JWT, CORS
│   └── router/                 # Routes
├── core/
│   ├── domain/                 # Entities, DTOs, enums
│   ├── usecase/                # Business logic
│   ├── service/                # Domain services
│   ├── websocket/              # Hub, Client management
│   └── port/                   # Interfaces
├── infrastructure/
│   └── store/                  # PostgreSQL repos, mappers
└── config/                     # YAML configs
```

## Configuration

### Environment Variables

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=serp_notification
DB_USER=
DB_PASSWORD=

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka
KAFKA_BROKERS=localhost:9092

# Keycloak
KEYCLOAK_URL=http://localhost:8180
```

### Database Tables

- `notification` - Notification records
- `notification_preference` - User preferences
- `processed_event` - Idempotency tracking
- `failed_event` - Failed event retry queue

## Development

```bash
# Run
./run-dev.sh

# Test
go test ./...

# Format & lint
go fmt ./...
go vet ./...
```

## Related Documentation

- [AGENTS.md](../AGENTS.md) - Code style and development guidelines
- [API Gateway](../api_gateway/README.md) - Request routing
