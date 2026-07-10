-- Compact school bus seed data for short route-planning demonstrations.
-- The data follows the same code namespace as the foundation seed and does not
-- create route/trip records, so users can create planning sessions manually.

DO $$
DECLARE
    v_tenant_id CONSTANT bigint := 1;
    v_seed_by CONSTANT varchar(100) := 'SEED_DATA';

    v_school_id bigint;
    v_pickup_point_id bigint;
    v_user_id bigint;
    v_parent_profile_id bigint;
    v_request_id bigint;
    v_student_id bigint;
    v_subscription_id bigint;
    v_account_user_id bigint;
    v_parent_name text;
    v_student_name text;
    v_student_number integer;
    v_student_code text;
    v_request_code text;
    v_subscription_code text;
    v_point_code text;
    v_point_address text;
    v_sequence_key text;
    v_expected_next bigint;
    v_keeper_id bigint;
    v_max_next bigint;
    v_non_seed_count integer;
    i integer;
    j integer;

    v_parent_names text[] := ARRAY[
        'Nguyễn Minh Hoàng',
        'Trần Thu Phương',
        'Lê Quốc Anh',
        'Phạm Ngọc Hà'
    ];
    v_student_names text[] := ARRAY[
        'Nguyễn Hoàng An',
        'Nguyễn Minh Khang',
        'Trần Bảo Ngọc',
        'Trần Gia Hân',
        'Lê Đức Minh',
        'Lê Tuệ Lâm',
        'Phạm Nhật Nam',
        'Phạm Khánh Linh'
    ];
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.school_bus_school
        WHERE tenant_id = v_tenant_id
          AND code = 'SBU000004'
          AND is_deleted = false
          AND created_by IS DISTINCT FROM v_seed_by
    ) THEN
        RAISE EXCEPTION 'Seed school code namespace collision detected';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_pickup_point
        WHERE tenant_id = v_tenant_id
          AND code IN ('PKP000025', 'PKP000026')
          AND is_deleted = false
          AND created_by IS DISTINCT FROM v_seed_by
    ) THEN
        RAISE EXCEPTION 'Seed pickup point code namespace collision detected';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_user
        WHERE account_user_id BETWEEN 900100076 AND 900100079
          AND is_deleted = false
          AND created_by IS DISTINCT FROM v_seed_by
    ) THEN
        RAISE EXCEPTION 'Seed parent user namespace collision detected';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_student
        WHERE tenant_id = v_tenant_id
          AND student_code BETWEEN 'STU000121' AND 'STU000128'
          AND is_deleted = false
          AND created_by IS DISTINCT FROM v_seed_by
    ) THEN
        RAISE EXCEPTION 'Seed student code namespace collision detected';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_transport_request
        WHERE tenant_id = v_tenant_id
          AND request_code BETWEEN 'REQ000091' AND 'REQ000094'
          AND is_deleted = false
          AND created_by IS DISTINCT FROM v_seed_by
    ) THEN
        RAISE EXCEPTION 'Seed request code namespace collision detected';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.school_bus_student_subscription
        WHERE tenant_id = v_tenant_id
          AND subscription_code BETWEEN 'SUB000121' AND 'SUB000128'
          AND is_deleted = false
          AND created_by IS DISTINCT FROM v_seed_by
    ) THEN
        RAISE EXCEPTION 'Seed subscription code namespace collision detected';
    END IF;

    INSERT INTO public.school_bus_school (
        tenant_id, name, code, address, contact_phone, contact_email,
        latitude, longitude, is_active, is_deleted,
        created_at, created_by, updated_at, updated_by
    )
    SELECT
        v_tenant_id,
        'Trường THPT Serp',
        'SBU000004',
        'Số 1 Đại Cồ Việt, quận Hai Bà Trưng, Hà Nội',
        '02439001004',
        'highschool@serp-school.edu.vn',
        21.007306,
        105.843084,
        true,
        false,
        CURRENT_TIMESTAMP,
        v_seed_by,
        CURRENT_TIMESTAMP,
        v_seed_by
    WHERE NOT EXISTS (
        SELECT 1
        FROM public.school_bus_school
        WHERE tenant_id = v_tenant_id
          AND code = 'SBU000004'
          AND is_deleted = false
    );

    UPDATE public.school_bus_school
    SET name = 'Trường THPT Serp',
        address = 'Số 1 Đại Cồ Việt, quận Hai Bà Trưng, Hà Nội',
        contact_phone = '02439001004',
        contact_email = 'highschool@serp-school.edu.vn',
        latitude = 21.007306,
        longitude = 105.843084,
        is_active = true,
        updated_at = CURRENT_TIMESTAMP,
        updated_by = v_seed_by
    WHERE tenant_id = v_tenant_id
      AND code = 'SBU000004'
      AND is_deleted = false
      AND created_by = v_seed_by;

    SELECT id
    INTO STRICT v_school_id
    FROM public.school_bus_school
    WHERE tenant_id = v_tenant_id
      AND code = 'SBU000004'
      AND is_deleted = false
    ORDER BY id
    LIMIT 1;

    INSERT INTO public.school_bus_pickup_point (
        tenant_id, code, name, address, latitude, longitude,
        usage_type, pickup_instruction, is_active, is_deleted,
        created_at, created_by, updated_at, updated_by
    )
    SELECT
        v_tenant_id,
        'PKP000025',
        'Khu tập thể Bách Khoa',
        'Phố Tạ Quang Bửu, quận Hai Bà Trưng, Hà Nội',
        21.005046,
        105.846401,
        'PICKUP_DROPOFF',
        'Tập trung trước cổng khu tập thể trên phố Tạ Quang Bửu.',
        true,
        false,
        CURRENT_TIMESTAMP,
        v_seed_by,
        CURRENT_TIMESTAMP,
        v_seed_by
    WHERE NOT EXISTS (
        SELECT 1
        FROM public.school_bus_pickup_point
        WHERE tenant_id = v_tenant_id
          AND code = 'PKP000025'
          AND is_deleted = false
    );

    INSERT INTO public.school_bus_pickup_point (
        tenant_id, code, name, address, latitude, longitude,
        usage_type, pickup_instruction, is_active, is_deleted,
        created_at, created_by, updated_at, updated_by
    )
    SELECT
        v_tenant_id,
        'PKP000026',
        'Công viên Thống Nhất',
        'Đường Trần Nhân Tông, quận Hai Bà Trưng, Hà Nội',
        21.017995,
        105.843664,
        'PICKUP_DROPOFF',
        'Chờ tại cổng chính công viên phía đường Trần Nhân Tông.',
        true,
        false,
        CURRENT_TIMESTAMP,
        v_seed_by,
        CURRENT_TIMESTAMP,
        v_seed_by
    WHERE NOT EXISTS (
        SELECT 1
        FROM public.school_bus_pickup_point
        WHERE tenant_id = v_tenant_id
          AND code = 'PKP000026'
          AND is_deleted = false
    );

    UPDATE public.school_bus_pickup_point
    SET name = CASE code
            WHEN 'PKP000025' THEN 'Khu tập thể Bách Khoa'
            ELSE 'Công viên Thống Nhất'
        END,
        address = CASE code
            WHEN 'PKP000025' THEN 'Phố Tạ Quang Bửu, quận Hai Bà Trưng, Hà Nội'
            ELSE 'Đường Trần Nhân Tông, quận Hai Bà Trưng, Hà Nội'
        END,
        latitude = CASE code
            WHEN 'PKP000025' THEN 21.005046
            ELSE 21.017995
        END,
        longitude = CASE code
            WHEN 'PKP000025' THEN 105.846401
            ELSE 105.843664
        END,
        usage_type = 'PICKUP_DROPOFF',
        is_active = true,
        updated_at = CURRENT_TIMESTAMP,
        updated_by = v_seed_by
    WHERE tenant_id = v_tenant_id
      AND code IN ('PKP000025', 'PKP000026')
      AND is_deleted = false
      AND created_by = v_seed_by;

    FOR v_point_code IN
        SELECT unnest(ARRAY['PKP000025', 'PKP000026'])
    LOOP
        SELECT id
        INTO STRICT v_pickup_point_id
        FROM public.school_bus_pickup_point
        WHERE tenant_id = v_tenant_id
          AND code = v_point_code
          AND is_deleted = false
        ORDER BY id
        LIMIT 1;

        INSERT INTO public.school_bus_school_pickup_point (
            tenant_id, school_id, pickup_point_id, is_default,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        SELECT
            v_tenant_id,
            v_school_id,
            v_pickup_point_id,
            false,
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        WHERE NOT EXISTS (
            SELECT 1
            FROM public.school_bus_school_pickup_point link
            WHERE link.school_id = v_school_id
              AND link.pickup_point_id = v_pickup_point_id
              AND link.is_deleted = false
        );

        UPDATE public.school_bus_school_pickup_point
        SET is_default = false,
            is_active = true,
            updated_at = CURRENT_TIMESTAMP,
            updated_by = v_seed_by
        WHERE school_id = v_school_id
          AND pickup_point_id = v_pickup_point_id
          AND is_deleted = false
          AND created_by = v_seed_by;
    END LOOP;

    FOR i IN 1..4 LOOP
        v_parent_name := v_parent_names[i];
        v_account_user_id := 900100075 + i;
        v_request_code := 'REQ0000' || (90 + i)::text;

        INSERT INTO public.school_bus_user (
            tenant_id, account_user_id, keycloak_id, email,
            first_name, last_name, full_name, phone_number,
            primary_organization_id, preferred_language, timezone,
            user_type, status, last_synced_at, sync_source, raw_payload_json,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        SELECT
            v_tenant_id,
            v_account_user_id,
            NULL,
            'parent' || lpad((75 + i)::text, 3, '0') || '@serp-school.edu.vn',
            regexp_replace(v_parent_name, '^.* ', ''),
            split_part(v_parent_name, ' ', 1),
            v_parent_name,
            '0901' || lpad((75 + i)::text, 6, '0'),
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
        WHERE NOT EXISTS (
            SELECT 1
            FROM public.school_bus_user
            WHERE account_user_id = v_account_user_id
              AND is_deleted = false
        );

        UPDATE public.school_bus_user
        SET email = 'parent' || lpad((75 + i)::text, 3, '0') || '@serp-school.edu.vn',
            first_name = regexp_replace(v_parent_name, '^.* ', ''),
            last_name = split_part(v_parent_name, ' ', 1),
            full_name = v_parent_name,
            phone_number = '0901' || lpad((75 + i)::text, 6, '0'),
            user_type = 'PARENT',
            status = 'ACTIVE',
            is_active = true,
            last_synced_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP,
            updated_by = v_seed_by
        WHERE account_user_id = v_account_user_id
          AND is_deleted = false
          AND created_by = v_seed_by;

        SELECT id
        INTO STRICT v_user_id
        FROM public.school_bus_user
        WHERE account_user_id = v_account_user_id
          AND is_deleted = false
        ORDER BY id
        LIMIT 1;

        INSERT INTO public.school_bus_parent_profile (
            tenant_id, user_id, full_name, phone, email, address,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        SELECT
            v_tenant_id,
            v_user_id,
            v_parent_name,
            '0901' || lpad((75 + i)::text, 6, '0'),
            'parent' || lpad((75 + i)::text, 3, '0') || '@serp-school.edu.vn',
            'Quận Hai Bà Trưng, Hà Nội',
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        WHERE NOT EXISTS (
            SELECT 1
            FROM public.school_bus_parent_profile
            WHERE tenant_id = v_tenant_id
              AND user_id = v_user_id
              AND is_deleted = false
        );

        UPDATE public.school_bus_parent_profile
        SET full_name = v_parent_name,
            phone = '0901' || lpad((75 + i)::text, 6, '0'),
            email = 'parent' || lpad((75 + i)::text, 3, '0') || '@serp-school.edu.vn',
            address = 'Quận Hai Bà Trưng, Hà Nội',
            is_active = true,
            updated_at = CURRENT_TIMESTAMP,
            updated_by = v_seed_by
        WHERE tenant_id = v_tenant_id
          AND user_id = v_user_id
          AND is_deleted = false
          AND created_by = v_seed_by;

        SELECT id
        INTO STRICT v_parent_profile_id
        FROM public.school_bus_parent_profile
        WHERE tenant_id = v_tenant_id
          AND user_id = v_user_id
          AND is_deleted = false
        ORDER BY id
        LIMIT 1;

        INSERT INTO public.school_bus_transport_request (
            tenant_id, parent_profile_id, request_type, status,
            effective_from, effective_to, notes, approved_by, approved_at,
            rejection_reason, request_code, requested_at, request_source, change_reason,
            is_active, is_deleted, created_at, created_by, updated_at, updated_by
        )
        SELECT
            v_tenant_id,
            v_parent_profile_id,
            'NEW_SERVICE',
            'APPROVED',
            CURRENT_DATE - 7,
            NULL,
            'Đăng ký sử dụng dịch vụ đưa đón hằng ngày.',
            1,
            CURRENT_TIMESTAMP - make_interval(days => 9 - i),
            NULL,
            v_request_code,
            CURRENT_TIMESTAMP - make_interval(days => 10 - i),
            'ADMIN',
            NULL,
            true,
            false,
            CURRENT_TIMESTAMP,
            v_seed_by,
            CURRENT_TIMESTAMP,
            v_seed_by
        WHERE NOT EXISTS (
            SELECT 1
            FROM public.school_bus_transport_request
            WHERE tenant_id = v_tenant_id
              AND request_code = v_request_code
              AND is_deleted = false
        );

        UPDATE public.school_bus_transport_request
        SET parent_profile_id = v_parent_profile_id,
            request_type = 'NEW_SERVICE',
            status = 'APPROVED',
            effective_from = CURRENT_DATE - 7,
            effective_to = NULL,
            notes = 'Đăng ký sử dụng dịch vụ đưa đón hằng ngày.',
            approved_by = 1,
            approved_at = CURRENT_TIMESTAMP - make_interval(days => 9 - i),
            request_source = 'ADMIN',
            is_active = true,
            updated_at = CURRENT_TIMESTAMP,
            updated_by = v_seed_by
        WHERE tenant_id = v_tenant_id
          AND request_code = v_request_code
          AND is_deleted = false
          AND created_by = v_seed_by;

        SELECT id
        INTO STRICT v_request_id
        FROM public.school_bus_transport_request
        WHERE tenant_id = v_tenant_id
          AND request_code = v_request_code
          AND is_deleted = false
        ORDER BY id
        LIMIT 1;

        FOR j IN 1..2 LOOP
            v_student_number := ((i - 1) * 2) + j;
            v_student_name := v_student_names[v_student_number];
            v_student_code := 'STU000' || (120 + v_student_number)::text;
            v_subscription_code := 'SUB000' || (120 + v_student_number)::text;
            v_point_code := CASE WHEN v_student_number <= 4 THEN 'PKP000025' ELSE 'PKP000026' END;

            SELECT id, address
            INTO STRICT v_pickup_point_id, v_point_address
            FROM public.school_bus_pickup_point
            WHERE tenant_id = v_tenant_id
              AND code = v_point_code
              AND is_deleted = false
            ORDER BY id
            LIMIT 1;

            INSERT INTO public.school_bus_student (
                tenant_id, school_id, parent_profile_id,
                pickup_point_id, default_dropoff_point_id,
                full_name, student_code, grade, class_name,
                home_address, date_of_birth, gender, special_note,
                is_active, is_deleted, created_at, created_by, updated_at, updated_by
            )
            SELECT
                v_tenant_id,
                v_school_id,
                v_parent_profile_id,
                v_pickup_point_id,
                v_pickup_point_id,
                v_student_name,
                v_student_code,
                '10',
                CASE WHEN v_student_number <= 4 THEN '10A1' ELSE '10A2' END,
                v_point_address,
                date '2010-09-01' + v_student_number,
                CASE WHEN v_student_number % 2 = 0 THEN 'FEMALE' ELSE 'MALE' END,
                'Học sinh đăng ký tuyến ngắn trong khu vực Hai Bà Trưng.',
                true,
                false,
                CURRENT_TIMESTAMP,
                v_seed_by,
                CURRENT_TIMESTAMP,
                v_seed_by
            WHERE NOT EXISTS (
                SELECT 1
                FROM public.school_bus_student
                WHERE tenant_id = v_tenant_id
                  AND student_code = v_student_code
                  AND is_deleted = false
            );

            UPDATE public.school_bus_student
            SET school_id = v_school_id,
                parent_profile_id = v_parent_profile_id,
                pickup_point_id = v_pickup_point_id,
                default_dropoff_point_id = v_pickup_point_id,
                full_name = v_student_name,
                grade = '10',
                class_name = CASE WHEN v_student_number <= 4 THEN '10A1' ELSE '10A2' END,
                home_address = v_point_address,
                special_note = 'Học sinh đăng ký tuyến ngắn trong khu vực Hai Bà Trưng.',
                is_active = true,
                updated_at = CURRENT_TIMESTAMP,
                updated_by = v_seed_by
            WHERE tenant_id = v_tenant_id
              AND student_code = v_student_code
              AND is_deleted = false
              AND created_by = v_seed_by;

            SELECT id
            INTO STRICT v_student_id
            FROM public.school_bus_student
            WHERE tenant_id = v_tenant_id
              AND student_code = v_student_code
              AND is_deleted = false
            ORDER BY id
            LIMIT 1;

            INSERT INTO public.school_bus_request_student (
                tenant_id, request_id, student_id,
                pickup_point_id, dropoff_point_id, trip_option,
                is_monday, is_tuesday, is_wednesday, is_thursday, is_friday,
                is_saturday, is_sunday,
                subscription_id, target_subscription_id, student_note,
                is_active, is_deleted, created_at, created_by, updated_at, updated_by
            )
            SELECT
                v_tenant_id,
                v_request_id,
                v_student_id,
                v_pickup_point_id,
                v_pickup_point_id,
                'ROUND_TRIP',
                true, true, true, true, true,
                false, false,
                NULL,
                NULL,
                'Đăng ký đã được phê duyệt.',
                true,
                false,
                CURRENT_TIMESTAMP,
                v_seed_by,
                CURRENT_TIMESTAMP,
                v_seed_by
            WHERE NOT EXISTS (
                SELECT 1
                FROM public.school_bus_request_student
                WHERE tenant_id = v_tenant_id
                  AND request_id = v_request_id
                  AND student_id = v_student_id
                  AND is_deleted = false
            );

            UPDATE public.school_bus_request_student
            SET pickup_point_id = v_pickup_point_id,
                dropoff_point_id = v_pickup_point_id,
                trip_option = 'ROUND_TRIP',
                student_note = 'Đăng ký đã được phê duyệt.',
                is_active = true,
                updated_at = CURRENT_TIMESTAMP,
                updated_by = v_seed_by
            WHERE tenant_id = v_tenant_id
              AND request_id = v_request_id
              AND student_id = v_student_id
              AND is_deleted = false
              AND created_by = v_seed_by;

            INSERT INTO public.school_bus_student_subscription (
                tenant_id, student_id,
                pickup_point_id, dropoff_point_id,
                subscription_code, trip_option,
                is_monday, is_tuesday, is_wednesday, is_thursday, is_friday,
                is_saturday, is_sunday,
                effective_from, effective_to, status, source_request_id,
                is_active, is_deleted, created_at, created_by, updated_at, updated_by
            )
            SELECT
                v_tenant_id,
                v_student_id,
                v_pickup_point_id,
                v_pickup_point_id,
                v_subscription_code,
                'ROUND_TRIP',
                true, true, true, true, true,
                false, false,
                CURRENT_DATE - 7,
                NULL,
                'ACTIVE',
                v_request_id,
                true,
                false,
                CURRENT_TIMESTAMP,
                v_seed_by,
                CURRENT_TIMESTAMP,
                v_seed_by
            WHERE NOT EXISTS (
                SELECT 1
                FROM public.school_bus_student_subscription
                WHERE tenant_id = v_tenant_id
                  AND subscription_code = v_subscription_code
                  AND is_deleted = false
            );

            UPDATE public.school_bus_student_subscription
            SET student_id = v_student_id,
                pickup_point_id = v_pickup_point_id,
                dropoff_point_id = v_pickup_point_id,
                trip_option = 'ROUND_TRIP',
                effective_from = CURRENT_DATE - 7,
                effective_to = NULL,
                status = 'ACTIVE',
                source_request_id = v_request_id,
                is_active = true,
                updated_at = CURRENT_TIMESTAMP,
                updated_by = v_seed_by
            WHERE tenant_id = v_tenant_id
              AND subscription_code = v_subscription_code
              AND is_deleted = false
              AND created_by = v_seed_by;

            SELECT id
            INTO STRICT v_subscription_id
            FROM public.school_bus_student_subscription
            WHERE tenant_id = v_tenant_id
              AND subscription_code = v_subscription_code
              AND is_deleted = false
            ORDER BY id
            LIMIT 1;

            UPDATE public.school_bus_request_student
            SET subscription_id = v_subscription_id,
                updated_at = CURRENT_TIMESTAMP,
                updated_by = v_seed_by
            WHERE tenant_id = v_tenant_id
              AND request_id = v_request_id
              AND student_id = v_student_id
              AND is_deleted = false
              AND created_by = v_seed_by;
        END LOOP;
    END LOOP;

    FOR v_sequence_key, v_expected_next IN
        SELECT *
        FROM (
            VALUES
                ('SCHOOL'::text, 5::bigint),
                ('PICKUP_POINT'::text, 27::bigint),
                ('STUDENT'::text, 129::bigint),
                ('REQUEST'::text, 95::bigint),
                ('SUBSCRIPTION'::text, 129::bigint)
        ) AS expected(sequence_key, next_value)
    LOOP
        SELECT
            count(*) FILTER (WHERE created_by IS DISTINCT FROM v_seed_by),
            max(next_value)
        INTO v_non_seed_count, v_max_next
        FROM public.school_bus_code_sequence
        WHERE tenant_id = v_tenant_id
          AND sequence_key = v_sequence_key
          AND is_deleted = false;

        IF v_non_seed_count > 1 THEN
            RAISE EXCEPTION
                'Multiple non-seed active code sequence rows detected for tenant %, key %',
                v_tenant_id,
                v_sequence_key;
        END IF;

        SELECT id
        INTO v_keeper_id
        FROM public.school_bus_code_sequence
        WHERE tenant_id = v_tenant_id
          AND sequence_key = v_sequence_key
          AND is_deleted = false
        ORDER BY
            CASE WHEN created_by IS DISTINCT FROM v_seed_by THEN 0 ELSE 1 END,
            id
        LIMIT 1;

        IF v_keeper_id IS NULL THEN
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
        ELSE
            UPDATE public.school_bus_code_sequence
            SET next_value = GREATEST(COALESCE(v_max_next, 0), v_expected_next),
                is_active = true,
                updated_at = CURRENT_TIMESTAMP,
                updated_by = v_seed_by
            WHERE id = v_keeper_id;

            DELETE FROM public.school_bus_code_sequence
            WHERE tenant_id = v_tenant_id
              AND sequence_key = v_sequence_key
              AND is_deleted = false
              AND id <> v_keeper_id
              AND created_by = v_seed_by;
        END IF;
    END LOOP;
END $$;
