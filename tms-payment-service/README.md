# TMS Payment Service

Service thanh toán của TMS, hiện tập trung vào ZaloPay: tạo đơn thanh toán,
callback, truy vấn trạng thái, hoàn tiền, lịch sử giao dịch và webhook xác nhận
thanh toán về `tms-order`.

## Tổng quan

- Framework: Spring Boot 3.2, Java 21.
- Package chính: `serp.project.tms_payment_service`.
- Port mặc định: `8103` qua biến `PAYMENT_SERVICE_PORT`.
- Database mặc định: PostgreSQL `payment_service`.
- Gateway path: `/payment/api/v1/*`.
- Auth: OAuth2 Resource Server với Keycloak JWT.
- Tích hợp: ZaloPay sandbox/API, Kafka notification events, Redis,
  webhook sang `tms-order`.

## Phạm vi nghiệp vụ

Module này xử lý:

- Tạo đơn thanh toán ZaloPay.
- Callback thanh toán từ ZaloPay.
- Query trạng thái thanh toán.
- Hoàn tiền và query hoàn tiền.
- Lịch sử giao dịch của user/admin.
- Gửi webhook payment-confirmed sang `tms-order`.
- Phát event email/user notification qua Kafka.

Không đặt logic trạng thái vận chuyển hoặc lifecycle đơn hàng ở đây; phần đó
thuộc `tms-order`.

## Cấu trúc thư mục

```text
src/main/java/serp/project/tms_payment_service/
  controller/
  dto/
  entity/
  enums/
  exception/
  gateway/
  kafka/
  repository/
  scheduler/
  service/
  util/
  config/
src/main/resources/
  db.migration/
  application.yaml
  application-local.yaml
```

## Yêu cầu môi trường

- JDK 21.
- PostgreSQL.
- Redis.
- Kafka nếu chạy notification events.
- Keycloak hoặc issuer tương thích JWT.
- ZaloPay sandbox credentials hoặc cấu hình gateway tương ứng.

Các biến môi trường thường dùng:

```bash
PAYMENT_SERVICE_PORT=8103
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/payment_service
SPRING_DATASOURCE_USERNAME=serp
SPRING_DATASOURCE_PASSWORD=serp123
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
KEYCLOAK_URL=http://localhost:8180
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
TMS_ORDER_PAYMENT_CONFIRMED_WEBHOOK_URL=http://localhost:8105/api/v1/internal/payment-webhooks/orders/payment-confirmed
TMS_ORDER_PAYMENT_WEBHOOK_SECRET=change-me
ZALOPAY_CALLBACK_URL=http://localhost:8080/payment/api/v1/payments/zalopay/callback
ZALOPAY_REDIRECT_URL=http://localhost:3000/payment/result
```

Không commit `.env` hoặc credential thật. Các key ZaloPay trong
`application.yaml` nên được override bằng biến môi trường hoặc profile local
khi triển khai thật.

## Chạy local

Từ thư mục `tms-payment-service/`:

```bash
./run-dev.sh
```

Hoặc chạy trực tiếp:

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

## API

Qua gateway:

- `POST /payment/api/v1/payments/zalopay/create-order`
- `POST /payment/api/v1/payments/zalopay/callback`
- `POST /payment/api/v1/payments/zalopay/query-order`
- `GET /payment/api/v1/payments/zalopay/health`
- `GET /payment/api/v1/payments/zalopay/banks`
- `POST /payment/api/v1/payments/zalopay/refund`
- `POST /payment/api/v1/payments/zalopay/query-refund`
- `GET /payment/api/v1/transactions/my-history`
- `GET /payment/api/v1/transactions/admin/history`

Service nội bộ cũng có controller generic theo gateway code:
`/v1/payments/{gateway}/...`.

## Ghi chú phát triển

- Callback public phải giữ contract với ZaloPay.
- Webhook sang `tms-order` phải dùng secret nhất quán giữa hai service.
- Retry webhook nằm trong `scheduler` và cấu hình `app.webhook.retry`.
- Khi đổi event notification, kiểm tra topic Kafka trong `app.kafka.topics`.
