# API Gateway

API Gateway is the central entry point for the SERP ERP system, providing unified access to all microservices through a single endpoint. It handles routing, authentication, authorization, and cross-cutting concerns for the entire platform.

## Overview

The API Gateway acts as a reverse proxy that routes client requests to appropriate backend services, enforces security policies, and provides resilience patterns for robust service communication.

**Technology Stack:**
- Go 1.25.0
- Gin Web Framework
- Uber FX (Dependency Injection)
- JWT Authentication (Keycloak)
- Redis (Caching)
- Circuit Breaker Pattern (gobreaker)

**Port:** 8080  
**Base Path:** `/`

## Architecture

This service follows **Clean Architecture** with a modular design:

```
src/
├── main.go                    # Application entry point
├── cmd/
│   ├── bootstrap/            # Dependency injection setup
│   │   ├── all.go           # Main module assembly
│   │   └── build.go         # Build metadata
│   └── modules/             # Service-specific modules
│       ├── account_module.go
│       ├── crm_module.go
│       ├── ptm_modules.go
│       ├── purchase_module.go
│       ├── logistics_module.go
│       ├── notification_module.go
│       ├── sales_module.go
│       └── discuss_module.go
├── ui/
│   ├── controller/          # HTTP request handlers
│   │   ├── common/         # Generic proxy controller
│   │   ├── account/        # Account service controllers
│   │   ├── crm/           # CRM service controllers
│   │   ├── ptm/           # PTM service controllers
│   │   ├── notification/  # Notification controllers
│   │   └── discuss/       # Discuss controllers
│   ├── router/             # Route definitions
│   │   ├── router.go      # Main router assembly
│   │   ├── account_router.go
│   │   ├── crm_router.go
│   │   ├── ptm_router.go
│   │   ├── purchase_router.go
│   │   ├── logistics_router.go
│   │   ├── notification_router.go
│   │   ├── sales_router.go
│   │   └── discuss_router.go
│   └── middleware/         # HTTP middlewares
│       ├── jwt_middleware.go   # JWT authentication
│       ├── auth_middleware.go  # Authorization
│       └── cors_middleware.go  # CORS handling
├── core/
│   ├── service/            # Business logic for proxying
│   └── domain/             # Domain models and constants
├── infrastructure/
│   └── client/             # External service adapters
│       └── account/        # Account service clients
├── kernel/
│   ├── properties/         # Configuration properties
│   │   ├── external_service_properties.go
│   │   ├── keycloak_properties.go
│   │   ├── cors_properties.go
│   │   └── circuit_breaker_properties.go
│   └── utils/              # Utility functions
│       ├── jwt_utils.go
│       ├── circuit_breaker.go
│       ├── http_transport.go
│       └── response.go
└── config/
    ├── default.yaml        # Default configuration
    ├── local.yaml          # Local development config
    └── production.yaml     # Production config
```

## Core Features

### 1. Request Routing & Proxying

The API Gateway routes requests to 9 backend services:

| Service | Port | Context Path | Description |
|---------|------|-------------|-------------|
| **Account Service** | 8081 | `/account-service` | Authentication, users, organizations |
| **CRM Service** | 8086 | `/crm` | Leads, opportunities, customers, contacts |
| **PTM Task** | 8083 | `/ptm-task` | Projects, tasks, notes |
| **PTM Schedule** | 8084 | `/ptm-schedule` | Schedule plans, availability, events |
| **Purchase Service** | 8088 | `/purchase` | Purchase orders, vendors, procurement |
| **Logistics Service** | 8089 | `/logistics` | Warehouses, inventory, shipments |
| **Notification Service** | 8090 | `/ns` | Notifications, alerts |
| **Sales Service** | 8090 | `/sales` | Sales orders, quotes, invoices |
| **Discuss Service** | 8092 | `/discuss` | Channels, messages, collaboration |

**Routing Strategies:**

1. **Generic Proxy Pattern** - For services with simple pass-through:
   ```go
   // Example: Purchase service uses generic proxy
   group.Any("/purchase/api/v1/*proxyPath", genericProxyController.ProxyToPurchase)
   ```

2. **Service-Specific Controllers** - For services requiring custom logic:
   ```go
   // Example: CRM has specific controllers for leads, opportunities, etc.
   group.POST("/crm/api/v1/leads", leadController.CreateLead)
   group.GET("/crm/api/v1/customers/:id", customerController.GetCustomerByID)
   ```

