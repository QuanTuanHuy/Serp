-- ============================================================================
-- V37: Update Profile Unique Indexes
-- ============================================================================
-- Enforce that a user can have at most one profile of each type (active or inactive)
-- per tenant. Soft deleted profiles (is_deleted = true) are excluded,
-- but inactive profiles are included so we cannot have duplicate user profiles.
-- ============================================================================

DROP INDEX IF EXISTS public.uk_school_bus_parent_tenant_user_active;
DROP INDEX IF EXISTS public.uk_school_bus_driver_tenant_user_active;
DROP INDEX IF EXISTS public.uk_school_bus_attendant_tenant_user_active;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_parent_tenant_user_not_deleted
    ON public.school_bus_parent_profile (tenant_id, user_id)
    WHERE is_deleted = false;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_driver_tenant_user_not_deleted
    ON public.school_bus_driver_profile (tenant_id, user_id)
    WHERE is_deleted = false;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_attendant_tenant_user_not_deleted
    ON public.school_bus_attendant_profile (tenant_id, user_id)
    WHERE is_deleted = false;
