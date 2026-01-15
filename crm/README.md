# CRM Service

## Overview

The **CRM Service** is a comprehensive Customer Relationship Management system that manages the complete customer lifecycle from lead acquisition to customer retention. Built with Java 21 and Spring Boot 3.5.6, it provides robust capabilities for lead management, opportunity tracking, customer accounts, contact management, activity tracking, and team collaboration.

**Technology Stack:**
- **Language**: Java 21
- **Framework**: Spring Boot 3.5.6
- **Database**: PostgreSQL (persistence), Redis (caching)
- **Messaging**: Apache Kafka
- **Authentication**: JWT via Keycloak (through API Gateway)
- **Architecture**: Clean Architecture / Hexagonal Architecture

**Service Configuration:**
- **Port**: 8086
- **Context Path**: `/crm`
- **Access**: All requests via API Gateway at `http://localhost:8080/crm`

## Architecture

The service follows Clean Architecture principles with clear separation of concerns:

```
crm/
├── ui/                          # Presentation Layer
│   └── controller/              # REST API controllers
│       ├── LeadController
│       ├── OpportunityController
│       ├── CustomerController
│       ├── ContactController
│       ├── ActivityController
│       ├── TeamController
│       └── SearchController
│
├── core/                        # Business Logic Layer
│   ├── domain/                  # Domain models
│   │   ├── entity/              # Business entities
│   │   ├── dto/                 # Data transfer objects
│   │   └── constant/            # Domain constants & enums
│   ├── usecase/                 # Business use cases
│   │   ├── LeadUseCase
│   │   ├── OpportunityUseCase
│   │   ├── CustomerUseCase
│   │   ├── ContactUseCase
│   │   ├── ActivityUseCase
│   │   ├── TeamUseCase
│   │   └── SearchUseCase
│   └── port/                    # Interface definitions
│       ├── store/               # Repository interfaces
│       ├── cache/               # Cache interfaces
│       └── mail/                # Mail service interfaces
│
└── infrastructure/              # External Adapters Layer
    ├── store/                   # PostgreSQL repositories
    │   ├── model/               # JPA entities
    │   └── repository/          # Repository implementations
    ├── cache/                   # Redis cache implementations
    └── mail/                    # Email service implementations
```

**Data Flow:**
1. **Request**: API Gateway → Controller (JWT validation)
2. **Authorization**: Extract tenantId & userId from JWT
3. **Business Logic**: Controller → UseCase → Service Layer
4. **Persistence**: Service → Port → Repository → PostgreSQL
5. **Caching**: Redis for frequently accessed data
6. **Response**: DTO mapping → JSON response

## Core Features

### 1. Lead Management
Capture and qualify potential customers through the sales funnel:
- **Lead Capture**: Create leads from multiple sources (web, email, phone, referral, social media, events)
- **Lead Scoring**: Track engagement and qualification criteria
- **Lead Qualification**: Workflow to qualify leads based on BANT (Budget, Authority, Need, Timeline)
- **Lead Nurturing**: Track communication history and engagement activities
- **Lead Conversion**: Convert qualified leads into customers with associated opportunities and contacts
- **Lead Status Tracking**: NEW → CONTACTED → NURTURING → QUALIFIED → CONVERTED/DISQUALIFIED

### 2. Opportunity Management
Track sales opportunities through the pipeline:
- **Sales Pipeline**: Manage opportunities through PROSPECTING → QUALIFICATION → PROPOSAL → NEGOTIATION → CLOSED_WON/CLOSED_LOST
- **Deal Tracking**: Monitor opportunity value, probability, expected close date
- **Stage Management**: Update opportunity stages with automatic validation
- **Close Management**: Close opportunities as won or lost with reason tracking
- **Forecasting**: Track expected revenue and close dates
- **Opportunity Association**: Link opportunities to customers and originating leads

### 3. Customer Management
Manage customer accounts with hierarchical structure:
- **Account Creation**: Create individual, business, or enterprise customer accounts
- **Hierarchical Structure**: Support parent-child customer relationships
- **Customer Types**: INDIVIDUAL, BUSINESS, ENTERPRISE
- **Customer Profiles**: Store comprehensive customer information (industry, size, revenue, etc.)
- **Multi-Contact Support**: Multiple contacts per customer account
- **Activity History**: Track all customer interactions and touchpoints

