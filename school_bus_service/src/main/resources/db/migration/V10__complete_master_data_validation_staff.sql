-- V10: Add driver license fields for complete master data validation
ALTER TABLE school_bus_driver_profile
    ADD COLUMN IF NOT EXISTS license_class VARCHAR(50),
    ADD COLUMN IF NOT EXISTS license_expiry_date DATE;
