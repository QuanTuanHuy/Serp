-- Meeting matching (phase 2): backfill nullable columns, then NOT NULL + CHECK constraints.
-- Depends on: V1_8 (columns exist), V1_9 (optional; independent of this script).

-- ---------------------------------------------------------------------------
-- team_members: defaults then integrity constraints
-- ---------------------------------------------------------------------------
UPDATE team_members SET experience_level = 'MID' WHERE experience_level IS NULL;
UPDATE team_members SET capacity = 100 WHERE capacity IS NULL;
UPDATE team_members SET max_meetings = 8 WHERE max_meetings IS NULL;

ALTER TABLE team_members
    ALTER COLUMN experience_level SET NOT NULL,
    ALTER COLUMN experience_level SET DEFAULT 'MID',
    ALTER COLUMN capacity SET NOT NULL,
    ALTER COLUMN capacity SET DEFAULT 100,
    ALTER COLUMN max_meetings SET NOT NULL,
    ALTER COLUMN max_meetings SET DEFAULT 8;

ALTER TABLE team_members
    DROP CONSTRAINT IF EXISTS chk_team_members_capacity,
    DROP CONSTRAINT IF EXISTS chk_team_members_max_meetings,
    DROP CONSTRAINT IF EXISTS chk_team_members_experience_level;

ALTER TABLE team_members
    ADD CONSTRAINT chk_team_members_capacity CHECK (capacity BETWEEN 1 AND 100),
    ADD CONSTRAINT chk_team_members_max_meetings CHECK (max_meetings > 0),
    ADD CONSTRAINT chk_team_members_experience_level CHECK (
        experience_level IN ('JUNIOR', 'MID', 'SENIOR', 'EXPERT')
    );

-- ---------------------------------------------------------------------------
-- accounts: tier + timezone defaults then integrity constraints
-- ---------------------------------------------------------------------------
UPDATE accounts SET tier = 'STANDARD' WHERE tier IS NULL;
UPDATE accounts SET timezone = 'Asia/Ho_Chi_Minh' WHERE timezone IS NULL;

ALTER TABLE accounts
    ALTER COLUMN tier SET NOT NULL,
    ALTER COLUMN tier SET DEFAULT 'STANDARD',
    ALTER COLUMN timezone SET NOT NULL,
    ALTER COLUMN timezone SET DEFAULT 'Asia/Ho_Chi_Minh';

ALTER TABLE accounts
    DROP CONSTRAINT IF EXISTS chk_accounts_tier;

ALTER TABLE accounts
    ADD CONSTRAINT chk_accounts_tier CHECK (
        tier IN ('STANDARD', 'SILVER', 'GOLD', 'PLATINUM')
    );
