-- Author: Nguyen The Anh

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    value BIGINT,
    quantity INT,
    weight DOUBLE PRECISION,
    order_id BIGINT NOT NULL,
    product_type_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    tenant_id BIGINT,
    CONSTRAINT fk_products_order
        FOREIGN KEY (order_id)
            REFERENCES orders(id),
    CONSTRAINT fk_products_product_type
        FOREIGN KEY (product_type_id)
            REFERENCES product_types(id)
);

CREATE INDEX IF NOT EXISTS idx_products_order_id ON products(order_id);
CREATE INDEX IF NOT EXISTS idx_products_product_type_id ON products(product_type_id);
CREATE INDEX IF NOT EXISTS idx_products_tenant_id ON products(tenant_id);
