# 💰 Sales Service

> End-to-End Sales Order Management and Customer Relationship System for the SERP ERP Platform

A comprehensive sales management microservice built with Spring Boot 4.0 that handles the complete sales cycle from customer acquisition to order fulfillment. Manages customer relationships, product catalogs, sales orders, inventory coordination, and multi-facility operations with enterprise-grade security and multi-tenant architecture.

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

- **👥 Customer Management** - Complete customer lifecycle from creation to relationship tracking
- **📝 Sales Order Processing** - End-to-end order management from quotation to fulfillment
- **✅ Order Approval Workflow** - Multi-stage approval and cancellation capabilities
- **🛍️ Product Catalog** - Centralized product database with pricing and categorization
- **📦 Inventory Integration** - Real-time inventory visibility and stock management
- **🏭 Multi-Facility Support** - Manage sales across multiple warehouses and locations
- **📍 Address Management** - Flexible address system for customers and facilities
- **🏷️ Category Hierarchy** - Product categorization for better organization
- **💼 Customer Profiles** - Detailed customer information with contact and address history
- **🔐 JWT Authentication** - OAuth2 resource server with Keycloak integration
- **🎯 Role-Based Access Control (RBAC)** - Granular permissions for Admin, Manager, and Staff
- **🏢 Multi-Tenant Architecture** - Built-in tenant isolation for enterprise deployments
- **📄 Pagination & Advanced Search** - Powerful filtering across customers and orders
- **📝 Comprehensive Logging** - Audit trails for compliance and debugging
- **📖 OpenAPI Documentation** - Auto-generated Swagger UI for API exploration

---

## 🛠️ Technology Stack

### Core Technologies

- **Language**: Java 21
- **Framework**: Spring Boot 4.0.1 (Latest)
- **Build Tool**: Maven 3.9+

### Key Dependencies

| Category          | Technology              | Purpose                        |
| ----------------- | ----------------------- | ------------------------------ |
| **Persistence**   | Spring Data JPA         | Database abstraction & ORM     |
|                   | PostgreSQL Driver       | Relational database            |
|                   | Flyway                  | Database migrations            |
| **Security**      | Spring Security         | Authentication & authorization |
|                   | OAuth2 Resource Server  | JWT token validation           |
|                   | Keycloak                | Identity & access management   |
| **Documentation** | SpringDoc OpenAPI 2.8.8 | Swagger UI generation          |
| **Utilities**     | Lombok                  | Boilerplate reduction          |
| **Validation**    | Jakarta Validation      | Request validation             |
| **Testing**       | Spring Boot Test        | Integration testing            |
|                   | Spring Security Test    | Security testing               |
|                   | Spring Data JPA Test    | Repository testing             |

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
│  (Business Logic, Sales Rules, Validation)      │
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
- **Multi-Tenancy**: Tenant ID filtering at service layer

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
   cd sales
   ```

2. **Configure environment variables**

   Set the following environment variables (via `.env` file or shell):

   ```bash
   # Database Configuration
   export DATABASE_URL=jdbc:postgresql://localhost:5432/serp_db
   export DATABASE_USERNAME=postgres
   export DATABASE_PASSWORD=your_password

   # Server Configuration
   export SERVER_PORT=8090

   # Keycloak Configuration
   export KEYCLOAK_URL=http://localhost:8180
   export KEYCLOAK_CLIENT_SECRET=your-client-secret
   export KEYCLOAK_ADMIN_USER=admin
   export KEYCLOAK_ADMIN_PASSWORD=admin

   # Optional: Connection Pool Settings
   export CONNECTION_TIMEOUT=30000
   export MAX_LIFETIME=1800000
   export IDLE_TIMEOUT=600000
   export VALIDATION_TIMEOUT=5000
   ```

3. **Run database migrations**

   Migrations run automatically on startup via Flyway.

4. **Start the service**

   **Option A: Using Maven wrapper (recommended)**

   ```bash
   ./mvnw spring-boot:run
   ```

   **Option B: Build and run JAR**

   ```bash
   ./mvnw clean package
   java -jar target/sales-0.1.1.jar
   ```

   **Option C: Using Windows**

   ```powershell
   # Set environment variables first
   $env:DATABASE_URL="jdbc:postgresql://localhost:5432/serp_db"
   $env:DATABASE_USERNAME="postgres"
   $env:DATABASE_PASSWORD="your_password"
   $env:SERVER_PORT="8090"
   $env:KEYCLOAK_URL="http://localhost:8180"
   $env:KEYCLOAK_CLIENT_SECRET="your-secret"

   # Run the application
   .\mvnw.cmd spring-boot:run
   ```

5. **Verify the service is running**

   The service should start on `http://localhost:8090`

   Check Swagger UI: `http://localhost:8090/swagger-ui.html`

---

## ⚙️ Configuration

### Environment Variables

#### Database Configuration

