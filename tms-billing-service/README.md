# TMS Billing Service

Service tính phí vận chuyển và quản lý cấu hình giá của TMS: tariff, surcharge,
VAS, chiến lược tính giá và API public/admin cho shipping fee.

## Tổng quan

- Framework: Spring Boot 3.5, Java 21.
- Package chính: `serp.project.tms_billing_service`.
- Port mặc định: `8104`.
- Database mặc định: PostgreSQL `tms-billing-service`.
- Gateway path: `/tms-billing-service/api/v1/*`.
- Auth: OAuth2 Resource Server với Keycloak JWT.
- Tích hợp: Kafka, Goong geocode/distance matrix, S3-compatible storage.
- Migration: có dependency Flyway nhưng mặc định `spring.flyway.enabled=false`.

## Phạm vi nghiệp vụ

Module này xử lý các phần liên quan đến giá:

- Tính shipping fee.
- Bảng giá/tariff theo tiêu chuẩn nghiệp vụ.
- Phụ phí và dịch vụ gia tăng.
- Admin CRUD cho pricing rules.
- Chiến lược tính giá trong `core/service/impl`.

Không đặt logic vòng đời đơn, pickup, hub, bag hoặc payment gateway ở đây.

## Cấu trúc thư mục

```text
src/main/java/serp/project/tms_billing_service/
  ui/controller/
  core/service/
  core/service/impl/
  core/service/support/
  repository/
  domain/
  dto/request|response/
  exception/
  kernel/
src/main/resources/
  db/migration/
  i18n/
  application.yaml
```

## Yêu cầu môi trường

- JDK 21.
- PostgreSQL.
- Keycloak hoặc issuer tương thích JWT.
- Kafka nếu chạy luồng đồng bộ/event.
- Goong/S3 nếu dùng tính khoảng cách hoặc lưu file.

Các biến môi trường thường dùng:

```bash
SERVER_PORT=8104
DB_URL=jdbc:postgresql://localhost:5432/tms-billing-service
DB_USERNAME=serp
DB_PASSWORD=serp123
KEYCLOAK_URL=http://localhost:8180
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
GOONG_API_KEY=
STORAGE_S3_ACCESS_KEY=
STORAGE_S3_SECRET_KEY=
```

Không commit `.env` hoặc credential thật.

## Chạy local

Từ thư mục `tms-billing-service/`:

```bash
./mvnw spring-boot:run
```

Trên Windows PowerShell/CMD:

```bash
.\mvnw.cmd spring-boot:run
```

Nếu muốn dùng Flyway local, cần bật rõ:

```bash
SPRING_FLYWAY_ENABLED=true
```

Hãy kiểm tra trạng thái database trước khi bật migration tự động.

## Build và test

```bash
./mvnw clean compile
./mvnw test
./mvnw -Dtest=TieuChuanPricingStrategyTest test
./mvnw -DskipTests clean package
```

Khi sửa pricing rule hoặc strategy, ưu tiên bổ sung test quanh case tính phí.

## API

- Service base path: `/api/v1/{resource}`.
- Qua gateway: `http://localhost:8080/tms-billing-service/api/v1/{resource}`.
- Request/response dùng DTO, không expose JPA entity.
- API message lấy từ `i18n/messages*.properties` khi có message cho người dùng.

## Ghi chú phát triển

- Layer chính: `ui/controller` -> `core/service` -> `repository`.
- Shared pricing helper đặt trong `core/service/support`.
- Không hard-code magic number cho rule giá; ưu tiên enum/config/entity rõ nghĩa.
- Khi thay đổi contract API, cập nhật frontend TMS trong `serp_web` nếu đang dùng.
