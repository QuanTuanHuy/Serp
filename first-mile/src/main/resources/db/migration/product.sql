CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,

                          name VARCHAR(255),
                          value BIGINT,
                          quantity INT,
                          weight DOUBLE PRECISION,

    -- Khóa ngoại (Foreign Keys)
                          order_id BIGINT NOT NULL,
                          product_type_id BIGINT NOT NULL,

    -- CÁC TRƯỜNG KẾ THỪA TỪ AbstractAudit
                          created_at TIMESTAMP,
                          updated_at TIMESTAMP,
                          created_by VARCHAR(255),
                          updated_by VARCHAR(255),
                          tenant_id BIGINT,

    -- Thiết lập Ràng buộc Khóa ngoại (Foreign Key Constraints)
                          CONSTRAINT fk_products_order
                              FOREIGN KEY (order_id)
                                  REFERENCES orders(id),

                          CONSTRAINT fk_products_product_type
                              FOREIGN KEY (product_type_id)
                                  REFERENCES product_types(id)
);

-- Index cho bảng products
CREATE INDEX idx_products_order_id ON products(order_id);
CREATE INDEX idx_products_product_type_id ON products(product_type_id);
CREATE INDEX idx_products_tenant_id ON products(tenant_id);