| Variable             | Description                        | Required | Default   |
| -------------------- | ---------------------------------- | -------- | --------- |
| `DATABASE_URL`       | PostgreSQL JDBC connection string  | ✅ Yes   | -         |
| `DATABASE_USERNAME`  | Database username                  | ✅ Yes   | -         |
| `DATABASE_PASSWORD`  | Database password                  | ✅ Yes   | -         |
| `CONNECTION_TIMEOUT` | Database connection timeout (ms)   | ❌ No    | `30000`   |
| `MAX_LIFETIME`       | Max connection lifetime (ms)       | ❌ No    | `1800000` |
| `IDLE_TIMEOUT`       | Idle connection timeout (ms)       | ❌ No    | `600000`  |
| `VALIDATION_TIMEOUT` | Connection validation timeout (ms) | ❌ No    | `5000`    |

#### Server Configuration

| Variable      | Description      | Required | Default |
| ------------- | ---------------- | -------- | ------- |
| `SERVER_PORT` | HTTP server port | ❌ No    | `8090`  |

#### Keycloak Configuration

| Variable                     | Description              | Required | Default         |
| ---------------------------- | ------------------------ | -------- | --------------- |
| `KEYCLOAK_URL`               | Keycloak server base URL | ✅ Yes   | -               |
| `KEYCLOAK_CLIENT_SECRET`     | OAuth2 client secret     | ✅ Yes   | -               |
| `KEYCLOAK_ADMIN_USER`        | Keycloak admin username  | ✅ Yes   | -               |
| `KEYCLOAK_ADMIN_PASSWORD`    | Keycloak admin password  | ✅ Yes   | -               |
| `SECURITY_ROLE_SERP_SERVICE` | Internal service role    | ❌ No    | `SERP_SERVICES` |

### Application Configuration

The service uses multiple configuration files:

- **application.yaml** - Main Spring Boot configuration
- **database.properties** - Database connection settings
- **keycloak.properties** - Keycloak integration details

Configuration includes:

- **Security**: JWT validation, JWKS endpoint, role-based access control
- **Database**: JPA/Hibernate settings, connection pooling with HikariCP
- **API Documentation**: Swagger UI configuration
- **Logging**: SQL logging and audit trail formats

---

## 📖 API Reference

The service exposes a comprehensive REST API at base path: `/sales/api/v1`

### Quick Access

- **Swagger UI**: `http://localhost:8090/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8090/v3/api-docs`

### Core API Modules

| Module              | Endpoint Prefix   | Description                              |
| ------------------- | ----------------- | ---------------------------------------- |
| **Orders**          | `/order`          | Sales order CRUD, approval, cancellation |
| **Customers**       | `/customer`       | Customer management and profiles         |
| **Products**        | `/product`        | Product catalog and pricing              |
| **Inventory Items** | `/inventory-item` | Inventory visibility and tracking        |
| **Facilities**      | `/facility`       | Warehouse and facility management        |
| **Categories**      | `/category`       | Product categorization hierarchy         |
| **Addresses**       | `/address`        | Address management for entities          |

### Example Requests

**Create Sales Order**

```bash
POST /sales/api/v1/order/create
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "orderName": "Q1 Enterprise License - ACME Corp",
  "customerId": "CUST-001",
  "facilityId": "FAC-001",
  "orderItems": [
    {
      "productId": "PROD-001",
      "quantity": 50,
      "unitPrice": 299.99
    }
  ],
  "expectedDeliveryDate": "2026-02-28",
  "notes": "Urgent order for new client"
}
```

**Approve Order**

```bash
PATCH /sales/api/v1/order/manage/{orderId}/approve
Authorization: Bearer <JWT_TOKEN>
```

**Create Customer**

```bash
POST /sales/api/v1/customer/create
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "name": "ACME Corporation",
  "email": "contact@acme.com",
  "phone": "+1-555-0123",
  "taxId": "12-3456789",
  "statusId": "ACTIVE",
  "addresses": [
    {
      "street": "123 Main Street",
      "city": "San Francisco",
      "state": "CA",
      "zipCode": "94102",
      "country": "USA",
      "type": "BILLING"
    }
  ]
}
```

**Search Orders**

```bash
GET /sales/api/v1/order/search?page=0&size=20&customerId=CUST-001&status=PENDING
Authorization: Bearer <JWT_TOKEN>
```

**Get Customer Details**

