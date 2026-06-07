-- ============================================================
-- V14 — Route Planning Workspace schema upgrade
-- Phase 1: planning session, plan student, issue tables;
--           alter route_plan + route_assignment.
-- ============================================================

-- ── 1. school_bus_route_planning_session ────────────────────

create table if not exists school_bus_route_planning_session
(
    id         bigserial primary key,
    tenant_id  bigint not null,

    school_id          bigint not null references school_bus_school(id),
    school_schedule_id bigint not null references school_bus_school_schedule(id),

    service_date    date         not null,
    route_direction varchar(30)  not null,
    planning_method varchar(30)  not null,

    status varchar(30) not null,

    total_eligible_students  integer default 0 not null,
    total_planned_students   integer default 0 not null,
    total_unassigned_students integer default 0 not null,

    total_routes       integer          default 0 not null,
    total_stops        integer          default 0 not null,
    total_distance_km  double precision,
    total_duration_min integer,

    generated_at  timestamp,
    generated_by  bigint,
    published_at  timestamp,
    published_by  bigint,

    constraint_json  text,
    planning_notes   text,

    is_active  boolean default true  not null,
    is_deleted boolean default false not null,
    created_at timestamp default current_timestamp not null,
    created_by varchar(100),
    updated_at timestamp default current_timestamp not null,
    updated_by varchar(100),

    constraint chk_planning_session_direction
        check (route_direction in ('OUTBOUND', 'RETURN')),

    constraint chk_planning_session_method
        check (planning_method in ('MANUAL', 'GREEDY', 'CLONED', 'IMPORTED')),

    constraint chk_planning_session_status
        check (status in ('DRAFT', 'GENERATED', 'REVIEWING', 'PUBLISHED', 'CANCELLED'))
);

create index if not exists idx_planning_session_context
    on school_bus_route_planning_session
        (tenant_id, school_id, school_schedule_id, service_date, route_direction, is_deleted);

create index if not exists idx_planning_session_status
    on school_bus_route_planning_session (tenant_id, status, is_deleted);

-- One active session per context (school+schedule+date+direction).
-- Drop first so re-run is idempotent.
create unique index if not exists uk_planning_session_context_active
    on school_bus_route_planning_session
        (tenant_id, school_id, school_schedule_id, service_date, route_direction)
    where is_deleted = false and status <> 'CANCELLED';

-- ── 2. Alter school_bus_route_plan ──────────────────────────

alter table school_bus_route_plan
    add column if not exists planning_session_id  bigint
        references school_bus_route_planning_session(id),
    add column if not exists school_schedule_id   bigint
        references school_bus_school_schedule(id),
    add column if not exists required_capacity    integer,
    add column if not exists quality_score        double precision,
    add column if not exists issue_count          integer default 0 not null,
    add column if not exists blocking_issue_count integer default 0 not null,
    add column if not exists published_at         timestamp,
    add column if not exists published_by         bigint;

-- Extend status check to cover the full lifecycle.
-- Drop old constraint first (name may vary) then re-add.
alter table school_bus_route_plan
    drop constraint if exists chk_route_plan_status;

alter table school_bus_route_plan
    add constraint chk_route_plan_status
        check (status in ('DRAFT', 'GENERATED', 'REVIEWING', 'PUBLISHED', 'ASSIGNED', 'TRIP_CREATED', 'CANCELLED'));

-- Index to quickly fetch routes for a session
create index if not exists idx_route_plan_session
    on school_bus_route_plan (planning_session_id, is_deleted)
    where planning_session_id is not null;

-- ── 3. school_bus_route_plan_student ────────────────────────

create table if not exists school_bus_route_plan_student
(
    id         bigserial primary key,
    tenant_id  bigint not null,

    route_id      bigint not null references school_bus_route_plan(id),
    route_stop_id bigint          references school_bus_route_stop(id),

    student_id      bigint not null references school_bus_student(id),
    subscription_id bigint not null references school_bus_student_subscription(id),

    service_action varchar(30) not null,
    planned_time   time,

    is_active  boolean default true  not null,
    is_deleted boolean default false not null,
    created_at timestamp default current_timestamp not null,
    created_by varchar(100),
    updated_at timestamp default current_timestamp not null,
    updated_by varchar(100),

    constraint chk_route_plan_student_action
        check (service_action in ('BOARD', 'DROPOFF'))
);

create index if not exists idx_route_plan_student_route
    on school_bus_route_plan_student (route_id, is_deleted);

create index if not exists idx_route_plan_student_stop
    on school_bus_route_plan_student (route_stop_id, is_deleted)
    where route_stop_id is not null;

create unique index if not exists uk_route_plan_student_action
    on school_bus_route_plan_student (route_id, student_id, subscription_id, service_action)
    where is_deleted = false;

-- ── 4. school_bus_route_planning_issue ──────────────────────

create table if not exists school_bus_route_planning_issue
(
    id         bigserial primary key,
    tenant_id  bigint not null,

    planning_session_id bigint references school_bus_route_planning_session(id),
    route_id            bigint references school_bus_route_plan(id),
    route_stop_id       bigint references school_bus_route_stop(id),
    student_id          bigint references school_bus_student(id),
    subscription_id     bigint references school_bus_student_subscription(id),

    issue_type  varchar(50)  not null,
    severity    varchar(20)  not null,
    message     text         not null,
    is_resolved boolean default false not null,

    is_active  boolean default true  not null,
    is_deleted boolean default false not null,
    created_at timestamp default current_timestamp not null,
    created_by varchar(100),
    updated_at timestamp default current_timestamp not null,
    updated_by varchar(100),

    constraint chk_planning_issue_severity
        check (severity in ('INFO', 'WARNING', 'BLOCKING'))
);

create index if not exists idx_planning_issue_session
    on school_bus_route_planning_issue (planning_session_id, severity, is_deleted)
    where planning_session_id is not null;

create index if not exists idx_planning_issue_route
    on school_bus_route_planning_issue (route_id, severity, is_deleted)
    where route_id is not null;

-- ── 5. Alter school_bus_route_assignment ────────────────────

alter table school_bus_route_assignment
    add column if not exists status          varchar(30) default 'ASSIGNED' not null,
    add column if not exists assigned_by     bigint,
    add column if not exists confirmed_at    timestamp,
    add column if not exists cancelled_at    timestamp,
    add column if not exists assignment_note text;

alter table school_bus_route_assignment
    drop constraint if exists chk_route_assignment_status;

alter table school_bus_route_assignment
    add constraint chk_route_assignment_status
        check (status in ('ASSIGNED', 'CONFIRMED', 'CANCELLED', 'REPLACED'));

-- One active assignment per route
create unique index if not exists uk_route_assignment_active
    on school_bus_route_assignment (tenant_id, route_id)
    where is_deleted = false and status in ('ASSIGNED', 'CONFIRMED');
