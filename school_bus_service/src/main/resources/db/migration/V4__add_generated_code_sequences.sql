CREATE TABLE IF NOT EXISTS school_bus_code_sequence (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    sequence_key VARCHAR(50) NOT NULL,
    next_value BIGINT NOT NULL,
    CONSTRAINT uk_school_bus_code_sequence_tenant_key UNIQUE (tenant_id, sequence_key)
);

CREATE INDEX IF NOT EXISTS idx_school_bus_code_sequence_tenant_deleted
    ON school_bus_code_sequence (tenant_id, is_deleted);

WITH existing AS (
    SELECT tenant_id,
           COALESCE(MAX(NULLIF(SUBSTRING(code FROM 4), '')::BIGINT), 0) AS max_value
    FROM school_bus_school
    WHERE code ~ '^SBU[0-9]{6}$'
    GROUP BY tenant_id
),
missing AS (
    SELECT id,
           tenant_id,
           ROW_NUMBER() OVER (PARTITION BY tenant_id ORDER BY id) AS rn
    FROM school_bus_school
    WHERE code IS NULL OR BTRIM(code) = ''
)
UPDATE school_bus_school school
SET code = 'SBU' || LPAD((COALESCE(existing.max_value, 0) + missing.rn)::TEXT, 6, '0')
FROM missing
LEFT JOIN existing ON existing.tenant_id = missing.tenant_id
WHERE school.id = missing.id;

WITH existing AS (
    SELECT tenant_id,
           COALESCE(MAX(NULLIF(SUBSTRING(student_code FROM 4), '')::BIGINT), 0) AS max_value
    FROM school_bus_student
    WHERE student_code ~ '^STU[0-9]{6}$'
    GROUP BY tenant_id
),
missing AS (
    SELECT id,
           tenant_id,
           ROW_NUMBER() OVER (PARTITION BY tenant_id ORDER BY id) AS rn
    FROM school_bus_student
    WHERE student_code IS NULL OR BTRIM(student_code) = ''
)
UPDATE school_bus_student student
SET student_code = 'STU' || LPAD((COALESCE(existing.max_value, 0) + missing.rn)::TEXT, 6, '0')
FROM missing
LEFT JOIN existing ON existing.tenant_id = missing.tenant_id
WHERE student.id = missing.id;

WITH existing AS (
    SELECT tenant_id,
           COALESCE(MAX(NULLIF(SUBSTRING(route_code FROM 4), '')::BIGINT), 0) AS max_value
    FROM school_bus_route_plan
    WHERE route_code ~ '^RTE[0-9]{6}$'
    GROUP BY tenant_id
),
missing AS (
    SELECT id,
           tenant_id,
           ROW_NUMBER() OVER (PARTITION BY tenant_id ORDER BY id) AS rn
    FROM school_bus_route_plan
    WHERE route_code IS NULL OR BTRIM(route_code) = ''
)
UPDATE school_bus_route_plan route
SET route_code = 'RTE' || LPAD((COALESCE(existing.max_value, 0) + missing.rn)::TEXT, 6, '0')
FROM missing
LEFT JOIN existing ON existing.tenant_id = missing.tenant_id
WHERE route.id = missing.id;

INSERT INTO school_bus_code_sequence (
    tenant_id,
    sequence_key,
    next_value,
    is_deleted,
    is_active,
    created_at,
    updated_at,
    created_by,
    updated_by
)
SELECT tenant_id,
       'SCHOOL',
       COALESCE(MAX(NULLIF(SUBSTRING(code FROM 4), '')::BIGINT), 0) + 1,
       FALSE,
       TRUE,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP,
       'SYSTEM',
       'SYSTEM'
FROM school_bus_school
WHERE code ~ '^SBU[0-9]{6}$'
GROUP BY tenant_id
ON CONFLICT (tenant_id, sequence_key) DO UPDATE
SET next_value = GREATEST(school_bus_code_sequence.next_value, EXCLUDED.next_value),
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM';

INSERT INTO school_bus_code_sequence (
    tenant_id,
    sequence_key,
    next_value,
    is_deleted,
    is_active,
    created_at,
    updated_at,
    created_by,
    updated_by
)
SELECT tenant_id,
       'STUDENT',
       COALESCE(MAX(NULLIF(SUBSTRING(student_code FROM 4), '')::BIGINT), 0) + 1,
       FALSE,
       TRUE,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP,
       'SYSTEM',
       'SYSTEM'
FROM school_bus_student
WHERE student_code ~ '^STU[0-9]{6}$'
GROUP BY tenant_id
ON CONFLICT (tenant_id, sequence_key) DO UPDATE
SET next_value = GREATEST(school_bus_code_sequence.next_value, EXCLUDED.next_value),
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM';

INSERT INTO school_bus_code_sequence (
    tenant_id,
    sequence_key,
    next_value,
    is_deleted,
    is_active,
    created_at,
    updated_at,
    created_by,
    updated_by
)
SELECT tenant_id,
       'ROUTE',
       COALESCE(MAX(NULLIF(SUBSTRING(route_code FROM 4), '')::BIGINT), 0) + 1,
       FALSE,
       TRUE,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP,
       'SYSTEM',
       'SYSTEM'
FROM school_bus_route_plan
WHERE route_code ~ '^RTE[0-9]{6}$'
GROUP BY tenant_id
ON CONFLICT (tenant_id, sequence_key) DO UPDATE
SET next_value = GREATEST(school_bus_code_sequence.next_value, EXCLUDED.next_value),
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM';

CREATE INDEX IF NOT EXISTS idx_school_bus_school_tenant_code
    ON school_bus_school (tenant_id, code);

CREATE INDEX IF NOT EXISTS idx_school_bus_student_tenant_code
    ON school_bus_student (tenant_id, student_code);

CREATE INDEX IF NOT EXISTS idx_school_bus_route_plan_tenant_code
    ON school_bus_route_plan (tenant_id, route_code);
