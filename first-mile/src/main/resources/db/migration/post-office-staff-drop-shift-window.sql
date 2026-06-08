-- Author: Nguyen The Anh
-- Description: Remove courier assignment shift window fields from post office staff assignments.

ALTER TABLE post_office_staff_assignments
    DROP CONSTRAINT IF EXISTS ck_assignment_shift_time;

ALTER TABLE post_office_staff_assignments
    DROP COLUMN IF EXISTS shift_start_time,
    DROP COLUMN IF EXISTS shift_end_time;
