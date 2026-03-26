# SERP - Smart ERP Microservices Architecture Guide

## Architecture Overview

Event-driven microservices ERP using **Clean Architecture**, **Kafka**, and **Keycloak**. All requests flow through API Gateway with JWT.

| Service | Port | Lang | Description |
|---------|------|------|-------------|
| `api_gateway` | 8080 | Go | JWT validation, routing (NO business logic) |
| `account` | 8081 | Java | User/auth/RBAC, Keycloak admin |
| `crm` | 8086 | Java | Customers, leads, opportunities |
| `ptm_task` | 8083 | Go | Personal task management |
| `ptm_schedule` | 8084 | Go | Calendar & scheduling |
| `ptm_optimization` | 8085 | Java | Task optimization |
| `purchase_service` | 8088 | Java | Purchase orders |
| `logistics` | 8089 | Java | Inventory & shipping |
| `notification_service` | 8090 | Go | Push notifications |
| `mailservice` | 8091 | Java | Email templates |
| `serp_llm` | 8089 | Python | AI/RAG (Gemini) |
| `discuss_service` | 8092 | Java | Discussions, attachments (S3), WebSockets |
| `sales` | 8087 | Go | Order management, quotations |
| `serp_web` | 3000 | TS | Next.js 15 + Redux + Shadcn |

