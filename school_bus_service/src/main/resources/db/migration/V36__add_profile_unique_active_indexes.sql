-- ============================================================================
-- V36: Add Profile Unique Active Indexes
-- ============================================================================
-- Enforce that a user can have at most one active profile of each type
-- per tenant. Soft deleted profiles (is_deleted = true) and inactive
-- profiles (is_active = false) are excluded from the uniqueness constraint.
-- ============================================================================

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_parent_tenant_user_active
    ON public.school_bus_parent_profile (tenant_id, user_id)
    WHERE is_deleted = false AND is_active = true;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_driver_tenant_user_active
    ON public.school_bus_driver_profile (tenant_id, user_id)
    WHERE is_deleted = false AND is_active = true;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_attendant_tenant_user_active
    ON public.school_bus_attendant_profile (tenant_id, user_id)
    WHERE is_deleted = false AND is_active = true;