### 4. Contact Management
Manage individual contacts within customer organizations:
- **Contact Profiles**: Store contact details (name, title, email, phone, department)
- **Customer Association**: Link contacts to customer accounts
- **Primary Contact**: Designate primary contact per customer
- **Contact Roles**: Track contact positions and decision-making authority
- **Contact Activity**: View interaction history per contact
- **Bulk Operations**: Manage multiple contacts across customer base

### 5. Activity Tracking
Track all customer interactions and tasks:
- **Activity Types**: CALL, MEETING, EMAIL, TASK
- **Activity Scheduling**: Create and schedule future activities
- **Activity Status**: SCHEDULED → COMPLETED/CANCELLED
- **Activity Association**: Link activities to leads, opportunities, customers, or contacts
- **Activity Management**: Complete, cancel, update, or delete activities
- **Activity History**: Comprehensive timeline of all customer touchpoints
- **Team Assignment**: Assign activities to team members

### 6. Team Management
Organize sales and support teams:
- **Team Creation**: Create sales teams, support teams, or specialized groups
- **Team Membership**: Add/remove team members with role assignments
- **Member Roles**: Define roles and responsibilities within teams
- **Team Hierarchy**: Support team leader and member structures
- **Workload Distribution**: Track team member assignments and capacity
- **Performance Tracking**: Monitor team and individual performance metrics

### 7. Global Search
Cross-entity search functionality:
- **Unified Search**: Search across leads, opportunities, customers, contacts, activities, and teams
- **Quick Lookup**: Fast keyword-based search with configurable result limits
- **Multi-Entity Results**: Return results from all relevant entity types
- **Tenant Isolation**: Search results filtered by tenant for data security

## API Routes

All routes are prefixed with `/crm/api/v1` when accessed through the API Gateway.

### Lead Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/leads` | Create a new lead |
| PATCH | `/leads/{id}` | Update an existing lead |
| GET | `/leads/{id}` | Get lead by ID |
| GET | `/leads` | Get all leads (paginated, default page=1, size=20) |
| DELETE | `/leads/{id}` | Delete a lead |
| POST | `/leads/{id}/qualify` | Qualify a lead (change status to QUALIFIED) |
| POST | `/leads/{id}/convert` | Convert lead to customer, contact, and opportunity |

**Query Parameters for GET `/leads`:**
- `page` (default: 1) - Page number
- `size` (default: 20) - Items per page

### Opportunity Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/opportunities` | Create a new opportunity |
| PATCH | `/opportunities/{id}` | Update an existing opportunity |
| GET | `/opportunities/{id}` | Get opportunity by ID |
| GET | `/opportunities` | Get all opportunities (paginated) |
| DELETE | `/opportunities/{id}` | Delete an opportunity |
| POST | `/opportunities/{id}/close` | Close opportunity (won or lost) |

**Query Parameters for GET `/opportunities`:**
- `page` (default: 1) - Page number
- `size` (default: 20) - Items per page

### Customer Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/customers` | Create a new customer |
| PATCH | `/customers/{id}` | Update an existing customer |
| GET | `/customers/{id}` | Get customer by ID |
| GET | `/customers` | Get all customers (paginated) |
| DELETE | `/customers/{id}` | Delete a customer |

**Query Parameters for GET `/customers`:**
- `page` (default: 1) - Page number
- `size` (default: 20) - Items per page

### Contact Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/customers/{customerId}/contacts` | Create contact for a customer |
| PATCH | `/customers/{customerId}/contacts/{id}` | Update a contact |
| GET | `/customers/{customerId}/contacts/{id}` | Get contact by ID |
| GET | `/customers/{customerId}/contacts` | Get all contacts for a customer |
| DELETE | `/customers/{customerId}/contacts/{id}` | Delete a contact |
| GET | `/contacts` | Get all contacts across all customers (paginated) |

**Query Parameters for GET `/contacts`:**
- `page` (default: 1) - Page number
- `size` (default: 20) - Items per page