### 2. Authentication & Authorization

**JWT Middleware:**
- Validates JWT tokens issued by Keycloak
- Extracts user information (userID, email, roles)
- Injects user context into requests
- Supports both access tokens and refresh tokens

**Authentication Flow:**
```
1. Client sends request with Authorization header: Bearer <token>
2. JWT Middleware validates token against Keycloak JWKS
3. Middleware extracts claims (userID, email, roles)
4. User context is propagated to backend services
5. Request forwarded with original token
```

**Authorization:**
- Role-based access control (RBAC)
- Middleware checks user roles against required roles
- Fine-grained permission validation

**Key Middleware Functions:**
```go
// Authenticate JWT token
middleware.AuthMiddleware()

// Require specific role
middleware.RequireRole("SUPER_ADMIN")
```

### 3. Cross-Cutting Concerns

**CORS (Cross-Origin Resource Sharing):**
- Configurable allowed origins, methods, headers
- Supports credentials
- Pre-flight request handling

Configuration:
```yaml
cors:
  allowedOrigins:
    - http://localhost:3000
    - https://serp-soict.vercel.app
  allowedMethods:
    - GET
    - POST
    - PUT
    - DELETE
    - OPTIONS
  allowedHeaders:
    - Authorization
    - Content-Type
  allowCredentials: true
  maxAge: 3600
```

**Circuit Breaker Pattern:**
- Prevents cascading failures
- Automatic failure detection
- Configurable failure threshold
- Half-open state for recovery testing
- Service-specific breakers

Circuit Breaker States:
- **Closed** - Normal operation, requests pass through
- **Open** - Service failing, requests fail fast
- **Half-Open** - Testing if service recovered

Default Configuration:
```go
MaxFailures:  5              // Open after 5 failures
ResetTimeout: 30s            // Try half-open after 30s
Timeout:      5s             // Request timeout
```

**Health Checks:**
```
GET /actuator/health         # Service health status
GET /actuator/info          # Service build information
```

### 4. Resilience & Fault Tolerance

**Timeout Management:**
- Configurable per-service timeouts
- Default: 15s for most services
- Context-based timeout propagation

**Error Handling:**
- Graceful error responses
- Circuit breaker fallbacks
- Retry logic (where applicable)
- Detailed error logging

**Request/Response Logging:**
- Structured logging with context
- Request ID tracking
- Performance metrics

## API Routes

### Account Service Routes

**Authentication:**
```
POST   /api/v1/auth/register              # Register new user
POST   /api/v1/auth/login                 # User login
POST   /api/v1/auth/get-token             # Get access token
POST   /api/v1/auth/refresh-token         # Refresh access token
POST   /api/v1/auth/revoke-token          # Revoke token
POST   /api/v1/auth/change-password       # Change password (requires auth)
```

**Users:**
```
GET    /api/v1/users/profile/me           # Get current user profile
GET    /api/v1/users                      # List users
POST   /api/v1/users/assign-roles         # Assign roles to user
PATCH  /api/v1/users/:userId/info         # Update user info
```

**Organizations:**
```
GET    /api/v1/organizations/me           # Get current organization
GET    /api/v1/organizations/:orgId/users # List organization users
POST   /api/v1/organizations/:orgId/users # Add user to organization
```

**Roles & Permissions:**
```
GET    /api/v1/roles                      # List all roles
POST   /api/v1/roles                      # Create role
POST   /api/v1/roles/:roleId/permissions  # Add permissions to role
GET    /api/v1/permissions                # List all permissions
POST   /api/v1/permissions                # Create permission
```

**Subscriptions:**
```
POST   /api/v1/subscriptions/subscribe         # Subscribe to plan
POST   /api/v1/subscriptions/trial             # Start trial
PUT    /api/v1/subscriptions/upgrade           # Upgrade subscription
PUT    /api/v1/subscriptions/cancel            # Cancel subscription
GET    /api/v1/subscriptions/me/active         # Get active subscription
```

**Departments:**
```
GET    /api/v1/organizations/:orgId/departments              # List departments
POST   /api/v1/organizations/:orgId/departments              # Create department
GET    /api/v1/organizations/:orgId/departments/:id/tree     # Get department tree
GET    /api/v1/organizations/:orgId/departments/:id/members  # List department members
```

