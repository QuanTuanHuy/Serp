# Account Service

Account Service is a core microservice in the SERP ERP system responsible for authentication, authorization, user management, organization management, and subscription handling.

## Overview

Account Service provides centralized identity and access management for the entire SERP platform, integrating with Keycloak for authentication and managing role-based access control (RBAC), multi-tenancy, and subscription-based features.

**Technology Stack:**
- Java 21
- Spring Boot 3.5.5
- PostgreSQL (database)
- Redis (caching)
- Apache Kafka (event streaming)
- Keycloak 26.0.5 (identity provider)
- Flyway (database migrations)

**Port:** 8081  
**Context Path:** `/account-service`

## Architecture

This service follows **Clean Architecture** principles with clear separation of concerns:

```
src/main/java/serp/project/account/
├── ui/
│   └── controller/           # REST API controllers (presentation layer)
├── core/
│   ├── usecase/             # Business logic orchestration
│   ├── service/             # Domain services (business rules)
│   ├── port/                # Interfaces for external dependencies
│   │   ├── store/           # Database port interfaces
│   │   └── client/          # External client port interfaces
│   ├── domain/              # Domain models and DTOs
│   │   ├── entity/          # Domain entities
│   │   ├── dto/             # Data transfer objects
│   │   ├── enums/           # Enumerations
│   │   └── constant/        # Constants
│   ├── exception/           # Custom exceptions
│   └── seed/                # Data initialization
├── infrastructure/
│   ├── store/               # Database implementations
│   │   ├── repository/      # JPA repositories
│   │   └── specification/   # Query specifications
│   └── client/              # External client adapters
│       ├── KeycloakAdapter
│       ├── KafkaProducerAdapter
│       └── RedisCacheAdapter
└── kernel/
    └── utils/               # Utility classes
```

## Core Features

### 1. Authentication & Authorization
- User registration and login
- JWT token management (access/refresh tokens)
- Password management (change password, reset)
- Integration with Keycloak for SSO
- Token revocation

**Key Endpoints:**
```
POST /api/v1/auth/register       - Register new user
POST /api/v1/auth/login          - User login
POST /api/v1/auth/get-token      - Get access token
POST /api/v1/auth/refresh-token  - Refresh access token
POST /api/v1/auth/revoke-token   - Revoke token
POST /api/v1/auth/change-password - Change user password
```

### 2. User Management
- User CRUD operations
- User profile management
- User status management (active/inactive/suspended)
- Role assignment to users
- Multi-organization user support

**Key Endpoints:**
```
GET  /api/v1/users                  - List users
GET  /api/v1/users/profile/me       - Get current user profile
GET  /api/v1/users/{id}/info        - Get user info
POST /api/v1/users/assign-roles     - Assign roles to user
```

### 3. Organization Management
- Organization CRUD operations
- Multi-tenancy support
- Organization-user relationship
- Organization status tracking
- Department hierarchy within organizations

**Key Endpoints:**
```
GET  /api/v1/organizations/me                      - Get current user's organization
GET  /api/v1/organizations/{orgId}/users           - List organization users
POST /api/v1/organizations/{orgId}/users           - Add user to organization
PUT  /api/v1/organizations/{orgId}/users/{userId}/status - Update user status
```

### 4. Role & Permission Management
- Dynamic role creation and assignment
- Fine-grained permission control
- Role scopes (SYSTEM, ORGANIZATION, MODULE)
- Permission types (CREATE, READ, UPDATE, DELETE, EXECUTE)
- Role-permission mapping

**Predefined Roles:**
- `SUPER_ADMIN` - System administrator
- `SYSTEM_MODERATOR` - System moderator
- `ORG_OWNER` - Organization owner
- `ORG_ADMIN` - Organization administrator
- `ORG_USER` - Organization user

**Key Endpoints:**
```
GET  /api/v1/roles              - List roles
POST /api/v1/roles              - Create role
GET  /api/v1/permissions        - List permissions
POST /api/v1/permissions        - Create permission
POST /api/v1/roles/{roleId}/permissions - Add permission to role
```

