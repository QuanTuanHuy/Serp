# 🛒 Purchase Service

> Enterprise-grade Purchase Management Microservice for the SERP ERP Platform

A comprehensive procurement and supply chain management microservice built with Spring Boot that handles end-to-end purchase order lifecycle, supplier management, product catalog, inventory facilities, and shipment tracking. Designed for multi-tenant environments with role-based access control.

---

## 📋 Table of Contents

- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [Architecture Overview](#-architecture-overview)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [API Reference](#-api-reference)
- [Security & Authentication](#-security--authentication)
- [Development](#-development)
- [Docker Deployment](#-docker-deployment)
- [Project Structure](#-project-structure)

---

## ✨ Key Features

- **📦 Purchase Order Management** - Complete lifecycle management from creation to approval/cancellation
- **👥 Supplier Management** - Comprehensive supplier directory with contact information and addresses
- **🏷️ Product Catalog** - Centralized product database with categorization and inventory tracking
- **🏭 Facility Management** - Multi-location warehouse and facility management
- **🚚 Shipment Tracking** - Real-time shipment status and logistics coordination
- **📍 Address Management** - Flexible address system for suppliers, facilities, and shipments
- **🔐 JWT Authentication** - OAuth2 resource server with Keycloak integration
- **🎯 Role-Based Access Control (RBAC)** - Granular permissions for Staff, Manager, and Admin roles
- **🏢 Multi-Tenant Support** - Built-in tenant isolation for enterprise deployments
- **📊 Pagination & Filtering** - Advanced search capabilities across all entities
- **📝 Comprehensive Logging** - SLF4J logging for monitoring and debugging
- **📖 OpenAPI Documentation** - Auto-generated Swagger UI for API exploration

---

## 🛠️ Technology Stack

### Core Technologies

- **Language**: Java 21
- **Framework**: Spring Boot 3.5.7
- **Build Tool**: Maven 3.9+

### Key Dependencies

| Category          | Technology             | Purpose                        |
| ----------------- | ---------------------- | ------------------------------ |
| **Persistence**   | Spring Data JPA        | Database abstraction & ORM     |
|                   | PostgreSQL Driver      | Relational database            |
|                   | Flyway                 | Database migrations            |
| **Security**      | Spring Security        | Authentication & authorization |
|                   | OAuth2 Resource Server | JWT token validation           |
|                   | Keycloak               | Identity & access management   |
| **Documentation** | SpringDoc OpenAPI      | Swagger UI generation          |
| **Utilities**     | Lombok                 | Boilerplate reduction          |
| **Validation**    | Jakarta Validation     | Request validation             |
| **Testing**       | Spring Boot Test       | Integration testing            |
|                   | Spring Security Test   | Security testing               |

### Infrastructure

- **Database**: PostgreSQL 14+
- **Authentication**: Keycloak (OAuth2/OpenID Connect)
- **Deployment**: Docker (multi-stage build)

---

## 🏗️ Architecture Overview

This service follows **Clean Architecture** principles with clear separation of concerns:

```
┌─────────────────────────────────────────────────┐
│           Controller Layer                       │
│  (REST APIs, Request/Response DTOs)             │
└───────────────┬─────────────────────────────────┘
                │
┌───────────────▼─────────────────────────────────┐
│           Service Layer                          │
│  (Business Logic, Validation, Orchestration)    │
└───────────────┬─────────────────────────────────┘
                │
┌───────────────▼─────────────────────────────────┐
│           Repository Layer                       │
│  (Spring Data JPA, Database Access)             │
└───────────────┬─────────────────────────────────┘
                │
┌───────────────▼─────────────────────────────────┐
│           PostgreSQL Database                    │
└─────────────────────────────────────────────────┘
```

### Design Patterns

- **Dependency Injection**: Constructor-based injection with Lombok `@RequiredArgsConstructor`
- **DTO Pattern**: Separate request/response DTOs from domain entities
- **Repository Pattern**: Spring Data JPA repositories for data access
- **Validation**: Jakarta Bean Validation with custom validators
- **Exception Handling**: Centralized error handling with custom exceptions

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.9+** (or use included `mvnw` wrapper)
- **PostgreSQL 14+** running and accessible
- **Keycloak** instance configured with the `serp` realm

### Installation

1. **Clone the repository** (if not already in workspace)

   ```bash
   cd purchase_service
   ```

2. **Configure environment variables**

   Create a `.env` file in the project root:

   ```bash
   # Database Configuration
   DATABASE_URL=jdbc:postgresql://localhost:5432/serp_db
   DATABASE_USERNAME=postgres
   DATABASE_PASSWORD=your_password

   # Keycloak Configuration
   KEYCLOAK_URL=http://localhost:8180
   KEYCLOAK_CLIENT_SECRET=your-client-secret
   KEYCLOAK_ADMIN_USER=admin
   KEYCLOAK_ADMIN_PASSWORD=admin

   # Optional: Server Port (defaults to 8088)
   SERVER_PORT=8088
   ```

3. **Run database migrations**

   Migrations run automatically on startup via Flyway.

4. **Start the service**

   **Option A: Using the provided script (loads .env automatically)**

   ```bash
   chmod +x run-dev.sh
   ./run-dev.sh
   ```

   **Option B: Using Maven directly**

   ```bash
   # Load environment variables first
   export $(cat .env | xargs)

   # Run the application
   ./mvnw spring-boot:run
   ```

   **Option C: Using Windows (PowerShell)**

   ```powershell
   # Load .env variables
   Get-Content .env | ForEach-Object {
       if ($_ -match '^([^=]+)=(.*)$') {
           [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2])
       }
   }

   # Run the application
   .\mvnw.cmd spring-boot:run
   ```

5. **Verify the service is running**

   The service should start on `http://localhost:8088`

   Check health: `curl http://localhost:8088/purchase-service/api/v1/health` (if available)

---

## ⚙️ Configuration

### Environment Variables

| Variable                     | Description                       | Required | Default         |
| ---------------------------- | --------------------------------- | -------- | --------------- |
| `DATABASE_URL`               | PostgreSQL JDBC connection string | ✅ Yes   | -               |
| `DATABASE_USERNAME`          | Database username                 | ✅ Yes   | -               |
| `DATABASE_PASSWORD`          | Database password                 | ✅ Yes   | -               |
| `KEYCLOAK_URL`               | Keycloak server base URL          | ✅ Yes   | -               |
| `KEYCLOAK_CLIENT_SECRET`     | OAuth2 client secret              | ✅ Yes   | -               |
| `KEYCLOAK_ADMIN_USER`        | Keycloak admin username           | ✅ Yes   | -               |
| `KEYCLOAK_ADMIN_PASSWORD`    | Keycloak admin password           | ✅ Yes   | -               |
| `SERVER_PORT`                | HTTP server port                  | ❌ No    | `8088`          |
| `SECURITY_ROLE_SERP_SERVICE` | Internal service role             | ❌ No    | `SERP_SERVICES` |

### Application Configuration

The service is configured via `application.yml` with the following key sections:

- **Security**: JWT validation, JWKS endpoint, role-based access control
- **Database**: JPA/Hibernate settings, connection pooling
- **API Documentation**: Swagger UI configuration
- **Logging**: Log levels and formats

---

## 📖 API Reference

The service exposes a comprehensive REST API at base path: `/purchase-service/api/v1`

### Quick Access

- **Swagger UI**: `http://localhost:8088/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8088/v3/api-docs`

### Core API Modules

| Module         | Endpoint Prefix | Description                                 |
| -------------- | --------------- | ------------------------------------------- |
| **Orders**     | `/order`        | Purchase order CRUD, approval, cancellation |
| **Suppliers**  | `/supplier`     | Supplier management and directory           |
| **Products**   | `/product`      | Product catalog and inventory               |
| **Categories** | `/category`     | Product categorization                      |
| **Facilities** | `/facility`     | Warehouse and facility management           |
| **Shipments**  | `/shipment`     | Shipment tracking and status                |
| **Addresses**  | `/address`      | Address management for entities             |

### Example Requests

**Create Purchase Order**

```bash
POST /purchase-service/api/v1/order/create
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "orderName": "Q1 Office Supplies",
  "supplierId": "SUP-001",
  "facilityId": "FAC-001",
  "orderItems": [
    {
      "productId": "PROD-001",
      "quantity": 100,
      "unitPrice": 15.99
    }
  ],
  "expectedDeliveryDate": "2026-02-15"
}
```

**Get Orders with Pagination**

```bash
GET /purchase-service/api/v1/order/search?page=0&size=20&status=PENDING
Authorization: Bearer <JWT_TOKEN>
```

For complete API documentation, refer to the [api-documents](./api-documents/README.md) directory.

---

## 🔐 Security & Authentication

### Authentication Flow

1. Client obtains JWT token from Keycloak (`/realms/serp/protocol/openid-connect/token`)
2. Client includes token in request: `Authorization: Bearer <token>`
3. Service validates token against Keycloak JWKS endpoint
4. User ID and Tenant ID are extracted from token claims
5. Role-based authorization is enforced

### Roles & Permissions

| Role               | Permissions          | Description                                |
| ------------------ | -------------------- | ------------------------------------------ |
| `PURCHASE_ADMIN`   | Full access          | Create, read, update, delete all resources |
| `PURCHASE_MANAGER` | Read, approve        | View all data, approve orders              |
| `PURCHASE_STAFF`   | Read, create, update | Daily operations, limited deletion         |

### Protected Endpoints

All `/purchase-service/api/v1/**` endpoints require authentication except:

- Swagger documentation (`/swagger-ui.html`, `/v3/api-docs`)
- Public health checks (if configured)

---

## 🔧 Development

### Building the Project

**Compile and package**

```bash
./mvnw clean package
```

**Skip tests**

```bash
./mvnw clean package -DskipTests
```

### Running Tests

```bash
./mvnw test
```

### Code Quality

**Lombok Configuration**

- Annotation processing is enabled automatically
- IDE plugins required: Install Lombok plugin for IntelliJ IDEA/Eclipse

**Validation**

- All DTOs use Jakarta Bean Validation (`@Valid`, `@NotNull`, etc.)
- Custom validators in `validator/` package

### Database Migrations

Migrations are managed by **Flyway** and located in:

```
src/main/resources/db/migration/
```

**Naming convention**: `V{version}__{description}.sql`

Example: `V1__create_orders_table.sql`

---

## 🐳 Docker Deployment

### Build Docker Image

```bash
docker build -t purchase-service:latest .
```

### Run Container

```bash
docker run -d \
  --name purchase-service \
  -p 8088:8088 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/serp_db \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=password \
  -e KEYCLOAK_URL=http://keycloak:8180 \
  -e KEYCLOAK_CLIENT_SECRET=your-secret \
  purchase-service:latest
```

### Docker Compose Integration

This service is designed to run as part of the SERP microservices platform:

```bash
# From workspace root
docker-compose -f docker-compose.dev.yml up -d purchase_service
```

---

## 📁 Project Structure

```
purchase_service/
├── src/
│   ├── main/
│   │   ├── java/serp/project/purchase_service/
│   │   │   ├── config/           # Spring configuration classes
│   │   │   ├── constant/         # Constants and enums
│   │   │   ├── controller/       # REST API controllers
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   │   ├── request/      # Request DTOs
│   │   │   │   └── response/     # Response DTOs
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── exception/        # Custom exceptions
│   │   │   ├── repository/       # Spring Data repositories
│   │   │   ├── service/          # Business logic
│   │   │   ├── util/             # Utility classes
│   │   │   ├── validator/        # Custom validators
│   │   │   └── PurchaseServiceApplication.java
│   │   └── resources/
│   │       ├── application.yml   # Application configuration
│   │       └── db/migration/     # Flyway migrations
│   └── test/                     # Test classes
├── api-documents/                # OpenAPI documentation
├── .env                          # Environment variables (local)
├── Dockerfile                    # Multi-stage Docker build
├── pom.xml                       # Maven dependencies
└── run-dev.sh                    # Development startup script
```

---

## 📝 Notes

- This service is part of the **SERP ERP Platform** microservices architecture
- All requests must flow through the **API Gateway** (port 8080) in production
- Multi-tenancy is enforced at the application level via tenant ID in JWT claims
- Database schema is versioned and managed through Flyway migrations
- Logging uses SLF4J with Logback implementation

---

## 📄 License

Part of the SERP Project - Enterprise Resource Planning Platform

**Author**: QuanTuanHuy

---

## 🆘 Support

For issues or questions:

1. Check the [API documentation](./api-documents/README.md)
2. Review application logs: `./logs/` directory
3. Verify Keycloak configuration and JWT token validity
4. Ensure database connectivity and schema migrations

---

**Version**: 0.2.1  
**Last Updated**: January 2026  
**Service Port**: 8088  
**Base Path**: `/purchase-service/api/v1`