### CRM Service Routes

**Leads:**
```
POST   /crm/api/v1/leads                  # Create lead
GET    /crm/api/v1/leads                  # List leads
GET    /crm/api/v1/leads/:id              # Get lead by ID
PATCH  /crm/api/v1/leads/:id              # Update lead
POST   /crm/api/v1/leads/:id/qualify      # Qualify lead
POST   /crm/api/v1/leads/:id/convert      # Convert lead to opportunity
DELETE /crm/api/v1/leads/:id              # Delete lead
POST   /crm/api/v1/leads/search           # Search leads
```

**Opportunities:**
```
POST   /crm/api/v1/opportunities                      # Create opportunity
GET    /crm/api/v1/opportunities                      # List opportunities
GET    /crm/api/v1/opportunities/:id                  # Get opportunity by ID
PATCH  /crm/api/v1/opportunities/:id                  # Update opportunity
PATCH  /crm/api/v1/opportunities/:id/stage            # Change stage
POST   /crm/api/v1/opportunities/:id/close-won        # Close as won
POST   /crm/api/v1/opportunities/:id/close-lost       # Close as lost
DELETE /crm/api/v1/opportunities/:id                  # Delete opportunity
POST   /crm/api/v1/opportunities/search               # Search opportunities
```

**Customers:**
```
POST   /crm/api/v1/customers              # Create customer
GET    /crm/api/v1/customers              # List customers
GET    /crm/api/v1/customers/:id          # Get customer by ID
PATCH  /crm/api/v1/customers/:id          # Update customer
DELETE /crm/api/v1/customers/:id          # Delete customer
POST   /crm/api/v1/customers/search       # Search customers
```

**Contacts:**
```
POST   /crm/api/v1/customers/:customerId/contacts        # Create contact
GET    /crm/api/v1/customers/:customerId/contacts        # List contacts by customer
GET    /crm/api/v1/customers/:customerId/contacts/:id    # Get contact by ID
PATCH  /crm/api/v1/customers/:customerId/contacts/:id    # Update contact
DELETE /crm/api/v1/customers/:customerId/contacts/:id    # Delete contact
GET    /crm/api/v1/contacts                              # List all contacts
```

### PTM (Project & Task Management) Routes

**Projects:**
```
POST   /ptm/api/v1/projects               # Create project
GET    /ptm/api/v1/projects               # List projects
GET    /ptm/api/v1/projects/:id           # Get project by ID
GET    /ptm/api/v1/projects/:id/tasks     # Get tasks by project
GET    /ptm/api/v1/projects/:id/notes     # Get notes by project
PATCH  /ptm/api/v1/projects/:id           # Update project
DELETE /ptm/api/v1/projects/:id           # Delete project
```

**Tasks:**
```
POST   /ptm/api/v1/tasks                  # Create task
GET    /ptm/api/v1/tasks                  # List tasks by user
GET    /ptm/api/v1/tasks/:id              # Get task by ID
GET    /ptm/api/v1/tasks/:id/tree         # Get task tree
GET    /ptm/api/v1/tasks/:id/notes        # Get notes by task
PATCH  /ptm/api/v1/tasks/:id              # Update task
DELETE /ptm/api/v1/tasks/:id              # Delete task
```

**Notes:**
```
POST   /ptm/api/v1/notes                  # Create note
GET    /ptm/api/v1/notes/search           # Search notes
GET    /ptm/api/v1/notes/:id              # Get note by ID
PATCH  /ptm/api/v1/notes/:id              # Update note
DELETE /ptm/api/v1/notes/:id              # Delete note
```

**Schedule Management:**
```
POST   /ptm/api/v1/schedule-plans                        # Get or create active plan
GET    /ptm/api/v1/schedule-plans/active                 # Get active plan
GET    /ptm/api/v1/schedule-plans/active/detail          # Get active plan details
POST   /ptm/api/v1/schedule-plans/reschedule             # Trigger reschedule
POST   /ptm/api/v1/schedule-plans/:id/apply              # Apply proposed plan
POST   /ptm/api/v1/schedule-plans/:id/revert             # Revert to plan
DELETE /ptm/api/v1/schedule-plans/:id                    # Discard proposed plan

GET    /ptm/api/v1/schedule-tasks                        # List schedule tasks

GET    /ptm/api/v1/availability-calendar                 # Get availability
POST   /ptm/api/v1/availability-calendar                 # Set availability
PUT    /ptm/api/v1/availability-calendar                 # Replace availability

GET    /ptm/api/v1/schedule-windows                      # List availability windows
POST   /ptm/api/v1/schedule-windows/materialize          # Materialize windows

GET    /ptm/api/v1/schedule-events                       # List events
POST   /ptm/api/v1/schedule-events                       # Save events
POST   /ptm/api/v1/schedule-events/:id/move              # Move event
POST   /ptm/api/v1/schedule-events/:id/complete          # Complete event
POST   /ptm/api/v1/schedule-events/:id/split             # Split event
```

