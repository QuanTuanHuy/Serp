-- Phase 1 foundation data for Serp International School System.
-- This migration intentionally does not create planning, route, trip, or attendance data.

DO $$
DECLARE
    v_tenant_id CONSTANT bigint := 1;
    v_seed_by CONSTANT varchar(100) := 'SEED_DATA';

    v_user_id bigint;
    v_parent_profile_id bigint;
    v_school_id bigint;
    v_pickup_point_id bigint;
    v_dropoff_point_id bigint;
    v_request_id bigint;
    v_request_student_id bigint;
    v_subscription_id bigint;

    v_full_name text;
    v_school_code text;
    v_pickup_code text;
    v_dropoff_code text;
    v_grade text;
    v_class_name text;
    v_trip_option text;
    v_request_status text;
    v_request_source text;
    v_home_address text;
    v_birth_year integer;
    v_birth_date date;
    v_parent_index integer;
    v_local_index integer;
    v_student_index integer;
    v_active_sequence_count integer;
    v_non_seed_sequence_count integer;
    v_sequence_keeper_id bigint;
    v_sequence_max_next bigint;
    v_sequence_key text;
    v_expected_next bigint;
    i integer;

    v_parent_surnames text[] := ARRAY[
        'Nguyễn', 'Trần', 'Lê', 'Phạm', 'Hoàng',
        'Huỳnh', 'Phan', 'Vũ', 'Võ', 'Đặng',
        'Bùi', 'Đỗ', 'Hồ', 'Ngô', 'Dương'
    ];
    v_parent_given_names text[] := ARRAY[
        'Minh Anh', 'Thu Hà', 'Quốc Huy', 'Ngọc Linh', 'Gia Bảo'
    ];
    v_student_surnames text[] := ARRAY[
        'Nguyễn', 'Trần', 'Lê', 'Phạm', 'Hoàng', 'Phan',
        'Vũ', 'Võ', 'Đặng', 'Bùi', 'Đỗ', 'Ngô'
    ];
    v_student_given_names text[] := ARRAY[
        'Minh An', 'Gia Bảo', 'Khánh Linh', 'Đức Minh', 'Phương Anh',
        'Quang Huy', 'Hải Yến', 'Nhật Nam', 'Bảo Ngọc', 'Tuệ Minh'
    ];
    v_driver_names text[] := ARRAY[
        'Nguyễn Văn Thành', 'Trần Quốc Tuấn', 'Lê Minh Đức',
        'Phạm Anh Dũng', 'Hoàng Văn Long', 'Phan Quang Vinh',
        'Vũ Đức Thắng', 'Võ Thành Công', 'Đặng Xuân Hòa',
        'Bùi Mạnh Cường', 'Đỗ Quốc Khánh', 'Ngô Văn Nam',
        'Dương Minh Tân', 'Hồ Anh Quân', 'Nguyễn Trung Kiên',
        'Trần Việt Hùng', 'Lê Hoàng Sơn', 'Phạm Đức Hải'
    ];
    v_attendant_names text[] := ARRAY[
        'Nguyễn Thị Mai', 'Trần Thu Hương', 'Lê Ngọc Lan',
        'Phạm Thanh Tâm', 'Hoàng Bích Ngọc', 'Phan Thùy Dung',
        'Vũ Khánh Chi', 'Võ Minh Trang', 'Đặng Thu Phương',
        'Bùi Hải Anh', 'Đỗ Quỳnh Hoa', 'Ngô Thanh Vân'
    ];
    v_primary_pickup_points integer[] := ARRAY[
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
        12, 13, 14, 15, 16, 17, 18, 19, 20, 21
    ];
    v_primary_dropoff_points integer[] := ARRAY[
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        11, 12, 13, 14, 15, 16, 17, 18, 22
    ];
    v_secondary_pickup_points integer[] := ARRAY[
        5, 6, 7, 8, 9, 10, 11, 12, 13,
        14, 15, 16, 17, 18, 19, 20, 21
    ];
    v_secondary_dropoff_points integer[] := ARRAY[
        5, 6, 7, 8, 9, 10, 11, 12, 13,
        14, 15, 16, 17, 18, 22, 23, 24
    ];
    v_record record;
