# Second-Mile Service

Backend TMS cho chặng giữa: quản lý hub, xe hub, tuyến vận chuyển, bag,
bag-order và các luồng bàn giao giữa bưu cục và hub.

## Tổng quan

- Framework: Spring Boot 3.5, Java 21.
- Package chính: `serp.project.second_mile`.
- Port mặc định: `8102`.
- Database mặc định: PostgreSQL `second-mile`.
- Gateway path: `/second-mile/api/v1/*`.
- Auth: OAuth2 Resource Server với Keycloak JWT.
- Tích hợp: Kafka, Goong geocode/distance matrix, S3-compatible storage,
  `tms-order`, `first-mile`.

## Phạm vi nghiệp vụ

Module này xử lý nghiệp vụ second-mile:

- Hub, nhân sự hub, mapping hub-bưu cục.
- Xe vận hành tại hub.
- Route cố định, tuyến linehaul.
- Bag, bag-order, xử lý hàng tại hub.
- Đồng bộ order, user, hub-post-office, handover manifest qua Kafka.

Pickup trip, courier check-in và xe bưu cục thuộc `first-mile`. Trạng thái đơn
hàng chuẩn thuộc `tms-order`.

## Cấu trúc thư mục

```text
src/main/java/serp/project/second_mile/
  ui/controller/
  service/
  service/impl/
  service/handler/
  repository/
  repository/specification/
  domain/
  dto/request|response/
  enums/
  exception/
  kernel/config|utils/
  kafka/
  caller/
src/main/resources/
  db.migration/           SQL schema scripts
  excel/                  Import templates
  i18n/
  application.yaml
```

Lưu ý: thư mục migration dùng tên `db.migration` có dấu chấm.

## Yêu cầu môi trường

- JDK 21.
- PostgreSQL.
- Kafka nếu chạy luồng đồng bộ.
- Keycloak hoặc issuer tương thích JWT.
- S3/Goong nếu dùng import, lưu file, định vị và tối ưu khoảng cách.

Các biến môi trường thường dùng:

```bash
SERVER_PORT=8102
DB_URL=jdbc:postgresql://localhost:5432/second-mile
DB_USERNAME=serp
DB_PASSWORD=serp123
KEYCLOAK_URL=http://localhost:8180
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
INTERNAL_API_KEY=change-me
TMS_ORDER_SERVICE_BASE_URL=http://localhost:8105
GOONG_API_KEY=
STORAGE_S3_ACCESS_KEY=
STORAGE_S3_SECRET_KEY=
```

Không commit `.env` hoặc credential thật.

## Chạy local

Từ thư mục `second-mile/`:

```bash
./mvnw spring-boot:run
```

Trên Windows PowerShell/CMD:

```bash
.\mvnw.cmd spring-boot:run
```

## Build và test

```bash
./mvnw clean compile
./mvnw test
./mvnw -DskipTests clean package
```

Module này không tự đăng ký danh sách migration như `first-mile`; khi thêm SQL
mới cần đảm bảo môi trường local/dev đã apply script tương ứng.

## API

- Service base path: `/api/v1/{resource}`.
- Qua gateway: `http://localhost:8080/second-mile/api/v1/{resource}`.
- Query params dùng `snake_case`.
- Response thường dùng `ApiResponse<T>` và `ApiResponse<PageResponse<T>>`.
- Message trả về lấy từ `MessageService` và `i18n/messages*.properties`.

## Ghi chú phát triển

- Controller mỏng, nghiệp vụ trong service.
- Hub-scoped access nên tái sử dụng `AuthUtils` và `SecondMileAccessUtils`.
- Kafka consumer phải giữ idempotency vì event có thể replay.
- Internal call dùng `X-Internal-Api-Key`, `X-Tenant-Id`,
  `X-Internal-Service`.
- Khi expose API mới, cập nhật type/transform tương ứng ở frontend TMS nếu cần.

trigger build 1