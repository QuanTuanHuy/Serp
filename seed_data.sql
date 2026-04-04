-- =============================================================================
-- TTCRS — Seed Data Script  (tenant_id = 4)
-- Chạy : psql -U postgres -d ttcrs -f seed_data.sql
--        hoặc paste vào DBeaver / DataGrip / pgAdmin
-- =============================================================================

BEGIN;

-- =============================================================================
-- 1. LOCATIONS
-- =============================================================================

INSERT INTO locations (tenant_id, location_code, lat, lng, type, created_stamp)
VALUES
    (4, 'PORT_HCM',       10.7769, 106.7009, 'PORT',            NOW()),
    (4, 'PORT_HP',        20.8449, 106.6881, 'PORT',            NOW()),
    (4, 'WH_HCM_01',      10.8231, 106.6297, 'WAREHOUSE',       NOW()),
    (4, 'WH_HN_01',       21.0245, 105.8412, 'WAREHOUSE',       NOW()),
    (4, 'DEPOT_CTR_HCM',  10.7512, 106.6324, 'DEPOT_CONTAINER', NOW()),
    (4, 'DEPOT_CTR_HN',   21.0058, 105.8200, 'DEPOT_CONTAINER', NOW()),
    (4, 'DEPOT_TRK_HCM',  10.7450, 106.6100, 'DEPOT_TRUCK',     NOW()),
    (4, 'DEPOT_TRK_HN',   21.0100, 105.8150, 'DEPOT_TRUCK',     NOW()),
    (4, 'DEPOT_TRL_HCM',  10.7380, 106.6050, 'DEPOT_TRAILER',   NOW()),
    (4, 'DEPOT_TRL_HN',   21.0150, 105.8100, 'DEPOT_TRAILER',   NOW())
ON CONFLICT (location_code) DO NOTHING;


-- =============================================================================
-- 2. TRUCKS
-- =============================================================================

INSERT INTO trucks (tenant_id, code, status, current_location_code, created_stamp)
VALUES
    (4, 'TRUCK-HCM-001', 'AVAILABLE',   'DEPOT_TRK_HCM', NOW()),
    (4, 'TRUCK-HCM-002', 'IN_USE',      'PORT_HCM',      NOW()),
    (4, 'TRUCK-HCM-003', 'MAINTENANCE', 'DEPOT_TRK_HCM', NOW()),
    (4, 'TRUCK-HN-001',  'AVAILABLE',   'DEPOT_TRK_HN',  NOW()),
    (4, 'TRUCK-HN-002',  'IN_USE',      'PORT_HP',       NOW())
ON CONFLICT (code) DO NOTHING;


-- =============================================================================
-- 3. TRAILERS
-- =============================================================================

INSERT INTO trailers (tenant_id, code, status, current_location_code, created_stamp)
VALUES
    (4, 'TRL-HCM-001', 'AVAILABLE',   'DEPOT_TRL_HCM', NOW()),
    (4, 'TRL-HCM-002', 'IN_USE',      'WH_HCM_01',     NOW()),
    (4, 'TRL-HCM-003', 'AVAILABLE',   'DEPOT_TRL_HCM', NOW()),
    (4, 'TRL-HN-001',  'AVAILABLE',   'DEPOT_TRL_HN',  NOW()),
    (4, 'TRL-HN-002',  'MAINTENANCE', 'DEPOT_TRL_HN',  NOW())
ON CONFLICT (code) DO NOTHING;


-- =============================================================================
-- 4. CONTAINERS  —  size: 'TWENTY' | 'FORTY'
-- =============================================================================

INSERT INTO containers (tenant_id, code, size, status, current_location_code, created_stamp)
VALUES
    (4, 'CTR-20-HCM-001', 'TWENTY', 'AVAILABLE',   'DEPOT_CTR_HCM', NOW()),
    (4, 'CTR-20-HCM-002', 'TWENTY', 'IN_USE',      'PORT_HCM',      NOW()),
    (4, 'CTR-40-HCM-001', 'FORTY',  'AVAILABLE',   'DEPOT_CTR_HCM', NOW()),
    (4, 'CTR-40-HCM-002', 'FORTY',  'MAINTENANCE', 'DEPOT_CTR_HCM', NOW()),
    (4, 'CTR-20-HN-001',  'TWENTY', 'AVAILABLE',   'DEPOT_CTR_HN',  NOW()),
    (4, 'CTR-40-HN-001',  'FORTY',  'AVAILABLE',   'DEPOT_CTR_HN',  NOW()),
    (4, 'CTR-20-HN-002',  'TWENTY', 'IN_USE',      'PORT_HP',       NOW())
