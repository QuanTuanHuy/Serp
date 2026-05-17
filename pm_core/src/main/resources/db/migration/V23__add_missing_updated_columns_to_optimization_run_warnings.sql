ALTER TABLE optimization_run_warnings
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

ALTER TABLE optimization_run_warnings
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;
