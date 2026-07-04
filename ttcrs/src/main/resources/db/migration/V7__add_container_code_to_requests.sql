-- Add container_code column to requests table (nullable, no FK constraint)
ALTER TABLE requests
    ADD COLUMN IF NOT EXISTS container_code VARCHAR(50);
