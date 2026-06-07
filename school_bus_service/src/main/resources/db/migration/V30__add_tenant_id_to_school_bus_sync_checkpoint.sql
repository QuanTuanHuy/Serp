ALTER TABLE public.school_bus_sync_checkpoint
    ADD COLUMN tenant_id bigint DEFAULT 0 NOT NULL;
