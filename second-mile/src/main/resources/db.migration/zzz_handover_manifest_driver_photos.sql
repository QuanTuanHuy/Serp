/*
Author: Nguyen The Anh
Description: Part of Serp Project - Store driver handover check-in photos
*/

ALTER TABLE handover_manifests
    ADD COLUMN IF NOT EXISTS driver_start_photo_url TEXT,
    ADD COLUMN IF NOT EXISTS driver_end_photo_url TEXT;