### Purchase Service Routes

```
ANY    /purchase/api/v1/*proxyPath        # Generic proxy to purchase service
```

### Logistics Service Routes

```
ANY    /logistics/api/v1/*proxyPath       # Generic proxy to logistics service
```

### Notification Service Routes

```
ANY    /notification/api/v1/*proxyPath    # Generic proxy to notification service
```

### Sales Service Routes

```
ANY    /sales/api/v1/*proxyPath           # Generic proxy to sales service
```

### Discuss Service Routes

```
ANY    /discuss/api/v1/*proxyPath         # Generic proxy to discuss service
```

## Configuration

### Environment Variables

Create a `.env` file in the service root:

```bash
# Keycloak Configuration
KEYCLOAK_URL=http://localhost:8180
CLIENT_SECRET=your_client_secret

# Backend Services
ACCOUNT_SERVICE_HOST=localhost
ACCOUNT_SERVICE_PORT=8081

CRM_SERVICE_HOST=localhost
CRM_SERVICE_PORT=8086

PTM_TASK_HOST=localhost
PTM_TASK_PORT=8083

PTM_SCHEDULE_HOST=localhost
PTM_SCHEDULE_PORT=8084

PURCHASE_SERVICE_HOST=localhost
PURCHASE_SERVICE_PORT=8088

LOGISTICS_SERVICE_HOST=localhost
LOGISTICS_SERVICE_PORT=8089

NOTIFICATION_SERVICE_HOST=localhost
NOTIFICATION_SERVICE_PORT=8090

SALES_SERVICE_HOST=localhost
SALES_SERVICE_PORT=8090

DISCUSS_SERVICE_HOST=localhost
DISCUSS_SERVICE_PORT=8092
```

### YAML Configuration

**default.yaml** - Base configuration:
```yaml
app:
  name: API Gateway
  port: 8080
```

**local.yaml** - Local development:
```yaml
app:
  logging:
    level: INFO
  redis:
    host: localhost
    port: 6379

  keycloak:
    url: ${KEYCLOAK_URL}
    realm: serp
    client-id: serp-api-gateway
    client-secret: ${CLIENT_SECRET}
    jwk-set-uri: ${KEYCLOAK_URL}/realms/serp/protocol/openid-connect/certs
    expected-issuer: ${KEYCLOAK_URL}/realms/serp

  cors:
    allowedOrigins:
      - http://localhost:3000
      - https://serp-soict.vercel.app
    allowedMethods:
      - GET
      - POST
      - PUT
      - DELETE
      - OPTIONS
    allowedHeaders:
      - Authorization
      - Content-Type
    allowCredentials: true
    maxAge: 3600

external:
  services:
    accountService:
      host: localhost
      port: "8081"
      timeout: 15s
    crmService:
      host: localhost
      port: "8086"
      timeout: 15s
    # ... other services
```

**production.yaml** - Production overrides:
```yaml
app:
  logging:
    level: WARN
  redis:
    host: redis-prod
    port: 6379

external:
  services:
    accountService:
      host: account-service
      port: "8081"
      timeout: 30s
    # ... production service addresses
```

## Request Flow

### Typical Request Flow