ON CONFLICT (code) DO NOTHING;


-- =============================================================================
-- 5. DRIVERS
-- =============================================================================

INSERT INTO drivers (tenant_id, name, status, created_stamp)
VALUES
    (4, 'Nguyen Van An',   'AVAILABLE', NOW()),
    (4, 'Tran Minh Tuan',  'IN_USE',    NOW()),
    (4, 'Le Quoc Hung',    'AVAILABLE', NOW()),
    (4, 'Pham Thanh Son',  'OFF',       NOW()),
    (4, 'Hoang Duc Manh',  'AVAILABLE', NOW()),
    (4, 'Vu Trong Nghia',  'IN_USE',    NOW());


-- =============================================================================
-- 6. DISTANCES   (distance: km, travel_time: phút)
-- =============================================================================

INSERT INTO distances (tenant_id, src_code, dest_code, distance, travel_time, created_stamp)
VALUES
    -- HCM nội vùng
    (4, 'PORT_HCM',      'WH_HCM_01',      15.2,   25.0, NOW()),
    (4, 'PORT_HCM',      'DEPOT_CTR_HCM',  12.5,   20.0, NOW()),
    (4, 'PORT_HCM',      'DEPOT_TRK_HCM',  11.8,   18.0, NOW()),
    (4, 'PORT_HCM',      'DEPOT_TRL_HCM',  13.0,   22.0, NOW()),
    (4, 'WH_HCM_01',     'PORT_HCM',       15.2,   25.0, NOW()),
    (4, 'WH_HCM_01',     'DEPOT_CTR_HCM',   8.3,   14.0, NOW()),
    (4, 'DEPOT_CTR_HCM', 'PORT_HCM',       12.5,   20.0, NOW()),
    (4, 'DEPOT_CTR_HCM', 'WH_HCM_01',       8.3,   14.0, NOW()),
    (4, 'DEPOT_TRK_HCM', 'PORT_HCM',       11.8,   18.0, NOW()),
    (4, 'DEPOT_TRL_HCM', 'PORT_HCM',       13.0,   22.0, NOW()),
    -- HN nội vùng
    (4, 'PORT_HP',       'WH_HN_01',       85.0,  110.0, NOW()),
    (4, 'PORT_HP',       'DEPOT_CTR_HN',   90.0,  115.0, NOW()),
    (4, 'PORT_HP',       'DEPOT_TRK_HN',   92.0,  118.0, NOW()),
    (4, 'WH_HN_01',      'PORT_HP',        85.0,  110.0, NOW()),
    (4, 'WH_HN_01',      'DEPOT_CTR_HN',    5.5,    9.0, NOW()),
    (4, 'DEPOT_CTR_HN',  'PORT_HP',        90.0,  115.0, NOW()),
    (4, 'DEPOT_CTR_HN',  'WH_HN_01',        5.5,    9.0, NOW()),
    (4, 'DEPOT_TRK_HN',  'PORT_HP',        92.0,  118.0, NOW()),
    (4, 'DEPOT_TRL_HN',  'DEPOT_CTR_HN',    3.2,    6.0, NOW()),
    -- Liên vùng HCM ↔ HN
    (4, 'PORT_HCM',      'PORT_HP',      1730.0, 1980.0, NOW()),
    (4, 'WH_HCM_01',     'WH_HN_01',    1720.0, 1960.0, NOW()),
    (4, 'PORT_HP',       'PORT_HCM',    1730.0, 1980.0, NOW())
ON CONFLICT (src_code, dest_code) DO NOTHING;


-- =============================================================================
-- 7. TRANSPORT PLANS
-- =============================================================================