### Activity Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/activities` | Create a new activity |
| PUT | `/activities/{id}` | Update an existing activity |
| GET | `/activities/{id}` | Get activity by ID |
| GET | `/activities` | Get all activities (paginated) |
| POST | `/activities/{id}/complete` | Mark activity as completed |
| POST | `/activities/{id}/cancel` | Cancel an activity |
| DELETE | `/activities/{id}` | Delete an activity |

**Query Parameters for GET `/activities`:**
- `page` (default: 1) - Page number
- `size` (default: 20) - Items per page

### Team Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/teams` | Create a new team |
| PUT | `/teams/{id}` | Update an existing team |
| GET | `/teams/{id}` | Get team by ID |
| GET | `/teams` | Get all teams (paginated) |
| DELETE | `/teams/{id}` | Delete a team |
| GET | `/teams/{id}/members` | Get team members (paginated) |
| POST | `/teams/{id}/members` | Add a member to the team |
| PATCH | `/teams/{id}/members/{memberId}` | Update team member details |
| DELETE | `/teams/{id}/members/{memberId}` | Remove member from team |

**Query Parameters:**
- `page` (default: 1) - Page number
- `size` (default: 20) - Items per page

### Search

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/search?q={query}&limit={limit}` | Global search across all entities |

**Query Parameters:**
- `q` (required) - Search query string
- `limit` (default: 5) - Maximum results per entity type

## Domain Models

### Core Entities

#### LeadEntity
Represents potential customers in the sales funnel.

**Key Attributes:**
- `id` - Unique identifier
- `tenantId` - Multi-tenancy identifier
- `firstName`, `lastName` - Lead contact details
- `email`, `phone` - Contact information
- `company`, `jobTitle` - Professional details
- `source` - Lead source (WEB, EMAIL, PHONE, REFERRAL, SOCIAL_MEDIA, EVENT, OTHER)
- `status` - Lead status (NEW, CONTACTED, NURTURING, QUALIFIED, CONVERTED, DISQUALIFIED)
- `score` - Lead scoring value (0-100)
- `industry`, `estimatedValue` - Qualification data
- `assignedUserId` - User responsible for the lead
- `notes` - Additional information
- `qualifiedAt`, `convertedAt` - Workflow timestamps

**Lifecycle:**
```
NEW → CONTACTED → NURTURING → QUALIFIED → CONVERTED
                                      ↓
                                 DISQUALIFIED
```

#### OpportunityEntity
Represents qualified sales opportunities.

**Key Attributes:**
- `id` - Unique identifier
- `tenantId` - Multi-tenancy identifier
- `name` - Opportunity name
- `customerId` - Associated customer
- `leadId` - Originating lead (if converted)
- `stage` - Pipeline stage (PROSPECTING, QUALIFICATION, PROPOSAL, NEGOTIATION, CLOSED_WON, CLOSED_LOST)
- `amount` - Deal value
- `probability` - Win probability (0-100)
- `expectedCloseDate` - Forecasted close date
- `actualCloseDate` - Actual close date
- `assignedUserId` - Opportunity owner
- `description`, `notes` - Opportunity details
- `lostReason` - Reason if CLOSED_LOST

**Sales Pipeline:**
```
PROSPECTING → QUALIFICATION → PROPOSAL → NEGOTIATION → CLOSED_WON
                                                    ↓
                                                CLOSED_LOST