BEGIN
    -- Refuse to overwrite real users or business codes in the reserved seed namespace.
    IF EXISTS (
        SELECT 1
        FROM public.school_bus_user
        WHERE account_user_id BETWEEN 900100001 AND 900399999
          AND created_by IS DISTINCT FROM v_seed_by
    ) THEN
        RAISE EXCEPTION 'Seed account_user_id namespace collision detected';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_user
        WHERE email ~* '^(parent|driver|attendant)[0-9]{3}@serp-school[.]edu[.]vn$'
          AND created_by IS DISTINCT FROM v_seed_by
    ) THEN
        RAISE EXCEPTION 'Seed school_bus_user email namespace collision detected';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_school
        WHERE tenant_id = v_tenant_id
          AND code IN ('SBU000001', 'SBU000002', 'SBU000003')
          AND is_deleted = false
          AND created_by IS DISTINCT FROM v_seed_by
    ) THEN
        RAISE EXCEPTION 'School code collision detected';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_depot
        WHERE tenant_id = v_tenant_id
          AND code BETWEEN 'DPT000001' AND 'DPT000002'
          AND is_deleted = false
          AND created_by IS DISTINCT FROM v_seed_by
    ) THEN
        RAISE EXCEPTION 'Depot code collision detected';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_pickup_point
        WHERE tenant_id = v_tenant_id
          AND code BETWEEN 'PKP000001' AND 'PKP000024'
          AND is_deleted = false
          AND created_by IS DISTINCT FROM v_seed_by
    ) THEN
        RAISE EXCEPTION 'Pickup point code collision detected';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_student
        WHERE tenant_id = v_tenant_id
          AND student_code BETWEEN 'STU000001' AND 'STU000120'
          AND is_deleted = false
          AND created_by IS DISTINCT FROM v_seed_by
    ) THEN
        RAISE EXCEPTION 'Student code collision detected';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_transport_request
        WHERE tenant_id = v_tenant_id
          AND request_code BETWEEN 'REQ000001' AND 'REQ000090'
          AND is_deleted = false
          AND created_by IS DISTINCT FROM v_seed_by
    ) THEN
        RAISE EXCEPTION 'Transport request code collision detected';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_student_subscription
        WHERE tenant_id = v_tenant_id
          AND subscription_code BETWEEN 'SUB000001' AND 'SUB000120'
          AND is_deleted = false
          AND created_by IS DISTINCT FROM v_seed_by
    ) THEN
        RAISE EXCEPTION 'Subscription code collision detected';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_bus
        WHERE tenant_id = v_tenant_id
          AND plate_number IN (
              '29B-207.15', '29B-214.36', '30F-118.42', '29B-365.90', '30G-452.18',
              '29B-518.27', '30F-672.31', '29B-731.46', '30G-804.22', '29B-846.53',
              '30F-915.68', '29B-932.74', '30G-246.19', '29B-408.65', '30F-557.83'
          )
          AND is_deleted = false
          AND created_by IS DISTINCT FROM v_seed_by
    ) THEN
        RAISE EXCEPTION 'Bus plate number collision detected';
    END IF;

    -- Remove only prior seed-owned dependent data, preserving all real records.
    DELETE FROM public.school_bus_attendance
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_trip_student
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_trip_stop_log
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_trip_execution
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_route_assignment_history
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_route_assignment
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_route_plan_student
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_route_stop
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_route_plan
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_route_planning_session
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_student_subscription_history
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_transport_request_history
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_request_student
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_student_subscription
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_transport_request
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_student
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_school_pickup_point
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_parent_profile
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_driver_profile
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_attendant_profile
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_bus
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_pickup_point
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_depot
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_school
    WHERE created_by = v_seed_by;

    DELETE FROM public.school_bus_user
    WHERE created_by = v_seed_by;

    -- Schools
    INSERT INTO public.school_bus_school (
        tenant_id, name, code, address, contact_phone, contact_email,
        latitude, longitude, is_active, is_deleted,
        created_at, created_by, updated_at, updated_by
    )
    VALUES
        (
            v_tenant_id,
            'Serp Kindergarten',
            'SBU000001',
            'Serp International Campus, Minh Khai, Hai Ba Trung District, Hanoi',
            '02439001001',
            'kindergarten@serp-school.edu.vn',
            20.994700,
            105.867100,
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        ),
        (
            v_tenant_id,
            'Serp Primary School',
            'SBU000002',
            'Serp International Campus, Minh Khai, Hai Ba Trung District, Hanoi',
            '02439001002',
            'primary@serp-school.edu.vn',
            20.995050,
            105.867500,
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        ),
        (
            v_tenant_id,
            'Serp Secondary School',
            'SBU000003',
            'Serp International Campus, Minh Khai, Hai Ba Trung District, Hanoi',
            '02439001003',
            'secondary@serp-school.edu.vn',
            20.995450,
            105.867900,
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        );

    -- Depots
    INSERT INTO public.school_bus_depot (
        tenant_id, code, name, address, latitude, longitude,
        contact_phone, description, is_active, is_deleted,
        created_at, created_by, updated_at, updated_by
    )
    VALUES
        (
            v_tenant_id,
            'DPT000001',
            'Serp Operations Depot - West',
            'My Dinh, Nam Tu Liem District, Hanoi',
            21.028877,
            105.778137,
            '02439002001',
            'Western fleet operations and maintenance base',
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        ),
        (
            v_tenant_id,
            'DPT000002',
            'Serp Operations Depot - East',
            'Long Bien District, Hanoi',
            21.027809,
            105.899211,
            '02439002002',
            'Eastern fleet operations and dispatch base',
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        );

    -- Pickup and drop-off points. Points 19-21 are pickup-only; 22-24 are drop-off-only.
    INSERT INTO public.school_bus_pickup_point (
        tenant_id, code, name, address, latitude, longitude,
        usage_type, pickup_instruction, is_active, is_deleted,
        created_at, created_by, updated_at, updated_by
    )
    VALUES
        (v_tenant_id, 'PKP000001', 'Times City', '458 Minh Khai, Hai Ba Trung District, Hanoi', 20.994102, 105.868334, 'PICKUP_DROPOFF', 'Meet beside the main residential lobby on Minh Khai Street.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000002', 'Royal City', '72A Nguyen Trai, Thanh Xuan District, Hanoi', 21.002504, 105.815117, 'PICKUP_DROPOFF', 'Wait at the designated vehicle bay near the R2 entrance.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000003', 'Trung Hoa Residential Area', 'Hoang Dao Thuy, Cau Giay District, Hanoi', 21.006521, 105.801284, 'PICKUP_DROPOFF', 'Gather at the community gate facing Hoang Dao Thuy Street.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000004', 'Cau Giay Intersection', 'Cau Giay Street, Cau Giay District, Hanoi', 21.033239, 105.800387, 'PICKUP_DROPOFF', 'Use the marked passenger area near the public bus shelter.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000005', 'Nghia Do Park', 'Nguyen Van Huyen Street, Cau Giay District, Hanoi', 21.041875, 105.797231, 'PICKUP_DROPOFF', 'Meet at the south gate beside Nguyen Van Huyen Street.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000006', 'Keangnam Landmark', 'Pham Hung Street, Nam Tu Liem District, Hanoi', 21.017312, 105.783122, 'PICKUP_DROPOFF', 'Wait at the service road passenger bay outside the tower complex.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000007', 'My Dinh Bus Hub', 'Pham Hung Street, Nam Tu Liem District, Hanoi', 21.028952, 105.778842, 'PICKUP_DROPOFF', 'Gather at the northern passenger pick-up lane.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000008', 'Ciputra Urban Area', 'Nguyen Hoang Ton Street, Tay Ho District, Hanoi', 21.074734, 105.789426, 'PICKUP_DROPOFF', 'Meet at the main community reception gate.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000009', 'West Lake Residence', 'Lac Long Quan Street, Tay Ho District, Hanoi', 21.067418, 105.810724, 'PICKUP_DROPOFF', 'Wait at the residential vehicle entrance on Lac Long Quan Street.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000010', 'Lotte Center Hanoi', '54 Lieu Giai, Ba Dinh District, Hanoi', 21.032078, 105.812887, 'PICKUP_DROPOFF', 'Meet at the passenger bay on Dao Tan Street.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000011', 'Giang Vo Residence', 'Giang Vo Street, Ba Dinh District, Hanoi', 21.027109, 105.823782, 'PICKUP_DROPOFF', 'Gather beside the community cultural center entrance.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000012', 'Kim Ma Residence', 'Kim Ma Street, Ba Dinh District, Hanoi', 21.030297, 105.821464, 'PICKUP_DROPOFF', 'Wait at the wide curb opposite the local post office.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000013', 'Van Quan Urban Area', 'Nguyen Khuyen Street, Ha Dong District, Hanoi', 20.980912, 105.787615, 'PICKUP_DROPOFF', 'Meet near the lakeside community gate.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000014', 'Linh Dam Urban Area', 'Nguyen Huu Tho Street, Hoang Mai District, Hanoi', 20.964817, 105.826927, 'PICKUP_DROPOFF', 'Gather at the main taxi and passenger waiting area.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000015', 'Giai Phong Residence', 'Giai Phong Street, Hoang Mai District, Hanoi', 20.982884, 105.841625, 'PICKUP_DROPOFF', 'Wait at the residential gate away from the intersection.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000016', 'Bach Mai Residence', 'Bach Mai Street, Hai Ba Trung District, Hanoi', 21.000463, 105.849769, 'PICKUP_DROPOFF', 'Meet beside the community hall entrance.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000017', 'Minh Khai Residence', 'Minh Khai Street, Hai Ba Trung District, Hanoi', 20.997018, 105.858936, 'PICKUP_DROPOFF', 'Wait at the internal road exit facing Minh Khai Street.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000018', 'Aeon Mall Long Bien', '27 Co Linh, Long Bien District, Hanoi', 21.027732, 105.899985, 'PICKUP_DROPOFF', 'Meet at the morning passenger zone near the western entrance.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000019', 'Long Bien Riverside', 'Ngoc Thuy Street, Long Bien District, Hanoi', 21.047119, 105.859754, 'PICKUP_ONLY', 'Morning collection is at the riverside community gate.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000020', 'Hoan Kiem Lakeside', 'Dinh Tien Hoang Street, Hoan Kiem District, Hanoi', 21.028666, 105.852448, 'PICKUP_ONLY', 'Morning collection is beside the designated tour bus bay.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000021', 'Van Phuc Residence', 'To Huu Street, Ha Dong District, Hanoi', 20.982147, 105.769863, 'PICKUP_ONLY', 'Morning collection is at the main residential security gate.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000022', 'Ha Dong Residential Area', 'Quang Trung Street, Ha Dong District, Hanoi', 20.971542, 105.778491, 'DROPOFF_ONLY', 'Afternoon drop-off is at the sheltered community entrance.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000023', 'Ecopark Residence', 'Ecopark Urban Area, Van Giang District, Hung Yen', 20.956233, 105.930416, 'DROPOFF_ONLY', 'Afternoon drop-off is at the central residential reception.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000024', 'Hoang Mai Residence', 'Tam Trinh Street, Hoang Mai District, Hanoi', 20.983613, 105.867412, 'DROPOFF_ONLY', 'Afternoon drop-off is beside the community playground gate.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by);

    -- School-to-point service coverage: 16 kindergarten, 22 primary, 20 secondary.
    INSERT INTO public.school_bus_school_pickup_point (
        tenant_id, school_id, pickup_point_id, is_default,
        is_active, is_deleted, created_at, created_by, updated_at, updated_by
    )
    SELECT
        v_tenant_id,
        school.id,
        point.id,
        point.code = 'PKP000001',
        true,
        false,
        CURRENT_TIMESTAMP,
        v_seed_by,
        CURRENT_TIMESTAMP,
        v_seed_by
    FROM public.school_bus_school school
    CROSS JOIN public.school_bus_pickup_point point
    WHERE school.tenant_id = v_tenant_id
      AND school.code = 'SBU000001'
      AND point.tenant_id = v_tenant_id
      AND point.code BETWEEN 'PKP000001' AND 'PKP000016';

    INSERT INTO public.school_bus_school_pickup_point (
        tenant_id, school_id, pickup_point_id, is_default,
        is_active, is_deleted, created_at, created_by, updated_at, updated_by
    )
    SELECT
        v_tenant_id,
        school.id,
        point.id,
        point.code = 'PKP000002',
        true,
        false,
        CURRENT_TIMESTAMP,
        v_seed_by,
        CURRENT_TIMESTAMP,
        v_seed_by
    FROM public.school_bus_school school
    CROSS JOIN public.school_bus_pickup_point point
    WHERE school.tenant_id = v_tenant_id
      AND school.code = 'SBU000002'
      AND point.tenant_id = v_tenant_id
      AND point.code BETWEEN 'PKP000001' AND 'PKP000022';

    INSERT INTO public.school_bus_school_pickup_point (
        tenant_id, school_id, pickup_point_id, is_default,
        is_active, is_deleted, created_at, created_by, updated_at, updated_by
    )
    SELECT
        v_tenant_id,
        school.id,
        point.id,
        point.code = 'PKP000018',
        true,
        false,
        CURRENT_TIMESTAMP,
        v_seed_by,
        CURRENT_TIMESTAMP,
        v_seed_by
    FROM public.school_bus_school school
    CROSS JOIN public.school_bus_pickup_point point
    WHERE school.tenant_id = v_tenant_id
      AND school.code = 'SBU000003'
      AND point.tenant_id = v_tenant_id
      AND point.code BETWEEN 'PKP000005' AND 'PKP000024';

    -- Parent shadow users and profiles.
    FOR i IN 1..75 LOOP
        v_full_name :=
            v_parent_surnames[((i - 1) / 5) + 1]
            || ' '
            || v_parent_given_names[((i - 1) % 5) + 1];

        INSERT INTO public.school_bus_user (
            tenant_id, account_user_id, keycloak_id, email,
            first_name, last_name, full_name, phone_number,
            primary_organization_id, preferred_language, timezone,
            user_type, status, last_synced_at, sync_source, raw_payload_json,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        VALUES (
            v_tenant_id,
            900100000 + i,
            NULL,
            'parent' || lpad(i::text, 3, '0') || '@serp-school.edu.vn',
            regexp_replace(v_full_name, '^.* ', ''),
            split_part(v_full_name, ' ', 1),
            v_full_name,
            '0901' || lpad(i::text, 6, '0'),
            v_tenant_id,
            'vi',
            'Asia/Ho_Chi_Minh',
            'PARENT',
            'ACTIVE',
            CURRENT_TIMESTAMP,
            v_seed_by,
            '{"source":"foundation-data"}',
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        )
        RETURNING id INTO v_user_id;

        INSERT INTO public.school_bus_parent_profile (
            tenant_id, user_id, full_name, phone, email, address,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        VALUES (
            v_tenant_id,
            v_user_id,
            v_full_name,
            '0901' || lpad(i::text, 6, '0'),
            'parent' || lpad(i::text, 3, '0') || '@serp-school.edu.vn',
            CASE
                WHEN i <= 20 THEN 'Hai Ba Trung District, Hanoi'
                WHEN i <= 50 THEN 'Cau Giay District, Hanoi'
                ELSE 'Long Bien District, Hanoi'
            END,
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        );
    END LOOP;

    -- Driver shadow users and profiles.
    FOR i IN 1..18 LOOP
        v_full_name := v_driver_names[i];

        INSERT INTO public.school_bus_user (
            tenant_id, account_user_id, keycloak_id, email,
            first_name, last_name, full_name, phone_number,
            primary_organization_id, preferred_language, timezone,
            user_type, status, last_synced_at, sync_source, raw_payload_json,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        VALUES (
            v_tenant_id,
            900200000 + i,
            NULL,
            'driver' || lpad(i::text, 3, '0') || '@serp-school.edu.vn',
            regexp_replace(v_full_name, '^.* ', ''),
            split_part(v_full_name, ' ', 1),
            v_full_name,
            '0912' || lpad(i::text, 6, '0'),
            v_tenant_id,
            'vi',
            'Asia/Ho_Chi_Minh',
            'DRIVER',
            'ACTIVE',
            CURRENT_TIMESTAMP,
            v_seed_by,
            '{"source":"foundation-data"}',
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        )
        RETURNING id INTO v_user_id;

        INSERT INTO public.school_bus_driver_profile (
            tenant_id, user_id, full_name, phone, status,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        VALUES (
            v_tenant_id,
            v_user_id,
            v_full_name,
            '0912' || lpad(i::text, 6, '0'),
            CASE
                WHEN i <= 15 THEN 'AVAILABLE'
                WHEN i = 16 THEN 'ASSIGNED'
                WHEN i = 17 THEN 'ON_LEAVE'
                ELSE 'INACTIVE'
            END,
            i <> 18,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        );
    END LOOP;

    -- Attendant shadow users and profiles.
    FOR i IN 1..12 LOOP
        v_full_name := v_attendant_names[i];

        INSERT INTO public.school_bus_user (
            tenant_id, account_user_id, keycloak_id, email,
            first_name, last_name, full_name, phone_number,
            primary_organization_id, preferred_language, timezone,
            user_type, status, last_synced_at, sync_source, raw_payload_json,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        VALUES (
            v_tenant_id,
            900300000 + i,
            NULL,
            'attendant' || lpad(i::text, 3, '0') || '@serp-school.edu.vn',
            regexp_replace(v_full_name, '^.* ', ''),
            split_part(v_full_name, ' ', 1),
            v_full_name,
            '0936' || lpad(i::text, 6, '0'),
            v_tenant_id,
            'vi',
            'Asia/Ho_Chi_Minh',
            'ATTENDANT',
            'ACTIVE',
            CURRENT_TIMESTAMP,
            v_seed_by,
            '{"source":"foundation-data"}',
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        )
        RETURNING id INTO v_user_id;

        INSERT INTO public.school_bus_attendant_profile (
            tenant_id, user_id, full_name, phone, status,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        VALUES (
            v_tenant_id,
            v_user_id,
            v_full_name,
            '0936' || lpad(i::text, 6, '0'),
            CASE
                WHEN i <= 9 THEN 'AVAILABLE'
                WHEN i = 10 THEN 'ASSIGNED'
                WHEN i = 11 THEN 'ON_LEAVE'
                ELSE 'INACTIVE'
            END,
            i <> 12,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        );
    END LOOP;

    -- Fleet: 12 available, 2 maintenance, 1 inactive.
    FOR v_record IN
        SELECT *
        FROM (
            VALUES
                ('29B-207.15', 'BUS_29_SEATS', 29, 'AVAILABLE',   'DPT000001'),
                ('29B-214.36', 'BUS_29_SEATS', 29, 'AVAILABLE',   'DPT000002'),
                ('30F-118.42', 'BUS_16_SEATS', 16, 'AVAILABLE',   'DPT000001'),
                ('29B-365.90', 'BUS_45_SEATS', 45, 'AVAILABLE',   'DPT000002'),
                ('30G-452.18', 'BUS_29_SEATS', 29, 'AVAILABLE',   'DPT000001'),
                ('29B-518.27', 'BUS_29_SEATS', 29, 'AVAILABLE',   'DPT000002'),
                ('30F-672.31', 'BUS_16_SEATS', 16, 'AVAILABLE',   'DPT000001'),
                ('29B-731.46', 'BUS_29_SEATS', 29, 'AVAILABLE',   'DPT000002'),
                ('30G-804.22', 'BUS_45_SEATS', 45, 'AVAILABLE',   'DPT000001'),
                ('29B-846.53', 'BUS_29_SEATS', 29, 'AVAILABLE',   'DPT000002'),
                ('30F-915.68', 'BUS_16_SEATS', 16, 'AVAILABLE',   'DPT000001'),
                ('29B-932.74', 'BUS_29_SEATS', 29, 'AVAILABLE',   'DPT000002'),
                ('30G-246.19', 'BUS_45_SEATS', 45, 'MAINTENANCE', 'DPT000001'),
                ('29B-408.65', 'BUS_29_SEATS', 29, 'MAINTENANCE', 'DPT000002'),
                ('30F-557.83', 'BUS_16_SEATS', 16, 'INACTIVE',    'DPT000001')
        ) AS fleet(plate_number, bus_type, capacity, status, depot_code)
    LOOP
        INSERT INTO public.school_bus_bus (
            tenant_id, plate_number, bus_type, capacity, status, home_depot_id,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        SELECT
            v_tenant_id,
            v_record.plate_number,
            v_record.bus_type,
            v_record.capacity,
            v_record.status,
            depot.id,
            v_record.status <> 'INACTIVE',
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        FROM public.school_bus_depot depot
        WHERE depot.tenant_id = v_tenant_id
          AND depot.code = v_record.depot_code
          AND depot.is_deleted = false;
    END LOOP;

    -- Students: 30 kindergarten, 50 primary, 40 secondary.
    FOR i IN 1..120 LOOP
        v_full_name :=
            v_student_surnames[((i - 1) / 10) + 1]
            || ' '
            || v_student_given_names[((i - 1) % 10) + 1];

        IF i <= 30 THEN
            v_school_code := 'SBU000001';
            v_local_index := i;
            v_parent_index := CASE WHEN i <= 20 THEN i ELSE i - 20 END;
            v_grade := 'K' || (3 + ((v_local_index - 1) % 3))::text;
            v_class_name := v_grade || '-A';
            v_birth_year := 2025 - (3 + ((v_local_index - 1) % 3));
            v_pickup_code := 'PKP' || lpad((1 + ((v_local_index - 1) % 16))::text, 6, '0');
            v_dropoff_code := 'PKP' || lpad((1 + ((v_local_index + 4) % 16))::text, 6, '0');
        ELSIF i <= 80 THEN
            v_school_code := 'SBU000002';
            v_local_index := i - 30;
            v_parent_index := CASE
                WHEN v_local_index <= 30 THEN 20 + v_local_index
                ELSE 20 + (v_local_index - 30)
            END;
            v_grade := (1 + ((v_local_index - 1) % 5))::text;
            v_class_name := v_grade || 'A1';
            v_birth_year := 2020 - v_grade::integer;
            v_pickup_code := 'PKP' || lpad(
                v_primary_pickup_points[
                    1 + ((v_local_index - 1) % array_length(v_primary_pickup_points, 1))
                ]::text,
                6,
                '0'
            );
            v_dropoff_code := 'PKP' || lpad(
                v_primary_dropoff_points[
                    1 + ((v_local_index + 3) % array_length(v_primary_dropoff_points, 1))
                ]::text,
                6,
                '0'
            );
        ELSE
            v_school_code := 'SBU000003';
            v_local_index := i - 80;
            v_parent_index := CASE
                WHEN v_local_index <= 25 THEN 50 + v_local_index
                ELSE 50 + (v_local_index - 25)
            END;
            v_grade := (6 + ((v_local_index - 1) % 4))::text;
            v_class_name := v_grade || 'A1';
            v_birth_year := 2020 - v_grade::integer;
            v_pickup_code := 'PKP' || lpad(
                v_secondary_pickup_points[
                    1 + ((v_local_index - 1) % array_length(v_secondary_pickup_points, 1))
                ]::text,
                6,
                '0'
            );
            v_dropoff_code := 'PKP' || lpad(
                v_secondary_dropoff_points[
                    1 + ((v_local_index + 3) % array_length(v_secondary_dropoff_points, 1))
                ]::text,
                6,
                '0'
            );
        END IF;

        v_birth_date := make_date(
            v_birth_year,
            1 + ((i - 1) % 12),
            1 + ((i - 1) % 28)
        );

        SELECT id
        INTO STRICT v_school_id
        FROM public.school_bus_school
        WHERE tenant_id = v_tenant_id
          AND code = v_school_code
          AND is_deleted = false;

        SELECT profile.id
        INTO STRICT v_parent_profile_id
        FROM public.school_bus_parent_profile profile
        JOIN public.school_bus_user school_user
          ON school_user.id = profile.user_id
        WHERE profile.tenant_id = v_tenant_id
          AND school_user.account_user_id = 900100000 + v_parent_index
          AND profile.is_deleted = false;

        SELECT id, address
        INTO STRICT v_pickup_point_id, v_home_address
        FROM public.school_bus_pickup_point
        WHERE tenant_id = v_tenant_id
          AND code = v_pickup_code
          AND is_deleted = false;

        SELECT id
        INTO STRICT v_dropoff_point_id
        FROM public.school_bus_pickup_point
        WHERE tenant_id = v_tenant_id
          AND code = v_dropoff_code
          AND is_deleted = false;

        INSERT INTO public.school_bus_student (
            tenant_id, school_id, parent_profile_id,
            pickup_point_id, default_dropoff_point_id,
            full_name, student_code, grade, class_name,
            home_address, date_of_birth, gender, special_note,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        VALUES (
            v_tenant_id,
            v_school_id,
            v_parent_profile_id,
            v_pickup_point_id,
            v_dropoff_point_id,
            v_full_name,
            'STU' || lpad(i::text, 6, '0'),
            v_grade,
            v_class_name,
            v_home_address,
            v_birth_date,
            CASE WHEN i % 2 = 0 THEN 'FEMALE' ELSE 'MALE' END,
            CASE
                WHEN i % 17 = 0 THEN 'Family requests a front-row seat when available.'
                WHEN i % 23 = 0 THEN 'Please remind the student before the scheduled stop.'
                ELSE NULL
            END,
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        );
    END LOOP;

    -- Approved requests 1-75: one request per parent, containing all of that parent's students.
    FOR i IN 1..75 LOOP
        SELECT profile.id, student.school_id
        INTO STRICT v_parent_profile_id, v_school_id
        FROM public.school_bus_parent_profile profile
        JOIN public.school_bus_user school_user
          ON school_user.id = profile.user_id
        JOIN public.school_bus_student student
          ON student.parent_profile_id = profile.id
         AND student.is_deleted = false
        WHERE profile.tenant_id = v_tenant_id
          AND school_user.account_user_id = 900100000 + i
          AND profile.is_deleted = false
        ORDER BY student.id
        LIMIT 1;

        v_request_source := CASE WHEN i % 5 = 0 THEN 'ADMIN' ELSE 'PARENT' END;

        INSERT INTO public.school_bus_transport_request (
            tenant_id, parent_profile_id, school_id,
            request_type, status, request_code, requested_at, request_source,
            effective_from, effective_to, notes,
            approved_by, approved_at, rejection_reason, change_reason,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        VALUES (
            v_tenant_id,
            v_parent_profile_id,
            v_school_id,
            'NEW_SERVICE',
            'APPROVED',
            'REQ' || lpad(i::text, 6, '0'),
            CURRENT_TIMESTAMP - INTERVAL '45 days' + make_interval(hours => i % 24),
            v_request_source,
            CURRENT_DATE - 30,
            NULL,
            'Regular weekday school transport registration.',
            NULL,
            CURRENT_TIMESTAMP - INTERVAL '44 days' + make_interval(hours => i % 24),
            NULL,
            NULL,
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        )
        RETURNING id INTO v_request_id;

        FOR v_record IN
            SELECT
                student.id,
                student.student_code,
                student.pickup_point_id,
                student.default_dropoff_point_id
            FROM public.school_bus_student student
            WHERE student.tenant_id = v_tenant_id
              AND student.parent_profile_id = v_parent_profile_id
              AND student.is_deleted = false
            ORDER BY student.student_code
        LOOP
            v_student_index := substring(v_record.student_code FROM 4)::integer;
            v_trip_option := CASE
                WHEN v_student_index <= 72 THEN 'ROUND_TRIP'
                WHEN v_student_index <= 102 THEN 'MORNING'
                ELSE 'AFTERNOON'
            END;

            INSERT INTO public.school_bus_request_student (
                tenant_id, request_id, student_id,
                pickup_point_id, dropoff_point_id, trip_option,
                is_monday, is_tuesday, is_wednesday, is_thursday, is_friday,
                is_saturday, is_sunday,
                subscription_id, target_subscription_id, student_note,
                is_active, is_deleted, created_at, created_by, updated_at, updated_by
            )
            VALUES (
                v_tenant_id,
                v_request_id,
                v_record.id,
                v_record.pickup_point_id,
                v_record.default_dropoff_point_id,
                v_trip_option,
                true, true, true, true, true,
                false, false,
                NULL,
                NULL,
                NULL,
                true,
                false,
                CURRENT_TIMESTAMP,
                v_seed_by,
                CURRENT_TIMESTAMP,
                v_seed_by
            );
        END LOOP;
    END LOOP;

    -- Requests 76-85 are submitted; 86-90 are rejected.
    FOR i IN 76..90 LOOP
        v_student_index := 1 + (((i - 76) * 7) % 120);

        SELECT
            student.id,
            student.school_id,
            student.parent_profile_id,
            student.pickup_point_id,
            student.default_dropoff_point_id
        INTO STRICT v_record
        FROM public.school_bus_student student
        WHERE student.tenant_id = v_tenant_id
          AND student.student_code = 'STU' || lpad(v_student_index::text, 6, '0')
          AND student.is_deleted = false;

        v_request_status := CASE WHEN i <= 85 THEN 'SUBMITTED' ELSE 'REJECTED' END;
        v_request_source := CASE WHEN i % 3 = 0 THEN 'ADMIN' ELSE 'PARENT' END;
        v_trip_option := CASE
            WHEN v_student_index <= 72 THEN 'ROUND_TRIP'
            WHEN v_student_index <= 102 THEN 'MORNING'
            ELSE 'AFTERNOON'
        END;

        INSERT INTO public.school_bus_transport_request (
            tenant_id, parent_profile_id, school_id,
            request_type, status, request_code, requested_at, request_source,
            effective_from, effective_to, notes,
            approved_by, approved_at, rejection_reason, change_reason,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        VALUES (
            v_tenant_id,
            v_record.parent_profile_id,
            v_record.school_id,
            'NEW_SERVICE',
            v_request_status,
            'REQ' || lpad(i::text, 6, '0'),
            CURRENT_TIMESTAMP - make_interval(days => 20 - (i - 76)),
            v_request_source,
            CURRENT_DATE + 7,
            NULL,
            CASE
                WHEN v_request_status = 'SUBMITTED'
                    THEN 'Registration is awaiting transport office review.'
                ELSE 'Registration was reviewed but could not be accepted for the requested start date.'
            END,
            NULL,
            NULL,
            CASE
                WHEN v_request_status = 'REJECTED'
                    THEN 'Requested service capacity was unavailable for the selected start date.'
                ELSE NULL
            END,
            NULL,
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        )
        RETURNING id INTO v_request_id;

        INSERT INTO public.school_bus_request_student (
            tenant_id, request_id, student_id,
            pickup_point_id, dropoff_point_id, trip_option,
            is_monday, is_tuesday, is_wednesday, is_thursday, is_friday,
            is_saturday, is_sunday,
            subscription_id, target_subscription_id, student_note,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        VALUES (
            v_tenant_id,
            v_request_id,
            v_record.id,
            v_record.pickup_point_id,
            v_record.default_dropoff_point_id,
            v_trip_option,
            true, true, true, true, true,
            false, false,
            NULL,
            NULL,
            CASE
                WHEN v_request_status = 'SUBMITTED' THEN 'Pending transport office review.'
                ELSE 'Retained for registration history.'
            END,
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        );
    END LOOP;

    -- One lifecycle history row for every request.
    INSERT INTO public.school_bus_transport_request_history (
        tenant_id, request_id, old_status, new_status,
        changed_by, changed_at, reason, notes,
        is_active, is_deleted, created_at, created_by, updated_at, updated_by
    )
    SELECT
        v_tenant_id,
        request.id,
        'DRAFT',
        request.status,
        request.approved_by,
        COALESCE(request.approved_at, request.requested_at),
        CASE request.status
            WHEN 'APPROVED' THEN 'Transport registration approved.'
            WHEN 'SUBMITTED' THEN 'Transport registration submitted for review.'
            WHEN 'REJECTED' THEN request.rejection_reason
            ELSE 'Transport registration status recorded.'
        END,
        'Foundation transport registration history.',
        true,
        false,
        CURRENT_TIMESTAMP,
        v_seed_by,
        CURRENT_TIMESTAMP,
        v_seed_by
    FROM public.school_bus_transport_request request
    WHERE request.tenant_id = v_tenant_id
      AND request.created_by = v_seed_by
      AND request.is_deleted = false;

    -- Active subscriptions and corresponding CREATED history.
    FOR i IN 1..120 LOOP
        SELECT
            student.id,
            student.school_id,
            student.parent_profile_id,
            student.pickup_point_id,
            student.default_dropoff_point_id
        INTO STRICT v_record
        FROM public.school_bus_student student
        WHERE student.tenant_id = v_tenant_id
          AND student.student_code = 'STU' || lpad(i::text, 6, '0')
          AND student.is_deleted = false;

        SELECT request.id
        INTO STRICT v_request_id
        FROM public.school_bus_transport_request request
        WHERE request.tenant_id = v_tenant_id
          AND request.parent_profile_id = v_record.parent_profile_id
          AND request.school_id = v_record.school_id
          AND request.status = 'APPROVED'
          AND request.created_by = v_seed_by
          AND request.is_deleted = false;

        v_trip_option := CASE
            WHEN i <= 72 THEN 'ROUND_TRIP'
            WHEN i <= 102 THEN 'MORNING'
            ELSE 'AFTERNOON'
        END;

        INSERT INTO public.school_bus_student_subscription (
            tenant_id, student_id, school_id,
            pickup_point_id, dropoff_point_id,
            subscription_code, trip_option,
            is_monday, is_tuesday, is_wednesday, is_thursday, is_friday,
            is_saturday, is_sunday,
            effective_from, effective_to, status, source_request_id,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        VALUES (
            v_tenant_id,
            v_record.id,
            v_record.school_id,
            v_record.pickup_point_id,
            v_record.default_dropoff_point_id,
            'SUB' || lpad(i::text, 6, '0'),
            v_trip_option,
            true, true, true, true, true,
            false, false,
            CURRENT_DATE - 30,
            NULL,
            'ACTIVE',
            v_request_id,
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        )
        RETURNING id INTO v_subscription_id;

        UPDATE public.school_bus_request_student
        SET subscription_id = v_subscription_id,
            updated_at = CURRENT_TIMESTAMP,
            updated_by = v_seed_by
        WHERE tenant_id = v_tenant_id
          AND request_id = v_request_id
          AND student_id = v_record.id
          AND created_by = v_seed_by
          AND is_deleted = false;

        SELECT id
        INTO STRICT v_request_student_id
        FROM public.school_bus_request_student
        WHERE tenant_id = v_tenant_id
          AND request_id = v_request_id
          AND student_id = v_record.id
          AND created_by = v_seed_by
          AND is_deleted = false;

        INSERT INTO public.school_bus_student_subscription_history (
            tenant_id, subscription_id, source_request_id, request_student_id,
            change_type, old_status, new_status,
            old_pickup_point_id, new_pickup_point_id,
            old_dropoff_point_id, new_dropoff_point_id,
            old_trip_option, new_trip_option,
            old_effective_from, new_effective_from,
            old_effective_to, new_effective_to,
            changed_by, changed_at, reason, notes,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        VALUES (
            v_tenant_id,
            v_subscription_id,
            v_request_id,
            v_request_student_id,
            'CREATED',
            NULL,
            'ACTIVE',
            NULL,
            v_record.pickup_point_id,
            NULL,
            v_record.default_dropoff_point_id,
            NULL,
            v_trip_option,
            NULL,
            CURRENT_DATE - 30,
            NULL,
            NULL,
            NULL,
            CURRENT_TIMESTAMP,
            'Created from an approved new service registration.',
            'Weekday transport service activated.',
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        );
    END LOOP;

    -- Keep exactly one active sequence row per key and never decrease a real sequence.
    FOR v_sequence_key, v_expected_next IN
        SELECT *
        FROM (
            VALUES
                ('SCHOOL'::text, 4::bigint),
                ('DEPOT'::text, 3::bigint),
                ('PICKUP_POINT'::text, 25::bigint),
                ('STUDENT'::text, 121::bigint),
                ('REQUEST'::text, 91::bigint),
                ('SUBSCRIPTION'::text, 121::bigint),
                ('ROUTE'::text, 1::bigint),
                ('TRIP'::text, 1::bigint)
        ) AS expected(sequence_key, next_value)
    LOOP
        SELECT count(*)
        INTO v_active_sequence_count
        FROM public.school_bus_code_sequence
        WHERE tenant_id = v_tenant_id
          AND sequence_key = v_sequence_key
          AND is_deleted = false;

        IF v_active_sequence_count > 1 THEN
            SELECT
                count(*) FILTER (WHERE created_by IS DISTINCT FROM v_seed_by),
                max(next_value)
            INTO v_non_seed_sequence_count, v_sequence_max_next
            FROM public.school_bus_code_sequence
            WHERE tenant_id = v_tenant_id
              AND sequence_key = v_sequence_key
              AND is_deleted = false;

            IF v_non_seed_sequence_count > 1 THEN
                RAISE EXCEPTION
                    'Multiple non-seed active code sequence rows detected for tenant %, key %',
                    v_tenant_id,
                    v_sequence_key;
            ELSIF v_non_seed_sequence_count = 1 THEN
                SELECT id
                INTO STRICT v_sequence_keeper_id
                FROM public.school_bus_code_sequence
                WHERE tenant_id = v_tenant_id
                  AND sequence_key = v_sequence_key
                  AND is_deleted = false
                  AND created_by IS DISTINCT FROM v_seed_by;
            ELSE
                SELECT min(id)
                INTO STRICT v_sequence_keeper_id
                FROM public.school_bus_code_sequence
                WHERE tenant_id = v_tenant_id
                  AND sequence_key = v_sequence_key
                  AND is_deleted = false;
            END IF;

            UPDATE public.school_bus_code_sequence
            SET next_value = GREATEST(v_sequence_max_next, v_expected_next),
                is_active = true,
                updated_at = CURRENT_TIMESTAMP,
                updated_by = v_seed_by
            WHERE id = v_sequence_keeper_id;

            DELETE FROM public.school_bus_code_sequence
            WHERE tenant_id = v_tenant_id
              AND sequence_key = v_sequence_key
              AND is_deleted = false
              AND id <> v_sequence_keeper_id
              AND created_by = v_seed_by;
        ELSIF v_active_sequence_count = 1 THEN
            UPDATE public.school_bus_code_sequence
            SET next_value = GREATEST(next_value, v_expected_next),
                is_active = true,
                updated_at = CURRENT_TIMESTAMP,
                updated_by = v_seed_by
            WHERE tenant_id = v_tenant_id
              AND sequence_key = v_sequence_key
              AND is_deleted = false;
        ELSE
            INSERT INTO public.school_bus_code_sequence (
                tenant_id, sequence_key, next_value,
                is_active, is_deleted, created_at, created_by, updated_at, updated_by
            )
            VALUES (
                v_tenant_id,
                v_sequence_key,
                v_expected_next,
                true,
                false,
                CURRENT_TIMESTAMP,
                v_seed_by,
                CURRENT_TIMESTAMP,
                v_seed_by
            );
        END IF;
    END LOOP;
END $$;

-- Verification queries to run after Flyway completes:
--
-- SELECT 'schools' AS name, count(*) FROM public.school_bus_school WHERE created_by = 'SEED_DATA'
-- UNION ALL SELECT 'depots', count(*) FROM public.school_bus_depot WHERE created_by = 'SEED_DATA'
-- UNION ALL SELECT 'users', count(*) FROM public.school_bus_user WHERE created_by = 'SEED_DATA'
-- UNION ALL SELECT 'buses', count(*) FROM public.school_bus_bus WHERE created_by = 'SEED_DATA'
-- UNION ALL SELECT 'parents', count(*) FROM public.school_bus_parent_profile WHERE created_by = 'SEED_DATA'
-- UNION ALL SELECT 'drivers', count(*) FROM public.school_bus_driver_profile WHERE created_by = 'SEED_DATA'
-- UNION ALL SELECT 'attendants', count(*) FROM public.school_bus_attendant_profile WHERE created_by = 'SEED_DATA'
-- UNION ALL SELECT 'pickup_points', count(*) FROM public.school_bus_pickup_point WHERE created_by = 'SEED_DATA'
-- UNION ALL SELECT 'school_pickup_links', count(*) FROM public.school_bus_school_pickup_point WHERE created_by = 'SEED_DATA'
-- UNION ALL SELECT 'students', count(*) FROM public.school_bus_student WHERE created_by = 'SEED_DATA'
-- UNION ALL SELECT 'requests', count(*) FROM public.school_bus_transport_request WHERE created_by = 'SEED_DATA'
-- UNION ALL SELECT 'request_students', count(*) FROM public.school_bus_request_student WHERE created_by = 'SEED_DATA'
-- UNION ALL SELECT 'request_history', count(*) FROM public.school_bus_transport_request_history WHERE created_by = 'SEED_DATA'
-- UNION ALL SELECT 'subscriptions', count(*) FROM public.school_bus_student_subscription WHERE created_by = 'SEED_DATA'
-- UNION ALL SELECT 'subscription_history', count(*) FROM public.school_bus_student_subscription_history WHERE created_by = 'SEED_DATA';
--
-- Expected: schools=3, depots=2, users=105, buses=15, parents=75, drivers=18,
-- attendants=12, pickup_points=24, school_pickup_links=58, students=120,
-- requests=90, request_students=135, request_history=90,
-- subscriptions=120, subscription_history=120.
--
-- SELECT
--     school.name AS school,
--     subscription.trip_option,
--     count(*) AS total
-- FROM public.school_bus_student_subscription subscription
-- JOIN public.school_bus_school school ON school.id = subscription.school_id
-- WHERE subscription.created_by = 'SEED_DATA'
--   AND subscription.status = 'ACTIVE'
--   AND subscription.is_deleted = false
-- GROUP BY school.name, subscription.trip_option
-- ORDER BY school.name, subscription.trip_option;
--
-- SELECT count(*) AS invalid_subscriptions
-- FROM public.school_bus_student_subscription
-- WHERE created_by = 'SEED_DATA'
--   AND status = 'ACTIVE'
--   AND (pickup_point_id IS NULL OR dropoff_point_id IS NULL);
-- Expected: 0.
--
-- SELECT count(*) AS subscriptions_without_school_point_link
-- FROM public.school_bus_student_subscription subscription
-- WHERE subscription.created_by = 'SEED_DATA'
--   AND subscription.status = 'ACTIVE'
--   AND subscription.is_deleted = false
--   AND (
--       NOT EXISTS (
--           SELECT 1
--           FROM public.school_bus_school_pickup_point link
--           WHERE link.school_id = subscription.school_id
--             AND link.pickup_point_id = subscription.pickup_point_id
--             AND link.is_deleted = false
--       )
--       OR NOT EXISTS (
--           SELECT 1
--           FROM public.school_bus_school_pickup_point link
--           WHERE link.school_id = subscription.school_id
--             AND link.pickup_point_id = subscription.dropoff_point_id
--             AND link.is_deleted = false
--       )
--   );
-- Expected: 0.
