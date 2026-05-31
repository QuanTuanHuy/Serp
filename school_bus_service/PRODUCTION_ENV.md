# School Bus Service — Production Environment Variables

Danh sách các biến môi trường cần set khi deploy production.
Nếu không set, giá trị mặc định (local dev) sẽ được sử dụng.

## Database

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://db-host:5432/serp_school_bus` |
| `DB_USERNAME` | Database username | `serp_prod` |
| `DB_PASSWORD` | Database password | `<secure-password>` |

## Server

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Application port | `8094` |

## Authentication (Keycloak)

| Variable | Description | Example |
|----------|-------------|---------|
| `KEYCLOAK_URL` | Keycloak base URL | `https://auth.serp.texkis.com` |

## Security

| Variable | Description | Default |
|----------|-------------|---------|
| `SECURITY_ROLE_SERP_SERVICE` | Inter-service role name | `SERP_SERVICES` |

## WebSocket

| Variable | Description | Default |
|----------|-------------|---------|
| `WS_ENDPOINT` | STOMP WebSocket endpoint path | `/ws/school-bus` |
| `WS_ALLOWED_ORIGINS` | Comma-separated allowed origins cho CORS | `http://localhost:3000,https://serp.texkis.com` |

> **Production**: set `WS_ALLOWED_ORIGINS=https://serp.texkis.com` (bỏ localhost).

## Map / Routing

| Variable | Description | Default |
|----------|-------------|---------|
| `MAP_NOMINATIM_BASE_URL` | Nominatim geocoding API | `https://nominatim.openstreetmap.org` |
| `MAP_NOMINATIM_USER_AGENT` | User-Agent header cho Nominatim | `SERP-SchoolBus/0.1 (local-dev)` |
| `MAP_ROUTING_USER_AGENT` | User-Agent header cho routing | `SERP-SchoolBus/0.1 (local-dev)` |
| `MAP_ROUTING_OSRM_BASE_URL` | OSRM routing engine URL | `https://router.project-osrm.org` |
| `MAP_ROUTING_OSRM_PROFILE` | OSRM routing profile | `driving` |

> **Production**: nên self-host OSRM và set `MAP_ROUTING_OSRM_BASE_URL` về instance riêng.
> Cập nhật User-Agent thành tên chính thức (e.g. `SERP-SchoolBus/1.0 (serp.texkis.com)`).

---

## Example `.env` file (production)

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://db-prod:5432/serp_school_bus
DB_USERNAME=serp_prod
DB_PASSWORD=<secure-password>
SERVER_PORT=8094
KEYCLOAK_URL=https://auth.serp.texkis.com
WS_ALLOWED_ORIGINS=https://serp.texkis.com
MAP_NOMINATIM_USER_AGENT=SERP-SchoolBus/1.0 (serp.texkis.com)
MAP_ROUTING_USER_AGENT=SERP-SchoolBus/1.0 (serp.texkis.com)
MAP_ROUTING_OSRM_BASE_URL=https://osrm.internal.serp.texkis.com
```
