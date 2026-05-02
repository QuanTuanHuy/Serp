-- CRM meeting matching (phase 1): extend schema with nullable columns only.
-- Application defaults live on entities; non-null + checks are applied in V1_10 after backfill.

-- ---------------------------------------------------------------------------
-- team_members: rep profile used by compatibility / capacity scoring
-- ---------------------------------------------------------------------------
ALTER TABLE team_members
    ADD COLUMN IF NOT EXISTS skills TEXT,
    ADD COLUMN IF NOT EXISTS languages TEXT,
    ADD COLUMN IF NOT EXISTS experience_level VARCHAR(20),
    ADD COLUMN IF NOT EXISTS capacity INTEGER,
    ADD COLUMN IF NOT EXISTS max_meetings INTEGER;

-- ---------------------------------------------------------------------------
-- accounts: customer preferences + tier for routing / priority
-- ---------------------------------------------------------------------------
ALTER TABLE accounts
    ADD COLUMN IF NOT EXISTS tier VARCHAR(20),
    ADD COLUMN IF NOT EXISTS preferred_time_slots TEXT,
    ADD COLUMN IF NOT EXISTS preferred_days TEXT,
    ADD COLUMN IF NOT EXISTS language VARCHAR(50),
    ADD COLUMN IF NOT EXISTS timezone VARCHAR(100);
