create table public.school_bus_user
(
    id                      bigserial primary key,
    tenant_id               bigint                              not null,

    account_user_id          bigint                              not null,
    keycloak_id              varchar(255),
    email                    varchar(255)                        not null,
    first_name               varchar(255),
    last_name                varchar(255),
    full_name                varchar(512),
    phone_number             varchar(100),
    avatar_url               varchar(1000),

    primary_organization_id  bigint,
    preferred_language       varchar(50),
    timezone                 varchar(100),
    user_type                varchar(100),
    status                   varchar(100),

    last_synced_at           timestamp,
    sync_source              varchar(50),
    raw_payload_json         text,

    is_active                boolean default true                not null,
    is_deleted               boolean default false               not null,
    created_at               timestamp default CURRENT_TIMESTAMP not null,
    created_by               varchar(100),
    updated_at               timestamp default CURRENT_TIMESTAMP not null,
    updated_by               varchar(100)
);

create unique index uk_school_bus_user_account_user
    on public.school_bus_user (account_user_id)
    where is_deleted = false;

create unique index uk_school_bus_user_keycloak
    on public.school_bus_user (keycloak_id)
    where keycloak_id is not null and is_deleted = false;

create unique index uk_school_bus_user_tenant_email
    on public.school_bus_user (tenant_id, email)
    where is_deleted = false;

create index idx_school_bus_user_tenant_deleted
    on public.school_bus_user (tenant_id, is_deleted);

create index idx_school_bus_user_tenant_status
    on public.school_bus_user (tenant_id, status, is_deleted);