INSERT INTO transport_plans (tenant_id, truck_id, driver_id, start_time, end_time, status, created_stamp)
VALUES
    (4,
     (SELECT id FROM trucks  WHERE code = 'TRUCK-HCM-002'),
     (SELECT id FROM drivers WHERE name = 'Tran Minh Tuan' AND tenant_id = 4),
     '2025-06-10 07:00:00', '2025-06-10 17:00:00', 'EXECUTING', NOW()),

    (4,
     (SELECT id FROM trucks  WHERE code = 'TRUCK-HCM-001'),
     (SELECT id FROM drivers WHERE name = 'Le Quoc Hung'   AND tenant_id = 4),
     '2025-06-08 08:00:00', '2025-06-08 16:00:00', 'COMPLETED', NOW()),

    (4,
     (SELECT id FROM trucks  WHERE code = 'TRUCK-HN-001'),
     (SELECT id FROM drivers WHERE name = 'Hoang Duc Manh' AND tenant_id = 4),
     '2025-06-12 06:30:00', '2025-06-12 18:00:00', 'CREATED',   NOW()),

    (4,
     (SELECT id FROM trucks  WHERE code = 'TRUCK-HN-002'),
     (SELECT id FROM drivers WHERE name = 'Vu Trong Nghia' AND tenant_id = 4),
     '2025-06-09 07:00:00', NULL,                   'CANCELLED', NOW());


-- =============================================================================
-- 8. REQUESTS
-- status : PENDING×4, PLANNED×3, IN_PROGRESS×2, COMPLETED×2, CANCELLED×1
-- type   : OF×4, IF×3, OE×3, IE×2
-- =============================================================================

INSERT INTO requests (
    tenant_id,
    src_location_code, dest_location_code,
    early_at_src, late_at_src,
    early_at_dest, late_at_dest,
    weight, container_size, drop_trailer_required,
    reason, evidence_at_src, evidence_at_dest,
    status, type,
    transport_plan_id, created_by, created_stamp
)
VALUES

-- ── PENDING ───────────────────────────────────────────────────────────────
(4, 'PORT_HCM',      'WH_HCM_01',
    '2025-06-15 06:00:00','2025-06-15 10:00:00',
    '2025-06-15 12:00:00','2025-06-15 16:00:00',
    18500.0, 'TWENTY', FALSE, NULL, NULL, NULL,
    'PENDING', 'OF', NULL, 401, '2025-06-10 09:00:00'),

(4, 'PORT_HCM',      'DEPOT_CTR_HCM',
    '2025-06-16 07:00:00','2025-06-16 11:00:00',
    '2025-06-16 13:00:00','2025-06-16 17:00:00',
    24000.0, 'FORTY', FALSE, NULL, NULL, NULL,
    'PENDING', 'OF', NULL, 401, '2025-06-10 10:30:00'),

(4, 'WH_HCM_01',     'PORT_HCM',
    '2025-06-17 08:00:00','2025-06-17 12:00:00',
    '2025-06-17 14:00:00','2025-06-17 18:00:00',
    12000.0, 'TWENTY', TRUE, NULL, NULL, NULL,
    'PENDING', 'IF', NULL, 402, '2025-06-11 08:00:00'),

(4, 'PORT_HP',       'WH_HN_01',
    '2025-06-18 06:00:00','2025-06-18 10:00:00',
    '2025-06-18 20:00:00','2025-06-19 02:00:00',
    21000.0, 'FORTY', FALSE, NULL, NULL, NULL,
    'PENDING', 'OE', NULL, 403, '2025-06-11 14:00:00'),

-- ── PLANNED ───────────────────────────────────────────────────────────────
(4, 'PORT_HCM',      'WH_HCM_01',
    '2025-06-12 06:00:00','2025-06-12 09:00:00',
    '2025-06-12 11:00:00','2025-06-12 14:00:00',
    16000.0, 'TWENTY', FALSE, NULL, NULL, NULL,
    'PLANNED', 'OF',
    (SELECT id FROM transport_plans WHERE status = 'CREATED'   AND tenant_id = 4),
    401, '2025-06-09 11:00:00'),

(4, 'WH_HN_01',      'PORT_HP',
    '2025-06-12 07:00:00','2025-06-12 11:00:00',
    '2025-06-12 13:00:00','2025-06-12 17:00:00',
    19500.0, 'FORTY', TRUE, NULL, NULL, NULL,
    'PLANNED', 'IE',
    (SELECT id FROM transport_plans WHERE status = 'CREATED'   AND tenant_id = 4),
    403, '2025-06-09 13:30:00'),