### 5. Module Access Management
- Module-based feature access control
- User-module access assignment
- Organization-module subscription tracking
- Bulk user assignment to modules

**Key Endpoints:**
```
GET  /api/v1/organizations/{orgId}/modules                     - List organization modules
GET  /api/v1/organizations/{orgId}/modules/{moduleId}/access   - Check module access
POST /api/v1/organizations/{orgId}/modules/{moduleId}/users    - Assign user to module
POST /api/v1/organizations/{orgId}/modules/{moduleId}/users/bulk - Bulk assign users
GET  /api/v1/organizations/{orgId}/users/me/modules            - Get current user's modules
```

### 6. Subscription Plan Management
- Subscription plan CRUD
- Plan features and module configuration
- Pricing models (FREE, PER_USER, PER_MODULE, FLAT_RATE, CUSTOM)
- Billing cycles (MONTHLY, QUARTERLY, ANNUAL)
- Subscription lifecycle management

**Subscription Statuses:**
- `ACTIVE` - Active subscription
- `TRIAL` - Trial period
- `EXPIRED` - Subscription expired
- `CANCELLED` - Cancelled subscription
- `SUSPENDED` - Suspended subscription

**Key Endpoints:**
```
GET  /api/v1/subscription-plans                    - List subscription plans
POST /api/v1/subscription-plans                    - Create subscription plan
POST /api/v1/subscription-plans/{planId}/modules   - Add module to plan
GET  /api/v1/subscriptions/me                      - Get current organization subscription
POST /api/v1/subscriptions/subscribe               - Subscribe to plan
POST /api/v1/subscriptions/trial                   - Start trial
POST /api/v1/subscriptions/upgrade                 - Upgrade subscription
POST /api/v1/subscriptions/cancel                  - Cancel subscription
```

### 7. Department Management
- Hierarchical department structure (tree)
- Department-user assignment
- Department statistics (member count)
- Department CRUD operations

**Key Endpoints:**
```
GET  /api/v1/organizations/{orgId}/departments                        - List departments
POST /api/v1/organizations/{orgId}/departments                        - Create department
GET  /api/v1/organizations/{orgId}/departments/{deptId}               - Get department details
PUT  /api/v1/organizations/{orgId}/departments/{deptId}               - Update department
DELETE /api/v1/organizations/{orgId}/departments/{deptId}             - Delete department
GET  /api/v1/organizations/{orgId}/departments/{deptId}/tree          - Get department tree
GET  /api/v1/organizations/{orgId}/departments/{deptId}/members       - List department members
POST /api/v1/organizations/{orgId}/departments/{deptId}/members       - Add member to department
```

### 8. Menu Display Management
- Dynamic menu configuration
- Role-based menu visibility
- Menu hierarchy and ordering
- Support for different menu types (PAGE, DROPDOWN, LINK, DIVIDER)

**Key Endpoints:**
```
GET  /api/v1/menu-displays              - List menu displays
POST /api/v1/menu-displays              - Create menu display
POST /api/v1/menu-displays/{menuId}/roles - Assign menu to roles
```

### 9. Keycloak Integration
- User synchronization with Keycloak
- Role mapping (Keycloak ↔ SERP)
- Group management in Keycloak
- Realm and client role management

**Key Endpoints:**
```
GET  /api/v1/keycloak/users             - List Keycloak users
POST /api/v1/keycloak/users             - Create Keycloak user
POST /api/v1/keycloak/roles/realm       - Create realm role
POST /api/v1/keycloak/roles/client      - Create client role
POST /api/v1/keycloak/groups            - Create group
```

## Database Schema

The service manages the following main entities:

