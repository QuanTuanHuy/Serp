ALTER TABLE optimization_runs
    ADD COLUMN algorithm_key VARCHAR(100) NOT NULL DEFAULT 'greedy-balanced',
    ADD COLUMN algorithm_version VARCHAR(50) NOT NULL DEFAULT 'v1',
    ADD COLUMN solver_status VARCHAR(50) NOT NULL DEFAULT 'FEASIBLE',
    ADD COLUMN objective_score NUMERIC(18, 6);
