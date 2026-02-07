# API Gateway

Central entry point for the SERP ERP system. Routes requests to backend microservices with JWT authentication, CORS, and circuit breaker resilience.

**Port:** `8080` | **Go:** 1.25+ | **Framework:** Gin + Uber FX

## Quick Start

```bash
# 1. Set up environment
cp .env.example .env  # Configure KEYCLOAK_URL, CLIENT_SECRET, etc.

# 2. Run
./run-dev.sh

# Or manually
go run src/main.go
```

**Prerequisites:** Go 1.25+, Redis, Keycloak

## Service Routing

| Service | Port | Route Prefix | Description |
|---------|------|--------------|-------------|
| Account | 8081 | `/api/v1/auth`, `/api/v1/users` | Auth, users, orgs |
| CRM | 8086 | `/crm/api/v1/*` | Leads, customers, contacts |
| PTM Task | 8083 | `/ptm/api/v1/projects`, `/tasks`, `/notes` | Projects & tasks |
| PTM Schedule | 8084 | `/ptm/api/v1/schedule-*` | Scheduling & calendar |
| Purchase | 8088 | `/purchase/api/v1/*` | Procurement |
| Logistics | 8089 | `/logistics/api/v1/*` | Warehouse & inventory |
| Notification | 8090 | `/notification/api/v1/*` | Alerts & notifications |
| Sales | 8090 | `/sales/api/v1/*` | Sales orders |
| Discuss | 8092 | `/discuss/api/v1/*` | Team messaging |

## Configuration

### Environment Variables (`.env`)

```bash
# Keycloak
KEYCLOAK_URL=http://localhost:8180
CLIENT_SECRET=

# Backend services (all follow same pattern)
ACCOUNT_SERVICE_HOST=localhost
ACCOUNT_SERVICE_PORT=8081
# ... repeat for other services
```

### YAML Config (`src/config/local.yaml`)

Key sections:
- `app.keycloak` - JWT validation settings
- `app.cors` - Allowed origins, methods, headers
- `external.services` - Backend service addresses and timeouts

## Features

- **JWT Authentication** - Validates tokens via Keycloak JWKS, extracts user context
- **CORS** - Configurable origins, methods, headers with credentials support
- **Circuit Breaker** - Prevents cascading failures (5 failures = open, 30s reset)
- **Request Proxying** - Generic proxy for simple pass-through, custom controllers for complex routes
- **Health Checks** - `GET /actuator/health`, `GET /actuator/info`

## Project Structure

```
src/
├── main.go                     # Entry point
├── cmd/
│   ├── bootstrap/all.go        # DI assembly (register new modules here)
│   └── modules/                # Service modules (account, crm, ptm, etc.)
├── ui/
│   ├── controller/             # HTTP handlers by service
│   ├── router/                 # Route definitions
│   └── middleware/             # JWT, CORS, auth middlewares
├── core/service/               # Business logic
├── infrastructure/client/      # Backend service adapters
├── kernel/
│   ├── properties/             # Config structs
│   └── utils/                  # JWT, circuit breaker, HTTP helpers
└── config/                     # YAML configs (default, local, production)
```

## Development

```bash
# Run
./run-dev.sh

# Test
go test ./...

# Format & lint
go fmt ./...
go vet ./...

# Build
go build -o bin/api-gateway src/main.go
```

## Adding a New Service

1. **Add config** in `src/config/local.yaml` under `external.services`
2. **Create module** in `src/cmd/modules/` (register controllers, services, adapters)
3. **Register module** in `src/cmd/bootstrap/all.go`
4. **Create router** in `src/ui/router/` and register in `router.go`
5. **For simple proxy**: Use `GenericProxyController.ProxyToService()`
6. **For custom logic**: Create dedicated controller in `src/ui/controller/`

## Related Documentation

- [AGENTS.md](../AGENTS.md) - Code style, testing, and development guidelines
- Router files in `src/ui/router/` - Complete API route definitions
