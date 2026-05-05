/*
Author: Nguyen The Anh
Description: Part of Serp Project - Post offices (by code) assigned under a Hub
*/

CREATE TABLE IF NOT EXISTS hub_post_office_mappings (
    id BIGSERIAL PRIMARY KEY,
    hub_id BIGINT NOT NULL REFERENCES hubs (id) ON DELETE CASCADE,
    post_office_code VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    tenant_id BIGINT,
    CONSTRAINT uq_hub_post_office_mappings_tenant_po UNIQUE (tenant_id, post_office_code)
);

CREATE INDEX IF NOT EXISTS idx_hub_post_office_mappings_hub_id ON hub_post_office_mappings (hub_id);

COMMENT ON TABLE hub_post_office_mappings IS 'Maps first-mile post office code to a hub; one hub per post office code per tenant';
