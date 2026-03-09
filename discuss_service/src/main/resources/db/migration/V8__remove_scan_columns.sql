-- V8: Remove virus scan columns from attachments table
-- Since virus scanning is not implemented, these columns are removed to simplify the attachment flow.
-- Files can now be downloaded immediately after upload.

ALTER TABLE attachments DROP COLUMN IF EXISTS scan_status;
ALTER TABLE attachments DROP COLUMN IF EXISTS scanned_at;
