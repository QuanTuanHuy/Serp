# First-Mile Service

Backend TMS cho chặng đầu: quản lý bưu cục, nhân sự bưu cục, đơn hàng pickup,
chuyến lấy hàng, bàn giao ra hub và các dữ liệu vận hành tại bưu cục.

## Tổng quan

- Framework: Spring Boot 3.5, Java 21.
- Package chính: `serp.project.first_mile`.
- Port mặc định: `8101`.
- Database mặc định: PostgreSQL `first-mile`.
- Gateway path: `/first-mile/api/v1/*`.
- Auth: OAuth2 Resource Server với Keycloak JWT.
- Tích hợp: Kafka, Goong geocode/distance matrix, S3-compatible storage,
  `tms-order`, `second-mile`, `tms-payment-service`.

## Phạm vi nghiệp vụ

Module này xử lý các nghiệp vụ thuộc chặng first-mile:

- Tỉnh, phường/xã, bưu cục, nhân viên bưu cục.
- Xe thuộc bưu cục.
- Đơn hàng pickup, check-in lấy hàng, tối ưu phân công pickup.
- Manifest bàn giao từ bưu cục sang hub.
- Đồng bộ user, hub-post-office, DLQ Kafka.

Không đặt logic hub, bag, route linehaul ở đây. Các phần đó thuộc
`second-mile`. Vòng đời đơn hàng chuẩn và trạng thái tổng thuộc `tms-order`.

## Cấu trúc thư mục

```text
src/main/java/serp/project/first_mile/
  ui/controller/          REST controller
  service/                Interface nghiệp vụ
  service/impl/           Business logic và transaction
  service/handler/        Kafka DLQ handlers
  repository/             Spring Data JPA
  repository/specification/
  domain/                 JPA entities
  dto/request|response/   API contracts
  mapper/                 Entity <-> DTO
  enums/
  exception/
  kernel/config|utils/
  kafka/
  caller/
src/main/resources/
  db/migration/           SQL schema scripts
  i18n/                   API messages
  application.yaml
```

## Yêu cầu môi trường

- JDK 21.
- PostgreSQL.
- Kafka nếu chạy các luồng đồng bộ.
- Keycloak hoặc issuer tương thích JWT.
- Redis/S3/Goong nếu chạy đầy đủ tính năng lưu file, geocode, tối ưu tuyến.

Các biến môi trường thường dùng:

```bash
SERVER_PORT=8101
DB_URL=jdbc:postgresql://localhost:5432/first-mile
DB_USERNAME=serp
DB_PASSWORD=serp123
KEYCLOAK_URL=http://localhost:8180
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
INTERNAL_API_KEY=change-me
TMS_ORDER_SERVICE_BASE_URL=http://localhost:8105
SECOND_MILE_SERVICE_BASE_URL=http://localhost:8102
PAYMENT_SERVICE_BASE_URL=http://localhost:8103
GOONG_API_KEY=
STORAGE_S3_ACCESS_KEY=
STORAGE_S3_SECRET_KEY=
```

Không commit `.env` hoặc credential thật.

## Chạy local

Từ thư mục `first-mile/`:

```bash
./mvnw spring-boot:run
```

Trên Windows PowerShell/CMD:

```bash
.\mvnw.cmd spring-boot:run
```

Nếu cần khởi tạo schema bằng SQL trong `application.yaml`, đặt:

```bash
DB_INIT_MODE=always
```

Chỉ dùng `always` cho database local/fresh DB. Mặc định đang là `never`.

## Build và test

```bash
./mvnw clean compile
./mvnw test
./mvnw -Dtest=PostOfficeServiceImplTest test
./mvnw -DskipTests clean package
```

Khi sửa logic nghiệp vụ, ưu tiên chạy test gần phần vừa sửa trước, rồi chạy
`clean compile`.

## API

- Service base path: `/api/v1/{resource}`.
- Qua gateway: `http://localhost:8080/first-mile/api/v1/{resource}`.
- Query params dùng `snake_case`.
- Request body dùng JSON `camelCase`.
- Response thường bọc bằng `ApiResponse<T>` hoặc `PageResponse<T>` tùy controller
  hiện có.

## Ghi chú phát triển

- Controller chỉ điều phối request/response, không gọi repository trực tiếp.
- Logic ghi dữ liệu đặt trong `service/impl` với `@Transactional`.
- Dữ liệu tenant/user lấy qua `AuthUtils`, không tự parse JWT trong service.
- Internal call dùng `X-Internal-Api-Key`, `X-Tenant-Id`,
  `X-Internal-Service`.
- Thêm bảng/cột bằng SQL trong `src/main/resources/db/migration/` và đăng ký
  trong `spring.sql.init.schema-locations` nếu muốn chạy qua init mode.

trigger build 1