-- V12: Schedule-aware school pickup point windows
-- =====================================================================
-- Window giờ đón/trả không nằm cố định ở school_bus_school_pickup_point nữa.
-- Thay vào đó, tạo bảng con gắn window theo (linked pickup point, schedule, direction).
-- =====================================================================

-- 1. Tạo bảng mới
create table if not exists school_bus_school_pickup_point_window
(
    id                              bigserial    primary key,
    tenant_id                       bigint       not null,

    school_pickup_point_id          bigint       not null
        references school_bus_school_pickup_point(id),

    school_schedule_id              bigint       not null
        references school_bus_school_schedule(id),

    direction                       varchar(40)  not null,

    window_start                    time         not null,
    window_end                      time         not null,

    estimated_distance_to_school_km double precision,
    estimated_duration_to_school_min integer,

    is_active                       boolean      default true  not null,
    is_deleted                      boolean      default false not null,

    created_at                      timestamp    default current_timestamp not null,
    created_by                      varchar(100),
    updated_at                      timestamp    default current_timestamp not null,
    updated_by                      varchar(100),

    constraint chk_spp_window_direction
        check (direction in ('PICKUP_TO_SCHOOL', 'DROPOFF_FROM_SCHOOL')),

    constraint chk_spp_window_time_range
        check (window_end >= window_start),

    constraint chk_spp_window_distance_non_negative
        check (
            estimated_distance_to_school_km is null
            or estimated_distance_to_school_km >= 0
        ),

    constraint chk_spp_window_duration_non_negative
        check (
            estimated_duration_to_school_min is null
            or estimated_duration_to_school_min >= 0
        )
);

-- 2. Unique mềm: 1 linked pickup point + 1 schedule + 1 direction = 1 active window
create unique index if not exists uk_spp_window_active
    on school_bus_school_pickup_point_window
    (tenant_id, school_pickup_point_id, school_schedule_id, direction)
    where is_deleted = false;

-- 3. Indexes phục vụ query
create index if not exists idx_spp_window_spp
    on school_bus_school_pickup_point_window
    (school_pickup_point_id, is_deleted);

create index if not exists idx_spp_window_schedule
    on school_bus_school_pickup_point_window
    (school_schedule_id, is_deleted);

create index if not exists idx_spp_window_tenant_schedule_direction
    on school_bus_school_pickup_point_window
    (tenant_id, school_schedule_id, direction, is_deleted);

-- 4. Drop cột window cũ khỏi school_bus_school_pickup_point
-- Cột window cố định sáng/chiều không còn phù hợp, dữ liệu giờ chuyển sang bảng con.
alter table school_bus_school_pickup_point
    drop column if exists morning_pickup_window_start,
    drop column if exists morning_pickup_window_end,
    drop column if exists afternoon_dropoff_window_start,
    drop column if exists afternoon_dropoff_window_end,
    drop column if exists estimated_distance_to_school_km,
    drop column if exists estimated_duration_to_school_min;

-- 5. Drop constraint cũ liên quan đến cột đã xóa
alter table school_bus_school_pickup_point
    drop constraint if exists chk_spp_morning_window;
alter table school_bus_school_pickup_point
    drop constraint if exists chk_spp_afternoon_window;
alter table school_bus_school_pickup_point
    drop constraint if exists chk_spp_distance_positive;
alter table school_bus_school_pickup_point
    drop constraint if exists chk_spp_duration_positive;
