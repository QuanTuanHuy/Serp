# 📦 Logistics Service

> Comprehensive Inventory and Warehouse Management System for the SERP ERP Platform

A full-featured logistics and supply chain microservice built with Spring Boot 4.0 that manages real-time inventory tracking, warehouse operations, shipment processing, and product distribution across multiple facilities. Designed for enterprise-scale operations with multi-tenant support and role-based access control.

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

- **📊 Real-Time Inventory Management** - Track stock levels, locations, and movements across facilities
- **🏭 Multi-Facility Support** - Manage multiple warehouses, distribution centers, and storage locations
- **🚚 Shipment Processing** - Complete shipment lifecycle from creation to import/receiving
- **📦 Product Catalog** - Centralized product database with SKU management and categorization
- **🔄 Stock Movement Tracking** - Monitor inventory transfers between facilities
- **📍 Address Management** - Flexible address system for facilities, suppliers, and customers
- **👥 Customer & Supplier Integration** - Read-only access to customer and supplier data
- **📋 Order Visibility** - View purchase and sales orders for logistics coordination
- **🔐 JWT Authentication** - OAuth2 resource server with Keycloak integration
- **🎯 Role-Based Access Control (RBAC)** - Separate permissions for Admin and Employee roles
- **🏢 Multi-Tenant Architecture** - Built-in tenant isolation for enterprise deployments
- **📄 Pagination & Advanced Filtering** - Search capabilities across all inventory and shipment data
- **📝 Comprehensive Logging** - SLF4J logging for audit trails and debugging
- **📖 OpenAPI Documentation** - Auto-generated Swagger UI for API exploration

---

## 🛠️ Technology Stack

### Core Technologies

- **Language**: Java 21
- **Framework**: Spring Boot 4.0.0
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
│  (Business Logic, Inventory Rules, Validation)  │
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
   cd logistics
   ```

2. **Configure environment variables**

   Create a `.env` file in the project root:

   ```bash
   # Database Configuration
   DATABASE_URL=jdbc:postgresql://localhost:5432/serp_db
   DATABASE_USERNAME=postgres
   DATABASE_PASSWORD=your_password

   # Server Configuration
   SERVER_PORT=8089

   # Keycloak Configuration
   KEYCLOAK_URL=http://localhost:8180
   KEYCLOAK_CLIENT_SECRET=your-client-secret
   KEYCLOAK_ADMIN_USER=admin
   KEYCLOAK_ADMIN_PASSWORD=admin
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

   The service should start on `http://localhost:8089`

   Check Swagger UI: `http://localhost:8089/swagger-ui.html`

---

## ⚙️ Configuration

### Environment Variables

| Variable                     | Description                       | Required | Default         |
| ---------------------------- | --------------------------------- | -------- | --------------- |
| `DATABASE_URL`               | PostgreSQL JDBC connection string | ✅ Yes   | -               |
| `DATABASE_USERNAME`          | Database username                 | ✅ Yes   | -               |
| `DATABASE_PASSWORD`          | Database password                 | ✅ Yes   | -               |
| `SERVER_PORT`                | HTTP server port                  | ❌ No    | `8089`          |
| `KEYCLOAK_URL`               | Keycloak server base URL          | ✅ Yes   | -               |
| `KEYCLOAK_CLIENT_SECRET`     | OAuth2 client secret              | ✅ Yes   | -               |
| `KEYCLOAK_ADMIN_USER`        | Keycloak admin username           | ✅ Yes   | -               |
| `KEYCLOAK_ADMIN_PASSWORD`    | Keycloak admin password           | ✅ Yes   | -               |
| `SECURITY_ROLE_SERP_SERVICE` | Internal service role             | ❌ No    | `SERP_SERVICES` |

### Application Configuration

The service is configured via `application.yml` with the following key sections:

- **Security**: JWT validation, JWKS endpoint, role-based access control
- **Database**: JPA/Hibernate settings, connection pooling
- **API Documentation**: Swagger UI configuration
- **Logging**: Log levels and audit trail formats

---

## 📖 API Reference

The service exposes a comprehensive REST API at base path: `/logistics/api/v1`

### Quick Access

- **Swagger UI**: `http://localhost:8089/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8089/v3/api-docs`

### Core API Modules

| Module              | Endpoint Prefix   | Description                                  |
| ------------------- | ----------------- | -------------------------------------------- |
| **Inventory Items** | `/inventory-item` | Real-time stock tracking and management      |
| **Shipments**       | `/shipment`       | Shipment creation, updates, and imports      |
| **Products**        | `/product`        | Product catalog and SKU management           |
| **Facilities**      | `/facility`       | Warehouse and distribution center management |
| **Categories**      | `/category`       | Product categorization hierarchy             |
| **Orders**          | `/order`          | Read-only purchase/sales order visibility    |
| **Suppliers**       | `/supplier`       | Read-only supplier directory                 |
| **Customers**       | `/customer`       | Read-only customer information               |
| **Addresses**       | `/address`        | Address management for entities              |

### Example Requests

**Create Shipment**

