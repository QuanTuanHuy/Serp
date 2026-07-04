CREATE TABLE public.school_bus_user_role
(
    id              bigserial primary key,
    tenant_id       bigint                              not null,
    user_id         bigint                              not null,
    role_name       varchar(100)                        not null,
    is_active       boolean default true                not null,
    is_deleted      boolean default false               not null,
    created_at      timestamp default CURRENT_TIMESTAMP not null,
    created_by      varchar(100),
    updated_at      timestamp default CURRENT_TIMESTAMP not null,
    updated_by      varchar(100),

    CONSTRAINT fk_school_bus_user_role_user
        FOREIGN KEY (user_id) REFERENCES public.school_bus_user (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uk_school_bus_user_role_active
    ON public.school_bus_user_role (user_id, role_name)
    WHERE is_deleted = false;

CREATE INDEX idx_school_bus_user_role_tenant_role
    ON public.school_bus_user_role (tenant_id, role_name, is_active, is_deleted);

DO
$$
DECLARE
    user_row RECORD;
    payload JSONB;
    role_array JSONB;
    role_value TEXT;
BEGIN
    FOR user_row IN
        SELECT id, tenant_id, raw_payload_json
        FROM public.school_bus_user
        WHERE is_deleted = false
          AND raw_payload_json IS NOT NULL
          AND btrim(raw_payload_json) <> ''
    LOOP
        BEGIN
            payload := user_row.raw_payload_json::jsonb;
            role_array := CASE
                WHEN jsonb_typeof(payload -> 'roles') = 'array' THEN payload -> 'roles'
                WHEN jsonb_typeof(payload -> 'roleNames') = 'array' THEN payload -> 'roleNames'
                ELSE '[]'::jsonb
            END;

            FOR role_value IN
                SELECT value
                FROM jsonb_array_elements_text(role_array) AS role_values(value)
            LOOP
                role_value := upper(btrim(role_value));
                IF role_value LIKE 'SCHOOL_BUS_%' THEN
                    INSERT INTO public.school_bus_user_role
                        (tenant_id, user_id, role_name, is_active, is_deleted,
                         created_at, created_by, updated_at, updated_by)
                    VALUES
                        (user_row.tenant_id, user_row.id, role_value, true, false,
                         CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM')
                    ON CONFLICT DO NOTHING;
                END IF;
            END LOOP;
        EXCEPTION
            WHEN OTHERS THEN
                CONTINUE;
        END;
    END LOOP;
END
$$;
