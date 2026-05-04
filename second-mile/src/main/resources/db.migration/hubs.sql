/*
Author: Nguyen The Anh
Description: Part of Serp Project - Table Hubs
*/

-- Kích hoạt extension PostGIS nếu chưa có (để dùng kiểu dữ liệu geography)
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE hubs (
    -- Các trường từ Hub Entity
                      id BIGSERIAL PRIMARY KEY,
                      code VARCHAR(255),
                      name VARCHAR(255),
                      hub_type VARCHAR(50), -- Mapping từ Enum HubType (String)
                      province_code VARCHAR(50),
                      ward_code VARCHAR(50),
                      address_detail TEXT,
                      phone_number VARCHAR(20),
                      location GEOGRAPHY(POINT, 4326), -- Hệ tọa độ WGS84
                      operational_start_date DATE,
                      operational_end_date DATE,
                      working_start_time TIMESTAMP,
                      working_end_time TIMESTAMP,
                      daily_capacity INTEGER DEFAULT 0,
                      current_load INTEGER DEFAULT 0,
                      status VARCHAR(50) DEFAULT 'ACTIVE', -- Mapping từ Enum HubStatus (String)
                      version BIGINT DEFAULT 0,
                      image_url TEXT,

    -- Các trường từ AbstractAudit (MappedSuperclass)
                      tenant_id BIGINT,
                      created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                      created_by VARCHAR(255),
                      updated_by VARCHAR(255)
);

-- Tạo Index cho trường location để tối ưu truy vấn không gian (Spatial Query)
CREATE INDEX idx_hubs_location ON hubs USING GIST (location);

-- Tạo Index cho code nếu bạn thường xuyên tìm kiếm theo mã hub
CREATE INDEX idx_hubs_code ON hubs(code);

-- Comment giải thích
COMMENT ON TABLE hubs IS 'Bảng lưu trữ thông tin các Hub trong dự án Serp Project';