```bash
POST /logistics/api/v1/shipment/create
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "shipmentName": "Weekly Restock - Warehouse A",
  "supplierId": "SUP-001",
  "destinationFacilityId": "FAC-001",
  "shipmentItems": [
    {
      "productId": "PROD-001",
      "quantity": 500,
      "unitPrice": 12.99
    }
  ],
  "expectedArrivalDate": "2026-02-20",
  "shippingMethod": "TRUCK"
}
```

**Import Shipment (Receive Goods)**

```bash
PATCH /logistics/api/v1/shipment/manage/{shipmentId}/import
Authorization: Bearer <JWT_TOKEN>
```

**Search Inventory Items**

```bash
GET /logistics/api/v1/inventory-item/search?facilityId=FAC-001&productId=PROD-001&page=0&size=20
Authorization: Bearer <JWT_TOKEN>
```

**Create Inventory Item**

```bash
POST /logistics/api/v1/inventory-item/create
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "productId": "PROD-001",
  "facilityId": "FAC-001",
  "quantity": 1000,
  "minStockLevel": 100,
  "maxStockLevel": 2000,
  "reorderPoint": 200
}
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

| Role                 | Permissions    | Description                                              |
| -------------------- | -------------- | -------------------------------------------------------- |
| `LOGISTICS_ADMIN`    | Full access    | Create, read, update, delete all resources               |
| `LOGISTICS_EMPLOYEE` | Read & execute | View inventory, process shipments, limited modifications |

### Protected Endpoints

All `/logistics/api/v1/**` endpoints require authentication except:

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

- All DTOs use Jakarta Bean Validation (`@Valid`, `@NotNull`, `@Min`, etc.)
- Custom validators in `validator/` package

### Database Migrations

Migrations are managed by **Flyway** and located in:

```
src/main/resources/db/migration/
```

**Naming convention**: `V{version}__{description}.sql`

Example: `V1__create_inventory_items_table.sql`

---

## 🐳 Docker Deployment

### Build Docker Image

```bash
docker build -t logistics-service:latest .
```

### Run Container

```bash
docker run -d \
  --name logistics-service \
  -p 8089:8089 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/serp_db \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=password \
  -e SERVER_PORT=8089 \
  -e KEYCLOAK_URL=http://keycloak:8180 \
  -e KEYCLOAK_CLIENT_SECRET=your-secret \
  logistics-service:latest
```

### Docker Compose Integration

This service is designed to run as part of the SERP microservices platform:

```bash
# From workspace root
docker-compose -f docker-compose.dev.yml up -d logistics
```

---

## 📁 Project Structure

```
logistics/
├── src/
│   ├── main/
│   │   ├── java/serp/project/logistics/
│   │   │   ├── config/              # Spring configuration classes
│   │   │   ├── constant/            # Constants and enums
│   │   │   ├── controller/          # REST API controllers
│   │   │   │   ├── AddressController.java
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── CustomerController.java
│   │   │   │   ├── FacilityController.java
│   │   │   │   ├── InventoryItemController.java  # Core inventory
│   │   │   │   ├── OrderController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── ShipmentController.java       # Core shipment
│   │   │   │   └── SupplierController.java
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── request/         # Request DTOs
│   │   │   │   └── response/        # Response DTOs
│   │   │   ├── entity/              # JPA entities
│   │   │   ├── exception/           # Custom exceptions
│   │   │   ├── repository/          # Spring Data repositories
│   │   │   ├── service/             # Business logic
│   │   │   ├── util/                # Utility classes (AuthUtils)
│   │   │   ├── validator/           # Custom validators
│   │   │   └── LogisticsApplication.java
│   │   └── resources/
│   │       ├── application.yml      # Application configuration
│   │       └── db/migration/        # Flyway migrations
│   └── test/                        # Test classes
├── api-documents/                   # OpenAPI documentation
├── .env                             # Environment variables (local)
├── Dockerfile                       # Multi-stage Docker build
├── pom.xml                          # Maven dependencies
└── run-dev.sh                       # Development startup script
```

---

## 🔄 Integration Points

This service integrates with other SERP microservices:

- **Purchase Service** (port 8088) - Receives purchase order data
- **Sales Service** - Receives sales order data for fulfillment
- **CRM** (port 8086) - Customer information lookup
- **API Gateway** (port 8080) - Routes all production requests

---

## 📝 Notes

- This service is part of the **SERP ERP Platform** microservices architecture
- All requests must flow through the **API Gateway** (port 8080) in production
- Multi-tenancy is enforced at the application level via tenant ID in JWT claims
- Database schema is versioned and managed through Flyway migrations
- Inventory updates are logged for audit trail purposes
- Shipment import operations update inventory levels automatically

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
5. Check inventory levels and facility configuration

---

## 📊 Key Operations

### Inventory Management Flow

1. **Create Inventory Item** → Define stock for a product at a facility
2. **Receive Shipment** → Import shipment to increase inventory
3. **Track Stock Levels** → Monitor min/max levels and reorder points
4. **Transfer Stock** → Move inventory between facilities (via shipments)

### Shipment Processing Flow

1. **Create Shipment** → Define incoming goods from supplier
2. **Update Shipment** → Modify items or details before import
3. **Import Shipment** → Receive goods and update inventory
4. **Track Status** → Monitor shipment lifecycle

---

**Version**: 0.2.1  
**Last Updated**: January 2026  
**Service Port**: 8089  
**Base Path**: `/logistics/api/v1`