- **users** - User accounts
- **organizations** - Tenant organizations
- **user_organizations** - User-organization relationships
- **roles** - System and custom roles
- **permissions** - Fine-grained permissions
- **role_permissions** - Role-permission mappings
- **user_roles** - User-role assignments
- **modules** - System modules/features
- **user_module_access** - User module access control
- **subscription_plans** - Subscription plan definitions
- **subscription_plan_modules** - Plan-module associations
- **organization_subscriptions** - Active subscriptions
- **departments** - Organizational departments
- **user_departments** - Department membership
- **menu_displays** - Dynamic menu configurations
- **menu_display_roles** - Menu-role visibility mappings

Database migrations are managed by Flyway and located in `src/main/resources/db/migration/`.

## Event-Driven Communication

Account Service publishes events to Kafka for cross-service communication:

**Published Events:**
- `user.created` - New user created
- `user.updated` - User information updated
- `user.deleted` - User deleted
- `organization.created` - New organization created
- `subscription.created` - New subscription created
- `subscription.updated` - Subscription status changed
- `notification.create` - Notification request

Events are sent to topic: `account-events`

## API Documentation

### Authentication Flow

1. **User Registration:**
   ```bash
   POST /account-service/api/v1/auth/register
   {
     "email": "user@example.com",
     "password": "password123",
     "firstName": "John",
     "lastName": "Doe"
   }
   ```

2. **Login:**
   ```bash
   POST /account-service/api/v1/auth/login
   {
     "email": "user@example.com",
     "password": "password123"
   }
   ```
   Returns JWT access token and refresh token.

3. **Authenticated Requests:**
   Include the access token in the Authorization header:
   ```
   Authorization: Bearer <access_token>
   ```

### Role-Based Access Control

The service implements URL-based access control defined in `application.yml`:

- **Public URLs:** No authentication required (login, register, etc.)
- **Protected URLs:** Require specific roles and/or permissions

Example:
```yaml
protectedUrls:
  - url-pattern: /api/v1/admin/**
    roles:
      - SUPER_ADMIN
      - SYSTEM_MODERATOR
```

## Development Guidelines

### Code Style

- **File Headers:** All source files must include author/description comments
- **Imports:** Organize as: Jakarta/javax → Lombok → Spring → Internal
- **Naming:** 
  - Classes/Interfaces: `PascalCase`
  - Methods/Variables: `camelCase`
  - Interfaces: `I` prefix (e.g., `IUserService`)
  - DTOs: Descriptive suffix (e.g., `CreateUserRequest`)
- **Annotations:** Use Lombok (`@RequiredArgsConstructor`, `@Getter`, `@Setter`, etc.)

### Clean Architecture Rules

1. **Controller → UseCase → Service → Port → Adapter** flow must be strictly followed
2. **Domain entities** are separate from **persistence models**
3. **Error handling:** Use custom `AppException` with error constants
4. **Transactions:** Use `@Transactional` on use case methods
5. **Context extraction:** Use `authUtils.getCurrentUserId()` and `authUtils.getCurrentTenantId()`

### Adding New Features

1. **Create Domain Entity** (if needed): `core/domain/entity/`
2. **Define DTOs**: `core/domain/dto/request/` and `core/domain/dto/response/`
3. **Create Port Interface**: `core/port/store/` or `core/port/client/`
4. **Implement Service**: `core/service/impl/`
5. **Create UseCase**: `core/usecase/`
6. **Add Controller**: `ui/controller/`
7. **Implement Adapter**: `infrastructure/store/` or `infrastructure/client/`
8. **Register in Configuration** (if needed)

## Related Services

- **API Gateway** (port 8080) - Routes requests to this service
- **Notification Service** - Consumes notification events
- **All other SERP services** - Authenticate through this service

## Contributing

1. Follow the code style guidelines in `AGENTS.md`
2. Write tests for new features
3. Run lint and tests before committing:
   ```bash
   ./mvnw test
   ./mvnw clean package
   ```
4. Use meaningful commit messages

## License

This project is part of the SERP ERP system and is licensed under the MIT License. See the [LICENSE](../LICENSE) file in the root directory for details.

---

For more information about the overall SERP architecture, see the main repository README.
