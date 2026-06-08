CREATE TABLE IF NOT EXISTS delivery_manifests (
    id                    BIGSERIAL PRIMARY KEY,
    tenant_id             BIGINT        NOT NULL,
    manifest_code         VARCHAR(50)   NOT NULL,
    post_office_code      VARCHAR(50)   NOT NULL,
    courier_id            BIGINT,
    courier_name          VARCHAR(255),
    vehicle_id            VARCHAR(50),
    status                VARCHAR(50)   NOT NULL DEFAULT 'CREATED',
    planned_date          DATE          NOT NULL,
    planned_departure_at  TIMESTAMP,
    actual_departure_at   TIMESTAMP,
    actual_return_at      TIMESTAMP,
    total_orders          INT           NOT NULL DEFAULT 0,
    delivered_count       INT           NOT NULL DEFAULT 0,
    failed_count          INT           NOT NULL DEFAULT 0,
    total_cod_amount      BIGINT        NOT NULL DEFAULT 0,
    collected_cod_amount  BIGINT        NOT NULL DEFAULT 0,
    total_shipping_fee    BIGINT        NOT NULL DEFAULT 0,
    collected_shipping_fee BIGINT       NOT NULL DEFAULT 0,
    route_geo_json        JSONB,
    note                  TEXT,
    created_at            TIMESTAMP,
    updated_at            TIMESTAMP,
    created_by            VARCHAR(255),
    updated_by            VARCHAR(255),
    UNIQUE (tenant_id, manifest_code)
);

CREATE INDEX IF NOT EXISTS idx_dm_tenant_post_office ON delivery_manifests (tenant_id, post_office_code);
CREATE INDEX IF NOT EXISTS idx_dm_tenant_courier     ON delivery_manifests (tenant_id, courier_id);
CREATE INDEX IF NOT EXISTS idx_dm_tenant_status      ON delivery_manifests (tenant_id, status);
