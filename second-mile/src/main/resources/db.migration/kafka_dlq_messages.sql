-- Migration script cho bảng kafka_dlq_messages (PostgreSQL)

CREATE TABLE kafka_dlq_messages (
    -- Primary Key
                                    id BIGSERIAL PRIMARY KEY,

    -- Thông tin Kafka Message
                                    topic VARCHAR(255) NOT NULL,
                                    message_key VARCHAR(255),
                                    payload TEXT NOT NULL,
                                    error_message TEXT,

    -- Cấu hình và Trạng thái Retry
                                    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                    retry_count INTEGER NOT NULL DEFAULT 0,
                                    max_retries INTEGER NOT NULL DEFAULT 5,
                                    next_retry_at TIMESTAMP WITHOUT TIME ZONE,

    -- Các trường Audit (Kế thừa từ AbstractAudit)
                                    tenant_id BIGINT,
                                    created_by VARCHAR(255),
                                    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                    updated_by VARCHAR(255),
                                    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tạo Index để tối ưu hóa cho các Worker/Cronjob xử lý Retry
-- Thường Cronjob sẽ query: WHERE status = 'PENDING' AND next_retry_at <= NOW()
CREATE INDEX idx_kafka_dlq_status_next_retry ON kafka_dlq_messages(status, next_retry_at);

-- Tạo Index cho topic để dễ dàng filter/thống kê lỗi theo từng luồng sự kiện
CREATE INDEX idx_kafka_dlq_topic ON kafka_dlq_messages(topic);

-- Tạo Index cho tenant_id (Phục vụ multi-tenant nếu có query theo tenant)
CREATE INDEX idx_kafka_dlq_tenant_id ON kafka_dlq_messages(tenant_id);