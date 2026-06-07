CREATE TABLE public.school_bus_sync_checkpoint
(
    id                   bigserial PRIMARY KEY,
    sync_code            varchar(100) NOT NULL,
    last_success_sync_at timestamp,
    last_attempt_sync_at timestamp,
    last_status          varchar(50),
    last_error_message   text,
    last_synced_count    integer DEFAULT 0 NOT NULL,
    is_active            boolean DEFAULT true NOT NULL,
    is_deleted           boolean DEFAULT false NOT NULL,
    created_at           timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by           varchar(100),
    updated_at           timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by           varchar(100)
);

CREATE UNIQUE INDEX uk_school_bus_sync_checkpoint_code
    ON public.school_bus_sync_checkpoint(sync_code)
    WHERE is_deleted = false;