(4, 'DEPOT_CTR_HCM', 'PORT_HCM',
    '2025-06-12 08:00:00','2025-06-12 10:00:00',
    '2025-06-12 11:30:00','2025-06-12 14:00:00',
    NULL, 'TWENTY', FALSE, NULL, NULL, NULL,
    'PLANNED', 'IF',
    (SELECT id FROM transport_plans WHERE status = 'CREATED'   AND tenant_id = 4),
    402, '2025-06-09 15:00:00'),

-- ── IN_PROGRESS ───────────────────────────────────────────────────────────
(4, 'PORT_HCM',      'WH_HCM_01',
    '2025-06-10 06:00:00','2025-06-10 09:00:00',
    '2025-06-10 11:00:00','2025-06-10 14:00:00',
    22000.0, 'FORTY', FALSE, NULL,
    'https://storage.ttcrs.io/evidence/req8_src.jpg', NULL,
    'IN_PROGRESS', 'OF',
    (SELECT id FROM transport_plans WHERE status = 'EXECUTING'  AND tenant_id = 4),
    401, '2025-06-07 10:00:00'),

(4, 'WH_HCM_01',     'DEPOT_CTR_HCM',
    '2025-06-10 10:00:00','2025-06-10 13:00:00',
    '2025-06-10 14:30:00','2025-06-10 17:00:00',
    NULL, NULL, TRUE, NULL,
    'https://storage.ttcrs.io/evidence/req9_src.jpg', NULL,
    'IN_PROGRESS', 'OE',
    (SELECT id FROM transport_plans WHERE status = 'EXECUTING'  AND tenant_id = 4),
    402, '2025-06-07 11:30:00'),

-- ── COMPLETED ─────────────────────────────────────────────────────────────
(4, 'PORT_HCM',      'WH_HCM_01',
    '2025-06-08 06:00:00','2025-06-08 09:00:00',
    '2025-06-08 11:00:00','2025-06-08 14:00:00',
    17500.0, 'TWENTY', FALSE, NULL,
    'https://storage.ttcrs.io/evidence/req10_src.jpg',
    'https://storage.ttcrs.io/evidence/req10_dest.jpg',
    'COMPLETED', 'IF',
    (SELECT id FROM transport_plans WHERE status = 'COMPLETED'  AND tenant_id = 4),
    401, '2025-06-05 09:00:00'),

(4, 'DEPOT_CTR_HN',  'PORT_HP',
    '2025-06-08 07:00:00','2025-06-08 10:00:00',
    '2025-06-08 19:00:00','2025-06-08 22:00:00',
    20000.0, 'FORTY', FALSE, NULL,
    'https://storage.ttcrs.io/evidence/req11_src.jpg',
    'https://storage.ttcrs.io/evidence/req11_dest.jpg',
    'COMPLETED', 'OE',
    (SELECT id FROM transport_plans WHERE status = 'COMPLETED'  AND tenant_id = 4),
    403, '2025-06-05 11:00:00'),

-- ── CANCELLED ─────────────────────────────────────────────────────────────
(4, 'PORT_HP',       'WH_HN_01',
    '2025-06-09 06:00:00','2025-06-09 10:00:00',
    '2025-06-09 20:00:00','2025-06-10 00:00:00',
    NULL, 'TWENTY', FALSE,
    'Tai xe bao om, khong du nhan luc dieu phoi', NULL, NULL,
    'CANCELLED', 'IE', NULL, 403, '2025-06-06 14:00:00');


-- =============================================================================
-- 9. TRANSPORT PLAN STOPS
-- =============================================================================

-- Stops cho plan EXECUTING
INSERT INTO transport_plan_stops (
    tenant_id, transport_plan_id, sequence,
    location_code, request_id, trailer_id, action,
    planned_arrival_time, actual_arrival_time, created_stamp
)
SELECT
    4,
    (SELECT id FROM transport_plans WHERE status = 'EXECUTING' AND tenant_id = 4),
    t.seq, t.loc, NULL::BIGINT, NULL::BIGINT, t.act,
    t.planned::TIMESTAMP, t.actual::TIMESTAMP, NOW()