```bash
GET /sales/api/v1/customer/search/{customerId}
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

| Role            | Permissions          | Description                                                |
| --------------- | -------------------- | ---------------------------------------------------------- |
| `SALES_ADMIN`   | Full access          | Create, read, update, delete all resources, approve orders |
| `SALES_MANAGER` | Read, approve        | View all data, approve orders, limited modifications       |
| `SALES_STAFF`   | Read, create, update | Daily operations, order creation, customer management      |

### Protected Endpoints

All `/sales/api/v1/**` endpoints require authentication except:

- Swagger documentation (`/swagger-ui.html`, `/v3/api-docs`)
- Public health checks (if configured)

### Keycloak Integration

The service integrates with Keycloak for:

- **JWT Validation**: JWKS endpoint verification
- **Role Mapping**: Keycloak roles to application permissions
- **Multi-Tenancy**: Tenant ID from token claims
- **Admin Operations**: Keycloak Admin API for user management

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

**Build Docker image**

```bash
docker build -t sales-service:latest .
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

- All DTOs use Jakarta Bean Validation (`@Valid`, `@NotNull`, `@Min`, etc.)
- Custom validators in `validator/` package

**SQL Logging**

- Set `spring.jpa.show-sql=true` in database.properties for query debugging

### Database Migrations

Migrations are managed by **Flyway** and located in:

```
src/main/resources/db/migration/
```

**Naming convention**: `V{version}__{description}.sql`

Example: `V1__create_customers_table.sql`

---

## 🐳 Docker Deployment

### Build Docker Image

```bash
docker build -t sales-service:latest .
```

### Run Container

```bash
docker run -d \
  --name sales-service \
  -p 8090:8090 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/serp_db \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=password \
  -e SERVER_PORT=8090 \
  -e KEYCLOAK_URL=http://keycloak:8180 \
  -e KEYCLOAK_CLIENT_SECRET=your-secret \
  -e KEYCLOAK_ADMIN_USER=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  sales-service:latest
```

### Docker Compose Integration

This service is designed to run as part of the SERP microservices platform:

```bash
# From workspace root
docker-compose -f docker-compose.dev.yml up -d sales
```

---

## 📁 Project Structure

```
sales/
├── src/
│   ├── main/
│   │   ├── java/serp/project/sales/
│   │   │   ├── config/              # Spring configuration classes
│   │   │   ├── constant/            # Constants and enums
│   │   │   ├── controller/          # REST API controllers
│   │   │   │   ├── AddressController.java
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── CustomerController.java      # Core customer mgmt
│   │   │   │   ├── FacilityController.java
│   │   │   │   ├── InventoryItemController.java
│   │   │   │   ├── OrderController.java         # Core sales orders
│   │   │   │   └── ProductController.java
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── request/         # Request DTOs
│   │   │   │   └── response/        # Response DTOs
│   │   │   ├── entity/              # JPA entities
│   │   │   ├── exception/           # Custom exceptions
│   │   │   ├── repository/          # Spring Data repositories
│   │   │   ├── service/             # Business logic
│   │   │   ├── util/                # Utility classes (AuthUtils)
│   │   │   ├── validator/           # Custom validators
│   │   │   └── SalesApplication.java
│   │   └── resources/
│   │       ├── application.yaml     # Main configuration
│   │       ├── database.properties  # Database settings
│   │       ├── keycloak.properties  # Keycloak integration
│   │       └── db/migration/        # Flyway migrations
│   └── test/                        # Test classes
├── api-documents/                   # OpenAPI documentation
├── Dockerfile                       # Multi-stage Docker build
├── pom.xml                          # Maven dependencies
└── mvnw / mvnw.cmd                  # Maven wrapper
```

---

## 🔄 Integration Points

This service integrates with other SERP microservices:

- **CRM** (port 8086) - Customer data synchronization
- **Logistics** (port 8089) - Inventory and shipment coordination
- **Purchase Service** (port 8088) - Product information
- **API Gateway** (port 8080) - Routes all production requests

---

## 📝 Notes

- This service is part of the **SERP ERP Platform** microservices architecture
- All requests must flow through the **API Gateway** (port 8080) in production
- Multi-tenancy is enforced at the application level via tenant ID in JWT claims
- Database schema is versioned and managed through Flyway migrations
- Order approval/cancellation operations are logged for audit trails
- Inventory checks are performed before order approval

---

## 📄 License

Part of the SERP Project - Enterprise Resource Planning Platform

**Author**: QuanTuanHuy

---

## 🆘 Support

For issues or questions:

1. Check the [API documentation](./api-documents/README.md)
2. Review application logs for detailed error information
3. Verify Keycloak configuration and JWT token validity
4. Ensure database connectivity and schema migrations
5. Check customer and product existence before creating orders

---

## 📊 Key Operations

### Sales Order Flow

1. **Create Order** → Define sales order with customer and products
2. **Add Items** → Add or modify order items
3. **Approve Order** → Manager/Admin approval for processing
4. **Fulfill Order** → Coordinate with Logistics for delivery
5. **Cancel Order** → Handle cancellations with reason tracking

### Customer Management Flow

1. **Create Customer** → Add new customer with profile information
2. **Manage Addresses** → Add billing and shipping addresses
3. **Update Profile** → Modify customer details and status
4. **Track Orders** → View customer order history

---

**Version**: 0.1.1  
**Last Updated**: January 2026  
**Service Port**: 8090  
**Base Path**: `/sales/api/v1`
