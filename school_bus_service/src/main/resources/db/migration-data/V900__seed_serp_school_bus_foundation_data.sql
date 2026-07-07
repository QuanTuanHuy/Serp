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
            'Trường Mầm non Serp',
            'SBU000001',
            'Khuôn viên Quốc tế Serp, Minh Khai, quận Hai Bà Trưng, Hà Nội',
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
            'Trường Tiểu học Serp',
            'SBU000002',
            'Khuôn viên Quốc tế Serp, Minh Khai, quận Hai Bà Trưng, Hà Nội',
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
            'Trường THCS Serp',
            'SBU000003',
            'Khuôn viên Quốc tế Serp, Minh Khai, quận Hai Bà Trưng, Hà Nội',
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
            'Bãi xe vận hành Serp - phía Tây',
            'Mỹ Đình, quận Nam Từ Liêm, Hà Nội',
            21.028877,
            105.778137,
            '02439002001',
            'Bãi vận hành và bảo dưỡng đội xe khu phía Tây',
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
            'Bãi xe vận hành Serp - phía Đông',
            'Quận Long Biên, Hà Nội',
            21.027809,
            105.899211,
            '02439002002',
            'Bãi vận hành và điều phối đội xe khu phía Đông',
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
        (v_tenant_id, 'PKP000001', 'Times City', '458 Minh Khai, quận Hai Bà Trưng, Hà Nội', 20.994102, 105.868334, 'PICKUP_DROPOFF', 'Tập trung cạnh sảnh cư dân chính trên phố Minh Khai.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000002', 'Royal City', '72A Nguyễn Trãi, quận Thanh Xuân, Hà Nội', 21.002504, 105.815117, 'PICKUP_DROPOFF', 'Chờ tại khu vực đón xe gần lối vào R2.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000003', 'Khu dân cư Trung Hòa', 'Hoàng Đạo Thúy, quận Cầu Giấy, Hà Nội', 21.006521, 105.801284, 'PICKUP_DROPOFF', 'Tập trung tại cổng khu dân cư phía đường Hoàng Đạo Thúy.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000004', 'Nút giao Cầu Giấy', 'Đường Cầu Giấy, quận Cầu Giấy, Hà Nội', 21.033239, 105.800387, 'PICKUP_DROPOFF', 'Đứng tại khu vực đón khách gần nhà chờ xe bus công cộng.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000005', 'Công viên Nghĩa Đô', 'Nguyễn Văn Huyên, quận Cầu Giấy, Hà Nội', 21.041875, 105.797231, 'PICKUP_DROPOFF', 'Tập trung tại cổng phía nam cạnh đường Nguyễn Văn Huyên.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000006', 'Keangnam Landmark', 'Đường Phạm Hùng, quận Nam Từ Liêm, Hà Nội', 21.017312, 105.783122, 'PICKUP_DROPOFF', 'Chờ tại làn đón khách ngoài khu tòa nhà.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000007', 'Bến xe Mỹ Đình', 'Đường Phạm Hùng, quận Nam Từ Liêm, Hà Nội', 21.028952, 105.778842, 'PICKUP_DROPOFF', 'Tập trung tại làn đón khách phía bắc.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000008', 'Khu đô thị Ciputra', 'Nguyễn Hoàng Tôn, quận Tây Hồ, Hà Nội', 21.074734, 105.789426, 'PICKUP_DROPOFF', 'Tập trung tại cổng lễ tân chính của khu dân cư.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000009', 'Khu dân cư Hồ Tây', 'Lạc Long Quân, quận Tây Hồ, Hà Nội', 21.067418, 105.810724, 'PICKUP_DROPOFF', 'Chờ tại lối xe ra vào trên đường Lạc Long Quân.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000010', 'Lotte Center Hanoi', '54 Liễu Giai, quận Ba Đình, Hà Nội', 21.032078, 105.812887, 'PICKUP_DROPOFF', 'Tập trung tại khu vực đón khách trên phố Đào Tấn.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000011', 'Khu dân cư Giảng Võ', 'Phố Giảng Võ, quận Ba Đình, Hà Nội', 21.027109, 105.823782, 'PICKUP_DROPOFF', 'Tập trung cạnh lối vào nhà văn hóa khu dân cư.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000012', 'Khu dân cư Kim Mã', 'Phố Kim Mã, quận Ba Đình, Hà Nội', 21.030297, 105.821464, 'PICKUP_DROPOFF', 'Chờ tại phần vỉa hè rộng đối diện bưu điện địa phương.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000013', 'Khu đô thị Văn Quán', 'Nguyễn Khuyến, quận Hà Đông, Hà Nội', 20.980912, 105.787615, 'PICKUP_DROPOFF', 'Tập trung gần cổng khu dân cư ven hồ.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000014', 'Khu đô thị Linh Đàm', 'Nguyễn Hữu Thọ, quận Hoàng Mai, Hà Nội', 20.964817, 105.826927, 'PICKUP_DROPOFF', 'Tập trung tại khu vực chờ taxi và đón khách chính.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000015', 'Khu dân cư Giải Phóng', 'Đường Giải Phóng, quận Hoàng Mai, Hà Nội', 20.982884, 105.841625, 'PICKUP_DROPOFF', 'Chờ tại cổng khu dân cư, tránh khu vực giao cắt.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000016', 'Khu dân cư Bạch Mai', 'Phố Bạch Mai, quận Hai Bà Trưng, Hà Nội', 21.000463, 105.849769, 'PICKUP_DROPOFF', 'Tập trung cạnh lối vào nhà sinh hoạt cộng đồng.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000017', 'Khu dân cư Minh Khai', 'Phố Minh Khai, quận Hai Bà Trưng, Hà Nội', 20.997018, 105.858936, 'PICKUP_DROPOFF', 'Chờ tại lối ra đường nội bộ hướng ra phố Minh Khai.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000018', 'Aeon Mall Long Biên', '27 Cổ Linh, quận Long Biên, Hà Nội', 21.027732, 105.899985, 'PICKUP_DROPOFF', 'Tập trung tại khu đón khách buổi sáng gần lối vào phía tây.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000019', 'Khu ven sông Long Biên', 'Ngọc Thụy, quận Long Biên, Hà Nội', 21.047119, 105.859754, 'PICKUP_ONLY', 'Điểm đón buổi sáng tại cổng khu dân cư ven sông.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000020', 'Khu vực Hồ Hoàn Kiếm', 'Đinh Tiên Hoàng, quận Hoàn Kiếm, Hà Nội', 21.028666, 105.852448, 'PICKUP_ONLY', 'Điểm đón buổi sáng cạnh khu vực xe du lịch được chỉ định.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000021', 'Khu dân cư Vạn Phúc', 'Đường Tố Hữu, quận Hà Đông, Hà Nội', 20.982147, 105.769863, 'PICKUP_ONLY', 'Điểm đón buổi sáng tại cổng bảo vệ chính của khu dân cư.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000022', 'Khu dân cư Hà Đông', 'Đường Quang Trung, quận Hà Đông, Hà Nội', 20.971542, 105.778491, 'DROPOFF_ONLY', 'Điểm trả buổi chiều tại lối vào khu dân cư có mái che.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000023', 'Ecopark Residence', 'Khu đô thị Ecopark, huyện Văn Giang, Hưng Yên', 20.956233, 105.930416, 'DROPOFF_ONLY', 'Điểm trả buổi chiều tại sảnh lễ tân trung tâm khu dân cư.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by),
        (v_tenant_id, 'PKP000024', 'Khu dân cư Hoàng Mai', 'Đường Tam Trinh, quận Hoàng Mai, Hà Nội', 20.983613, 105.867412, 'DROPOFF_ONLY', 'Điểm trả buổi chiều cạnh cổng sân chơi cộng đồng.', true, false, CURRENT_TIMESTAMP, v_seed_by, CURRENT_TIMESTAMP, v_seed_by);

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
                WHEN i <= 20 THEN 'Quận Hai Bà Trưng, Hà Nội'
                WHEN i <= 50 THEN 'Quận Cầu Giấy, Hà Nội'
                ELSE 'Quận Long Biên, Hà Nội'
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
                WHEN i % 17 = 0 THEN 'Gia đình mong muốn học sinh ngồi hàng ghế đầu khi còn chỗ.'
                WHEN i % 23 = 0 THEN 'Vui lòng nhắc học sinh chuẩn bị trước khi đến điểm dừng.'
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
            tenant_id, parent_profile_id,
            request_type, status, request_code, requested_at, request_source,
            effective_from, effective_to, notes,
            approved_by, approved_at, rejection_reason, change_reason,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        VALUES (
            v_tenant_id,
            v_parent_profile_id,
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
            tenant_id, parent_profile_id,
            request_type, status, request_code, requested_at, request_source,
            effective_from, effective_to, notes,
            approved_by, approved_at, rejection_reason, change_reason,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        VALUES (
            v_tenant_id,
            v_record.parent_profile_id,
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

    -- Active subscriptions.
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
          AND request.status = 'APPROVED'
          AND request.created_by = v_seed_by
          AND request.is_deleted = false;

        v_trip_option := CASE
            WHEN i <= 72 THEN 'ROUND_TRIP'
            WHEN i <= 102 THEN 'MORNING'
            ELSE 'AFTERNOON'
        END;

        INSERT INTO public.school_bus_student_subscription (
            tenant_id, student_id,
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
-- UNION ALL SELECT 'subscriptions', count(*) FROM public.school_bus_student_subscription WHERE created_by = 'SEED_DATA'
--
-- Expected: schools=3, depots=2, users=105, buses=15, parents=75, drivers=18,
-- attendants=12, pickup_points=24, school_pickup_links=58, students=120,
-- requests=90, request_students=135, subscriptions=120.
--
-- SELECT
--     school.name AS school,
--     subscription.trip_option,
--     count(*) AS total
-- FROM public.school_bus_student_subscription subscription
-- JOIN public.school_bus_student student ON student.id = subscription.student_id
-- JOIN public.school_bus_school school ON school.id = student.school_id
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
--           WHERE link.school_id = student.school_id
--             AND link.pickup_point_id = subscription.pickup_point_id
--             AND link.is_deleted = false
--       )
--       OR NOT EXISTS (
--           SELECT 1
--           FROM public.school_bus_school_pickup_point link
--           WHERE link.school_id = student.school_id
--             AND link.pickup_point_id = subscription.dropoff_point_id
--             AND link.is_deleted = false
--       )
--   );
-- Expected: 0.