FROM (VALUES
    (1, 'DEPOT_TRK_HCM', 'DEPOT_START',        '2025-06-10 07:00:00', '2025-06-10 07:05:00'),
    (2, 'PORT_HCM',       'PICKUP_CONTAINER',   '2025-06-10 07:25:00', '2025-06-10 07:30:00'),
    (3, 'WH_HCM_01',      'DROP_TRAILER',       '2025-06-10 10:00:00', NULL),
    (4, 'WH_HCM_01',      'DELIVERY_CONTAINER', '2025-06-10 10:30:00', NULL),
    (5, 'DEPOT_TRK_HCM',  'DEPOT_END',          '2025-06-10 12:00:00', NULL)
) AS t(seq, loc, act, planned, actual)
ON CONFLICT (transport_plan_id, sequence) DO NOTHING;

-- Stops cho plan COMPLETED
INSERT INTO transport_plan_stops (
    tenant_id, transport_plan_id, sequence,
    location_code, request_id, trailer_id, action,
    planned_arrival_time, actual_arrival_time, created_stamp
)
SELECT
    4,
    (SELECT id FROM transport_plans WHERE status = 'COMPLETED' AND tenant_id = 4),
    t.seq, t.loc, NULL::BIGINT, NULL::BIGINT, t.act,
    t.planned::TIMESTAMP, t.actual::TIMESTAMP, NOW()
FROM (VALUES
    (1, 'DEPOT_TRK_HCM', 'DEPOT_START',        '2025-06-08 08:00:00', '2025-06-08 08:00:00'),
    (2, 'PORT_HCM',       'PICKUP_CONTAINER',   '2025-06-08 08:20:00', '2025-06-08 08:22:00'),
    (3, 'WH_HCM_01',      'DELIVERY_CONTAINER', '2025-06-08 09:00:00', '2025-06-08 09:05:00'),
    (4, 'DEPOT_CTR_HN',   'PICKUP_CONTAINER',   '2025-06-08 10:00:00', '2025-06-08 10:10:00'),
    (5, 'PORT_HP',         'DELIVERY_CONTAINER', '2025-06-08 12:00:00', '2025-06-08 12:15:00'),
    (6, 'DEPOT_TRK_HCM',  'DEPOT_END',          '2025-06-08 16:00:00', '2025-06-08 15:50:00')
) AS t(seq, loc, act, planned, actual)
ON CONFLICT (transport_plan_id, sequence) DO NOTHING;


-- =============================================================================
-- VERIFICATION
-- =============================================================================

SELECT tbl, rows FROM (
    SELECT 'locations'           AS tbl, COUNT(*) AS rows FROM locations            UNION ALL
    SELECT 'trucks',                      COUNT(*)          FROM trucks              UNION ALL
    SELECT 'trailers',                    COUNT(*)          FROM trailers            UNION ALL
    SELECT 'containers',                  COUNT(*)          FROM containers          UNION ALL
    SELECT 'drivers',                     COUNT(*)          FROM drivers             UNION ALL
    SELECT 'distances',                   COUNT(*)          FROM distances           UNION ALL
    SELECT 'transport_plans',             COUNT(*)          FROM transport_plans     UNION ALL
    SELECT 'transport_plan_stops',        COUNT(*)          FROM transport_plan_stops UNION ALL
    SELECT 'requests',                    COUNT(*)          FROM requests
) t ORDER BY tbl;

COMMIT;

-- =============================================================================
-- GỢI Ý TEST POSTMAN
-- Header: Authorization: Bearer <JWT có claim "tid" = "4">
--
-- GET /api/v1/requests                                                   → 12 records
-- GET /api/v1/requests?statuses=PENDING                                  → 4
-- GET /api/v1/requests?statuses=PENDING&statuses=PLANNED                 → 7
-- GET /api/v1/requests?type=OF                                           → 4
-- GET /api/v1/requests?srcLocationCode=PORT_HCM                         → 4
-- GET /api/v1/requests?createdFrom=2025-06-09T00:00:00&createdTo=2025-06-11T23:59:59 → 6
-- GET /api/v1/requests?statuses=PENDING&type=OF&sortBy=createdAt&sortDirection=asc
-- =============================================================================
