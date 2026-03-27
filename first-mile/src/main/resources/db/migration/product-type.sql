CREATE TABLE product_types (
                               id BIGSERIAL PRIMARY KEY,

                               code VARCHAR(255),
                               name VARCHAR(255),
                               is_active BOOLEAN,

    -- CÁC TRƯỜNG KẾ THỪA TỪ AbstractAudit
                               created_at TIMESTAMP,
                               updated_at TIMESTAMP,
                               created_by VARCHAR(255),
                               updated_by VARCHAR(255),
                               tenant_id BIGINT
);
-- Index cho bảng product_types
CREATE INDEX idx_product_types_code ON product_types(code);
CREATE INDEX idx_product_types_tenant_id ON product_types(tenant_id);