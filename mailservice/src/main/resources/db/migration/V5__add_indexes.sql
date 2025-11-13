-- /*
-- Author: QuanTuanHuy
-- Description: Part of Serp Project
-- */

-- -- ======================================
-- -- V5: Add additional indexes and optimizations
-- -- ======================================

-- -- ==================== Performance Optimizations ====================

-- -- Partial index for pending emails (frequently queried)
-- CREATE INDEX idx_emails_pending ON emails(created_at DESC) WHERE status = 'PENDING';

-- -- Partial index for failed emails to retry
-- CREATE INDEX idx_emails_failed_retry ON emails(next_retry_at, retry_count) 
--     WHERE status = 'RETRY' AND retry_count < max_retries;

-- -- Index for email history queries by tenant and date range
-- CREATE INDEX idx_emails_tenant_date_range ON emails(tenant_id, created_at DESC, status);

-- -- Index for provider performance monitoring
-- CREATE INDEX idx_emails_provider_status_date ON emails(provider, status, sent_at DESC);

-- -- ==================== Email Templates Optimizations ====================

-- -- Index for template lookup by code and tenant
-- CREATE INDEX idx_email_templates_lookup ON email_templates(code, tenant_id, is_active);

-- -- ==================== Email Attachments Optimizations ====================

-- -- Index for cleanup of expired attachments
-- CREATE INDEX idx_attachments_cleanup ON email_attachments(expires_at, active_status) 
--     WHERE expires_at IS NOT NULL AND active_status = 'ACTIVE';

-- -- ==================== Email Stats Optimizations ====================

-- -- Index for dashboard time-series queries
-- CREATE INDEX idx_stats_dashboard_timeseries ON email_stats(stat_date DESC, stat_hour DESC, provider, status);

-- -- Index for tenant analytics
-- CREATE INDEX idx_stats_tenant_analytics ON email_stats(tenant_id, stat_date DESC, provider, email_type);

-- -- ==================== Add Foreign Key Constraints ====================

-- -- Add FK from emails to email_templates (optional, allows NULL)
-- ALTER TABLE emails 
--     ADD CONSTRAINT fk_emails_template 
--     FOREIGN KEY (template_id) 
--     REFERENCES email_templates(id) 
--     ON DELETE SET NULL;

-- -- ==================== Create Function for Auto-Update Timestamp ====================

-- CREATE OR REPLACE FUNCTION update_updated_at_column()
-- RETURNS TRIGGER AS $$
-- BEGIN
--     NEW.updated_at = CURRENT_TIMESTAMP;
--     RETURN NEW;
-- END;
-- $$ language 'plpgsql';

-- -- ==================== Create Triggers ====================

-- -- Trigger for emails table
-- CREATE TRIGGER update_emails_updated_at 
--     BEFORE UPDATE ON emails 
--     FOR EACH ROW 
--     EXECUTE FUNCTION update_updated_at_column();

-- -- Trigger for email_templates table
-- CREATE TRIGGER update_email_templates_updated_at 
--     BEFORE UPDATE ON email_templates 
--     FOR EACH ROW 
--     EXECUTE FUNCTION update_updated_at_column();

-- -- Trigger for email_attachments table
-- CREATE TRIGGER update_email_attachments_updated_at 
--     BEFORE UPDATE ON email_attachments 
--     FOR EACH ROW 
--     EXECUTE FUNCTION update_updated_at_column();

-- -- Trigger for email_stats table
-- CREATE TRIGGER update_email_stats_updated_at 
--     BEFORE UPDATE ON email_stats 
--     FOR EACH ROW 
--     EXECUTE FUNCTION update_updated_at_column();

-- -- ==================== Comments ====================

-- COMMENT ON INDEX idx_emails_pending IS 'Optimizes queries for pending emails';
-- COMMENT ON INDEX idx_emails_failed_retry IS 'Optimizes retry scheduler queries';
-- COMMENT ON INDEX idx_attachments_cleanup IS 'Optimizes cleanup job for expired attachments';
-- COMMENT ON FUNCTION update_updated_at_column() IS 'Auto-updates updated_at timestamp on row modification';