```

#### CustomerEntity
Represents customer accounts with hierarchical support.

**Key Attributes:**
- `id` - Unique identifier
- `tenantId` - Multi-tenancy identifier
- `name` - Customer/company name
- `type` - Customer type (INDIVIDUAL, BUSINESS, ENTERPRISE)
- `industry` - Industry sector
- `email`, `phone`, `website` - Contact information
- `address`, `city`, `state`, `country`, `postalCode` - Address details
- `annualRevenue`, `employeeCount` - Company metrics
- `parentCustomerId` - Parent customer for hierarchies
- `assignedUserId` - Account manager
- `notes` - Additional information

#### ContactEntity
Represents individual contacts within customer organizations.

**Key Attributes:**
- `id` - Unique identifier
- `tenantId` - Multi-tenancy identifier
- `customerId` - Associated customer
- `firstName`, `lastName` - Contact name
- `email`, `phone`, `mobilePhone` - Contact information
- `jobTitle`, `department` - Professional details
- `isPrimary` - Primary contact flag
- `address`, `city`, `state`, `country`, `postalCode` - Contact address
- `notes` - Additional information

#### ActivityEntity
Represents customer interactions and tasks.

**Key Attributes:**
- `id` - Unique identifier
- `tenantId` - Multi-tenancy identifier
- `type` - Activity type (CALL, MEETING, EMAIL, TASK)
- `status` - Activity status (SCHEDULED, COMPLETED, CANCELLED)
- `subject` - Activity subject/title
- `description` - Detailed description
- `scheduledAt` - Scheduled date/time
- `completedAt` - Completion timestamp
- `duration` - Activity duration (minutes)
- `assignedUserId` - Responsible user
- `leadId`, `opportunityId`, `customerId`, `contactId` - Associated entities
- `notes`, `outcome` - Activity results

#### TeamEntity
Represents sales or support teams.

**Key Attributes:**
- `id` - Unique identifier
- `tenantId` - Multi-tenancy identifier
- `name` - Team name
- `description` - Team purpose/description
- `teamLeaderId` - Team leader user ID
- `isActive` - Active status

#### TeamMemberEntity
Represents team membership assignments.

**Key Attributes:**
- `id` - Unique identifier
- `tenantId` - Multi-tenancy identifier
- `teamId` - Associated team
- `userId` - Team member user ID
- `role` - Member role within team
- `joinedAt` - Membership start date

### Key Enumerations

#### LeadStatus
- `NEW` - Newly created lead
- `CONTACTED` - Initial contact made
- `NURTURING` - Lead being nurtured through marketing/sales activities
- `QUALIFIED` - Lead meets qualification criteria
- `CONVERTED` - Lead converted to customer/opportunity
- `DISQUALIFIED` - Lead does not meet criteria

#### LeadSource
- `WEB` - Website form or inquiry
- `EMAIL` - Email campaign or direct email
- `PHONE` - Phone inquiry
- `REFERRAL` - Customer or partner referral
- `SOCIAL_MEDIA` - Social media channels
- `EVENT` - Trade show, conference, or event
- `OTHER` - Other sources

#### OpportunityStage
- `PROSPECTING` - Initial research and outreach
- `QUALIFICATION` - Qualifying opportunity (BANT)
- `PROPOSAL` - Proposal submitted
- `NEGOTIATION` - Negotiating terms and pricing
- `CLOSED_WON` - Opportunity won
- `CLOSED_LOST` - Opportunity lost

#### ActivityType
- `CALL` - Phone call
- `MEETING` - In-person or virtual meeting
- `EMAIL` - Email communication
- `TASK` - To-do task or action item

#### ActivityStatus
- `SCHEDULED` - Activity scheduled for future
- `COMPLETED` - Activity completed
- `CANCELLED` - Activity cancelled

#### CustomerType
- `INDIVIDUAL` - Individual consumer
- `BUSINESS` - Small/medium business
- `ENTERPRISE` - Large enterprise customer

### Key Workflows

#### Lead Conversion Workflow
When a lead is converted (POST `/leads/{id}/convert`):
1. Lead status changed to `CONVERTED`
2. New `CustomerEntity` created from lead data
3. New `ContactEntity` created linked to customer
4. New `OpportunityEntity` created linked to customer and original lead
5. Conversion timestamp recorded (`convertedAt`)

#### Opportunity Close Workflow
When an opportunity is closed (POST `/opportunities/{id}/close`):
1. Stage updated to `CLOSED_WON` or `CLOSED_LOST`
2. `actualCloseDate` set to current timestamp
3. If lost, `lostReason` recorded
4. Probability automatically updated (100 for won, 0 for lost)

#### Activity Completion Workflow
When an activity is completed (POST `/activities/{id}/complete`):
1. Status changed to `COMPLETED`
2. `completedAt` timestamp recorded
3. Activity outcome can be documented in `notes`

## Configuration

### Environment Variables

The service requires the following environment variables (typically configured in `.env`):

```bash
# Server Configuration
SERVER_PORT=8086

# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/serp_crm
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password

# Redis Configuration
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# Kafka Configuration
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# JWT/Keycloak Configuration
JWT_PUBLIC_KEY=your_jwt_public_key
KEYCLOAK_REALM=serp
KEYCLOAK_AUTH_SERVER_URL=http://localhost:8180