```
┌─────────┐
│ Client  │
│(Browser)│
└────┬────┘
     │ 1. HTTP Request + JWT Token
     │    GET /crm/api/v1/leads
     ▼
┌──────────────────────┐
│   API Gateway:8080   │
│                      │
│  ┌────────────────┐  │
│  │ CORS Middleware│  │ 2. Handle CORS
│  └───────┬────────┘  │
│          ▼           │
│  ┌────────────────┐  │
│  │ JWT Middleware │  │ 3. Validate JWT
│  │                │  │    - Check signature
│  │                │  │    - Extract claims
│  └───────┬────────┘  │
│          ▼           │
│  ┌────────────────┐  │
│  │ Auth Middleware│  │ 4. Check roles
│  └───────┬────────┘  │
│          ▼           │
│  ┌────────────────┐  │
│  │  Router        │  │ 5. Route to controller
│  └───────┬────────┘  │
│          ▼           │
│  ┌────────────────┐  │
│  │  Controller    │  │ 6. Call service
│  └───────┬────────┘  │
│          ▼           │
│  ┌────────────────┐  │
│  │Circuit Breaker │  │ 7. Check circuit state
│  └───────┬────────┘  │
└──────────┼───────────┘
           │ 8. Forward request
           ▼
┌────────────────────┐
│  CRM Service:8086  │  9. Process request
│                    │     - Business logic
│  ┌──────────────┐  │     - Database ops
│  │ Lead Service │  │
│  └──────────────┘  │
└────────┬───────────┘
         │ 10. Response
         ▼
┌──────────────────────┐
│   API Gateway:8080   │  11. Return to client
│                      │      - Transform response
│                      │      - Add headers
└────────┬─────────────┘
         │ 12. HTTP Response
         ▼
┌─────────┐
│ Client  │
└─────────┘
```

### Authentication Flow

```
1. User Login
   ┌──────┐     POST /api/v1/auth/login      ┌─────────────┐
   │Client├──────────────────────────────────►│API Gateway  │
   └──────┘                                   └──────┬──────┘
                                                     │
                                                     ▼
                                              ┌─────────────┐
                                              │Account      │
                                              │Service      │
                                              └──────┬──────┘
                                                     │
                                                     ▼
                                              ┌─────────────┐
                                              │Keycloak     │
                                              └──────┬──────┘
                                                     │
   ┌──────┐  ◄──────────────────────────────────────┘
   │Client│  { accessToken, refreshToken }
   └──────┘

2. Authenticated Request
   ┌──────┐     GET /crm/api/v1/leads         ┌─────────────┐
   │Client├─────Authorization: Bearer <token>─►│API Gateway  │
   └──────┘                                    └──────┬──────┘
                                                      │
                                               JWT Validation
                                                      │
                                                      ▼
                                               ┌─────────────┐
                                               │CRM Service  │
                                               │(with token) │
                                               └──────┬──────┘
                                                      │
   ┌──────┐  ◄──────────────────────────────────────┘
   │Client│  { leads: [...] }
   └──────┘
```

## Development Guidelines

### Code Style

**Go Conventions:**
- **Exported identifiers:** `PascalCase` (e.g., `ProxyController`, `AuthMiddleware`)
- **Unexported identifiers:** `camelCase` (e.g., `jwtUtils`, `validateToken`)
- **Interfaces:** `I` prefix (e.g., `IUserService`)
- **File names:** `snake_case` (e.g., `jwt_middleware.go`)
- **Test files:** `_test.go` suffix

**Import Organization:**
```go
import (
    // 1. Standard library
    "context"
    "fmt"
    "net/http"
    
    // 2. External packages
    "github.com/gin-gonic/gin"
    "go.uber.org/fx"
    
    // 3. Internal packages
    "github.com/serp/api-gateway/src/core/service"
    "github.com/serp/api-gateway/src/kernel/utils"
)
```

**File Header:**
All source files must include:
```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/
```

### Adding New Service Integration

To integrate a new backend service:

**1. Add Service Properties**

Edit `src/kernel/properties/external_service_properties.go`:
```go
type ExternalServiceProperties struct {
    // ... existing services
    NewService ServiceProperty
}
```

**2. Update Configuration**

Edit `src/config/local.yaml`:
```yaml
external:
  services:
    newService:
      host: localhost
      port: "8091"
      timeout: 15s
```

**3. Create Module**

Create `src/cmd/modules/new_service_module.go`:
```go
package modules

import (
    "go.uber.org/fx"
    // Import controllers, services, clients
)

func NewServiceModule() fx.Option {
    return fx.Options(
        // Register controllers
        fx.Provide(NewServiceController),
        // Register services
        fx.Provide(NewServiceService),
        // Register clients
        fx.Provide(NewServiceClientAdapter),
    )
}
```

