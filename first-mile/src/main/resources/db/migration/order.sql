-- CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,

                        order_code VARCHAR(255),
                        customer_order_code VARCHAR(255),

    -- Thông tin người gửi
                        sender_name VARCHAR(255),
                        sender_phone VARCHAR(255),
                        sender_ward_code VARCHAR(255),
                        sender_province_code VARCHAR(255),
                        sender_address_detail VARCHAR(255),
                        sender_location geography(Point, 4326),

    -- Khung giờ lấy hàng
                        pickup_time_start TIMESTAMP,
                        pickup_time_end TIMESTAMP,

    -- Khung giờ giao hàng
                        delivery_request_time VARCHAR(255),

    -- Thông tin người nhận
                        receiver_name VARCHAR(255),
                        receiver_phone VARCHAR(255),
                        receiver_ward_code VARCHAR(255),
                        receiver_province_code VARCHAR(255),
                        receiver_address_detail VARCHAR(255),
                        receiver_location geography(Point, 4326),

    -- Định tuyến bưu cục
                        origin_post_office_code VARCHAR(255),
                        destination_post_office_code VARCHAR(255),

    -- Thông tin hàng hóa và trạng thái
                        status VARCHAR(255),
                        total_weight DOUBLE PRECISION,
                        total_value DOUBLE PRECISION,
                        dimensions jsonb,
                        total_volume DOUBLE PRECISION,

                        pickup_attempts INT DEFAULT 0,
                        order_product_category VARCHAR(255),
                        order_type VARCHAR(255),

    -- Thông tin cước phí và thanh toán
                        base_shipping_fee BIGINT,
                        cod_fee BIGINT,
                        extra_fee BIGINT,
                        total_shipping_fee BIGINT,
                        cod_amount BIGINT,
                        fee_payer VARCHAR(255),
                        payment_status VARCHAR(255),
                        note TEXT,

    -- CÁC TRƯỜNG KẾ THỪA TỪ AbstractAudit
                        created_at TIMESTAMP,
                        updated_at TIMESTAMP,
                        created_by VARCHAR(255),
                        updated_by VARCHAR(255),
                        tenant_id BIGINT
);

-- Tạo Index cho các trường thường xuyên dùng để query hoặc filter
CREATE INDEX idx_orders_order_code ON orders(order_code);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_tenant_id ON orders(tenant_id); -- Nên có index cho tenant_id nếu bạn dùng Multi-tenancy
CREATE INDEX idx_orders_sender_location ON orders USING GIST(sender_location);
CREATE INDEX idx_orders_receiver_location ON orders USING GIST(receiver_location);