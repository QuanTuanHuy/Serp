# TMS Order Service

Service quản lý đơn hàng trung tâm của TMS: tạo đơn, định danh đơn, trạng thái
đơn, lịch sử trạng thái và điều phối trạng thái giữa các chặng vận chuyển.

## Tổng quan

- Framework: Spring Boot 3.5, Java 21.
- Package chính: `serp.project.tms_order`.
- Port mặc định: `8105`.
- Database mặc định: PostgreSQL `tms-order`.
- Gateway path: `/tms-order/api/v1/*`.
- Auth: OAuth2 Resource Server với Keycloak JWT.
- Tích hợp: Kafka, Goong geocode/distance matrix, S3-compatible storage,
  `first-mile`, `second-mile`, `tms-payment-service`.

## Phạm vi nghiệp vụ

Module này là nguồn dữ liệu chuẩn cho đơn hàng TMS:

- Tạo đơn và quản lý thông tin sender/receiver.
- Mã đơn, mã đơn khách hàng, sản phẩm trong đơn.
- Trạng thái đơn, lịch sử trạng thái, transition log.
- Hủy/xác nhận đơn, cập nhật payment status.
- Nhận event từ first-mile, second-mile, last-mile/payment.
- DLQ và retry cho Kafka/event lỗi.

Không đặt logic pickup trip ở đây; phần đó thuộc `first-mile`. Không đặt logic
hub, bag, route linehaul ở đây; phần đó thuộc `second-mile`. Không đặt bảng giá
và tính phí vận chuyển ở đây; phần đó thuộc `tms-billing-service`.

## Cấu trúc thư mục

```text
src/main/java/serp/project/tms_order/
  caller/
  domain/
  dto/
  enums/
  exception/
  kernel/config/
  kernel/interceptor/
  kernel/ratelimit/
  kernel/utils/
  kafka/
  repository/
  service/
  service/impl/
  ui/controller/
src/main/resources/
  db.migration/
  i18n/
  application.yaml
```

## Yêu cầu môi trường

- JDK 21.
- PostgreSQL.
- Kafka nếu chạy luồng đồng bộ.
- Keycloak hoặc issuer tương thích JWT.
- Goong/S3 nếu dùng geocode, distance matrix hoặc lưu file.

Các biến môi trường thường dùng:

```bash
SERVER_PORT=8105
DB_URL=jdbc:postgresql://localhost:5432/tms-order
DB_USERNAME=serp
DB_PASSWORD=serp123
KEYCLOAK_URL=http://localhost:8180
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
INTERNAL_API_KEY=change-me
FIRST_MILE_SERVICE_BASE_URL=http://localhost:8101
PAYMENT_SERVICE_BASE_URL=http://localhost:8103
TMS_ORDER_PAYMENT_WEBHOOK_SECRET=change-me
GOONG_API_KEY=
STORAGE_S3_ACCESS_KEY=
STORAGE_S3_SECRET_KEY=
```

Không commit `.env` hoặc credential thật.

## Chạy local

Từ thư mục `tms-order/`:

```bash
./mvnw spring-boot:run
```

Trên Windows PowerShell/CMD:

```bash
.\mvnw.cmd spring-boot:run
```

Nếu cần chạy schema init từ `application.yaml`, đặt:

```bash
DB_INIT_MODE=always
```

Chỉ dùng cho database local/fresh DB. Mặc định đang là `never`.

## Build và test

```bash
./mvnw clean compile
./mvnw test
./mvnw -Dtest=TmsOrderApplicationTests test
./mvnw -DskipTests clean package
```

Khi sửa lifecycle hoặc status transition, nên bổ sung/chạy test hồi quy gần
logic đó.

## API

- Service base path: `/api/v1/{resource}`.
- Qua gateway: `http://localhost:8080/tms-order/api/v1/{resource}`.
- Query params dùng `snake_case`.
- Request body dùng JSON `camelCase`.
- Response nên dùng `ApiResponse<T>` và `PageResponse<T>` theo controller gần
nhất.

## Ghi chú phát triển

- Trạng thái đơn nên được xử lý tập trung, tránh mỗi consumer tự quyết định
  transition.
- Consumer Kafka phải idempotent vì event có thể bị gửi lại.
- Mọi truy vấn/sửa đơn cần kiểm tra tenant ownership.
- Internal call dùng `X-Internal-Api-Key`, `X-Tenant-Id`,
  `X-Internal-Service`.
- Thêm SQL trong `src/main/resources/db.migration/` và đăng ký vào
  `spring.sql.init.schema-locations` nếu dùng init mode.