# Mail Configuration (for future notification features)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_email_password
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
```

### Application Configuration

Key configurations from `application.yml`:

```yaml
server:
  port: ${SERVER_PORT:8086}
  servlet:
    context-path: /crm

spring:
  datasource:
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  data:
    redis:
      repositories:
        enabled: true
  
  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

### Database Migrations

Database schema is managed using Flyway migrations located in `src/main/resources/db/migration/`. Migrations run automatically on application startup.

**Migration Naming Convention:**
- `V{version}__{description}.sql`
- Example: `V1__init_schema.sql`, `V2__add_lead_scoring.sql`

## Development Guidelines

### Clean Architecture Principles

1. **Controller Layer**: Handle HTTP requests, extract JWT context, delegate to use cases
   ```java
   Long tenantId = authUtils.getCurrentTenantId().orElse(null);
   Long userId = authUtils.getCurrentUserId().orElse(null);
   var response = leadUseCase.createLead(request, userId, tenantId);
   ```

2. **Use Case Layer**: Implement business logic, orchestrate services, manage transactions
   - Use `@Transactional` annotation on use case methods
   - Validate business rules before persistence
   - Return standardized response DTOs

3. **Service Layer**: Domain-specific operations, interact with repositories
   - Implement business logic specific to domain entities
   - Use repository ports for data access

4. **Repository Layer**: Data access implementations
   - Extend Spring Data JPA repositories
   - Custom queries use JPQL or native SQL

### Coding Standards

**Entity Design:**
- All entities extend `BaseEntity` (provides `createdAt`, `updatedAt`, `createdBy`, `updatedBy`)
- Use `@SuperBuilder` for builder pattern support
- Include `tenantId` for multi-tenancy isolation

**DTO Validation:**
- Use Jakarta Validation annotations (`@Valid`, `@NotNull`, `@Email`, etc.)
- Separate request/response DTOs for different operations
- Naming convention: `Create{Entity}Request`, `Update{Entity}Request`, `{Entity}Response`

**Error Handling:**
- Use custom `AppException` with error constants
- Return appropriate HTTP status codes
- Provide meaningful error messages

**Multi-Tenancy:**
- Always filter queries by `tenantId`
- Extract `tenantId` from JWT in controllers
- Enforce tenant isolation at database level

### Testing

Write tests for:
- **Use Cases**: Business logic validation
- **Services**: Domain-specific operations
- **Repositories**: Custom queries and data access
- **Controllers**: API endpoint integration tests

### Database Conventions

- Table names: lowercase with underscores (e.g., `lead`, `opportunity`, `customer`)
- Foreign keys: `{entity}_id` (e.g., `customer_id`, `lead_id`)
- Indexes on: `tenant_id`, foreign keys, frequently queried fields
- Audit fields: `created_at`, `updated_at`, `created_by`, `updated_by`

## Multi-Tenancy

The CRM service enforces strict multi-tenancy isolation:

- **Tenant Identification**: `tenantId` extracted from JWT token in every request
- **Data Isolation**: All database queries filtered by `tenantId`
- **Entity Design**: All entities include `tenantId` field
- **Repository Layer**: Automatic tenant filtering in queries
- **Security**: Users can only access data within their tenant

**Example Tenant Filtering:**
```java
@Query("SELECT l FROM LeadModel l WHERE l.tenantId = :tenantId")
List<LeadModel> findAllByTenantId(@Param("tenantId") Long tenantId);
```

## Integration Points

### API Gateway
- All external requests route through API Gateway (port 8080)
- JWT authentication enforced at gateway level
- Context path: `/crm` prepended to all routes

### Keycloak
- User authentication and authorization
- JWT token generation and validation
- Multi-tenant realm support

### PostgreSQL
- Primary data store for all CRM entities
- Schema: `crm` (or service-specific schema)
- Connection pooling via HikariCP

### Redis
- Caching layer for frequently accessed data
- Session management
- Query result caching

## License

This project is part of the SERP ERP system and is licensed under the MIT License. See the [LICENSE](../LICENSE) file in the root directory for details.

## Author

**QuanTuanHuy**  
Part of Serp Project
