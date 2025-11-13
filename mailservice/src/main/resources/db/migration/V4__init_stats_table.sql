/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

-- ======================================
-- V4: Create email_stats table
-- ======================================

CREATE TABLE IF NOT EXISTS email_stats (
    id BIGSERIAL PRIMARY KEY,
    
    -- Aggregation dimensions
    tenant_id BIGINT,
    provider VARCHAR(50) NOT NULL,
    email_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    
    -- Time dimension
    stat_date DATE NOT NULL,
    stat_hour INTEGER,
    
    -- Metrics
    total_count INTEGER DEFAULT 0,
    success_count INTEGER DEFAULT 0,
    failed_count INTEGER DEFAULT 0,
    retry_count INTEGER DEFAULT 0,
    
    -- Performance metrics
    avg_response_time_ms INTEGER,
    min_response_time_ms INTEGER,
    max_response_time_ms INTEGER,
    
    -- Additional aggregations
    total_size_bytes BIGINT DEFAULT 0,
    attachment_count INTEGER DEFAULT 0,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_stats_provider CHECK (provider IN ('JAVA_MAIL', 'BREVO', 'ALL')),
    -- CONSTRAINT chk_stats_email_type CHECK (email_type IN ('VERIFICATION', 'NOTIFICATION', 'MARKETING', 'TRANSACTIONAL', 'PASSWORD_RESET', 'ALERT', 'REMINDER', 'WELCOME', 'ALL')),
    CONSTRAINT chk_stats_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'RETRY', 'CANCELLED', 'ALL')),
    CONSTRAINT chk_stats_hour CHECK (stat_hour IS NULL OR (stat_hour >= 0 AND stat_hour <= 23)),
    CONSTRAINT uq_email_stats_dimensions UNIQUE (tenant_id, provider, email_type, status, stat_date, stat_hour)
);

-- Indexes for fast aggregation queries
CREATE INDEX idx_email_stats_tenant_id ON email_stats(tenant_id);
CREATE INDEX idx_email_stats_stat_date ON email_stats(stat_date DESC);
CREATE INDEX idx_email_stats_provider ON email_stats(provider);
CREATE INDEX idx_email_stats_email_type ON email_stats(email_type);
CREATE INDEX idx_email_stats_status ON email_stats(status);

-- Composite indexes for common queries
CREATE INDEX idx_email_stats_tenant_date ON email_stats(tenant_id, stat_date DESC);
CREATE INDEX idx_email_stats_provider_date ON email_stats(provider, stat_date DESC);
CREATE INDEX idx_email_stats_date_hour ON email_stats(stat_date DESC, stat_hour DESC);

-- Comments
COMMENT ON TABLE email_stats IS 'Pre-aggregated email statistics for admin dashboard';
COMMENT ON COLUMN email_stats.stat_date IS 'Date of the statistics (for daily aggregation)';
COMMENT ON COLUMN email_stats.stat_hour IS 'Hour of the day (0-23) for hourly aggregation, NULL for daily';
COMMENT ON COLUMN email_stats.total_count IS 'Total number of emails in this dimension';
COMMENT ON COLUMN email_stats.success_count IS 'Number of successfully sent emails';
COMMENT ON COLUMN email_stats.avg_response_time_ms IS 'Average response time from provider in milliseconds';
