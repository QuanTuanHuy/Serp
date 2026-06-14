-- V35: Remove driver license fields from database
ALTER TABLE public.school_bus_driver_profile
    DROP COLUMN IF EXISTS license_number,
    DROP COLUMN IF EXISTS license_class,
    DROP COLUMN IF EXISTS license_expiry_date;
