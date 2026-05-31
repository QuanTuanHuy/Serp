ALTER TABLE optimization_runs
    ADD COLUMN objective VARCHAR(50) NOT NULL DEFAULT 'BALANCED_WORKLOAD',
    ADD COLUMN change_scope VARCHAR(50) NOT NULL DEFAULT 'ASSIGNMENT_AND_SCHEDULE';

UPDATE optimization_runs
SET objective = CASE
        WHEN mode = 'MINIMAL_REASSIGNMENT' THEN 'MINIMAL_REASSIGNMENT'
        ELSE 'BALANCED_WORKLOAD'
    END,
    change_scope = CASE
        WHEN mode = 'ASSIGNMENT_ONLY' THEN 'ASSIGNMENT_ONLY'
        WHEN mode = 'SCHEDULE_ONLY' THEN 'SCHEDULE_ONLY'
        WHEN allow_reassignment = TRUE AND allow_schedule_changes = FALSE THEN 'ASSIGNMENT_ONLY'
        WHEN allow_reassignment = FALSE AND allow_schedule_changes = TRUE THEN 'SCHEDULE_ONLY'
        ELSE 'ASSIGNMENT_AND_SCHEDULE'
    END;

ALTER TABLE optimization_runs
    DROP COLUMN mode,
    DROP COLUMN allow_reassignment,
    DROP COLUMN allow_schedule_changes;
