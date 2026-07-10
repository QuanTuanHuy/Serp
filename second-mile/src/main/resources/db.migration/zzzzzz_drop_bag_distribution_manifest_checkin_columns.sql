-- Author: Nguyen The Anh
-- Description: Remove deprecated bag distribution manifest check-in columns

ALTER TABLE bag_distribution_manifests
    DROP COLUMN IF EXISTS driver_start_latitude,
    DROP COLUMN IF EXISTS driver_start_longitude,
    DROP COLUMN IF EXISTS driver_start_distance_m,
    DROP COLUMN IF EXISTS driver_start_photo_url,
    DROP COLUMN IF EXISTS driver_end_latitude,
    DROP COLUMN IF EXISTS driver_end_longitude,
    DROP COLUMN IF EXISTS driver_end_distance_m,
    DROP COLUMN IF EXISTS driver_end_photo_url;