**4. Register Module**

Edit `src/cmd/bootstrap/all.go`:
```go
func All() fx.Option {
    return fx.Options(
        CoreInfrastructure(),
        
        // ... existing modules
        modules.NewServiceModule(), // Add this line
        
        HttpServerModule(),
    )
}
```

**5. Create Router**

Create `src/ui/router/new_service_router.go`:
```go
package router

func RegisterNewServiceRoutes(
    group *gin.RouterGroup,
    controller *newservice.NewServiceController,
    middleware *middleware.JWTMiddleware,
) {
    serviceV1 := group.Group("/new-service/api/v1")
    serviceV1.Use(middleware.AuthenticateJWT())
    {
        serviceV1.GET("/resource", controller.GetResource)
        serviceV1.POST("/resource", controller.CreateResource)
    }
}
```

**6. Register Routes**

Edit `src/ui/router/router.go`:
```go
func RegisterGinRouters(p RegisterRoutersIn) {
    // ... existing routes
    
    RegisterNewServiceRoutes(
        group,
        p.NewServiceController,
        p.JWTMiddleware,
    )
}
```

**7. Add Generic Proxy (Optional)**

If using generic proxy pattern, edit `src/ui/controller/common/generic_proxy_controller.go`:
```go
func (c *GenericProxyController) ProxyToNewService(ctx *gin.Context) {
    target := fmt.Sprintf("http://%s:%s", c.props.NewService.Host, c.props.NewService.Port)
    c.proxyWithResilience(ctx, target, "/new-service/api/v1", "/new-service/api/v1", "newservice")
}
```

### Adding Circuit Breaker

Circuit breakers are automatically created for services in `GenericProxyController`. Configuration:

```go
settings := gobreaker.Settings{
    Name:        serviceName,
    MaxRequests: 3,              // Max requests in half-open state
    Interval:    60 * time.Second,    // Reset failure count interval
    Timeout:     30 * time.Second,    // Duration in open state
    ReadyToTrip: func(counts gobreaker.Counts) bool {
        failureRatio := float64(counts.TotalFailures) / float64(counts.Requests)
        return counts.Requests >= 3 && failureRatio >= 0.6
    },
}
```

### Testing Guidelines

**Controller Tests:**
```go
func TestProxyController(t *testing.T) {
    // Setup
    gin.SetMode(gin.TestMode)
    router := gin.New()
    
    // Create controller with mock dependencies
    controller := NewGenericProxyController(mockProps)
    
    // Register routes
    router.Any("/test/*proxyPath", controller.ProxyToService)
    
    // Create test request
    req := httptest.NewRequest("GET", "/test/resource", nil)
    w := httptest.NewRecorder()
    
    // Execute
    router.ServeHTTP(w, req)
    
    // Assert
    assert.Equal(t, http.StatusOK, w.Code)
}
```

**Service Tests:**
```go
func TestServiceMethod(t *testing.T) {
    // Use testify/mock for dependencies
    mockClient := new(MockClient)
    service := NewService(mockClient)
    
    // Setup expectations
    mockClient.On("GetData").Return(expectedData, nil)
    
    // Execute
    result, err := service.ProcessData()
    
    // Assert
    assert.NoError(t, err)
    assert.Equal(t, expectedResult, result)
    mockClient.AssertExpectations(t)
}
```

## Related Services

- **Account Service** (8081) - Authentication & authorization
- **CRM Service** (8086) - Customer relationship management
- **PTM Task** (8083) - Project & task management
- **PTM Schedule** (8084) - Schedule & calendar management
- **Purchase Service** (8088) - Procurement management
- **Logistics Service** (8089) - Warehouse & inventory
- **Notification Service** (8090) - Notifications & alerts
- **Sales Service** (8090) - Sales order management
- **Discuss Service** (8092) - Team communication
- **Frontend** (3000) - Next.js web application

## Contributing

1. Follow Go code style guidelines in `AGENTS.md`
2. Add tests for new features
3. Update configuration examples
4. Document new endpoints
5. Run tests before committing:
   ```bash
   go test ./...
   go fmt ./...
   go vet ./...
   ```

## License

This project is part of the SERP ERP system and is licensed under the MIT License. See the [LICENSE](../LICENSE) file in the root directory for details.

---

For more information about the overall SERP architecture, see the main repository README and `AGENTS.md`.
