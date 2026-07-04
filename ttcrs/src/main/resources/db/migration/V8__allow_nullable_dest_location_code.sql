-- Allow dest_location_code to be nullable for IE requests
ALTER TABLE requests
    ALTER COLUMN dest_location_code DROP NOT NULL;
