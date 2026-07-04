-- Allow src_location_code to be nullable for OE requests
ALTER TABLE requests
    ALTER COLUMN src_location_code DROP NOT NULL;
