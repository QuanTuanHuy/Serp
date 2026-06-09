ALTER TABLE public.school_bus_route_plan
    ADD COLUMN IF NOT EXISTS selected_bus_id bigint;

CREATE INDEX IF NOT EXISTS idx_route_plan_selected_bus
    ON public.school_bus_route_plan (selected_bus_id)
    WHERE selected_bus_id IS NOT NULL;
