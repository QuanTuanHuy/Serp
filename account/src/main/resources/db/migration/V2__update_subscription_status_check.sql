/*
Author: QuanTuanHuy
Description: Migration to update organization_subscriptions status check to include PENDING_UPGRADE
*/

ALTER TABLE organization_subscriptions
    DROP CONSTRAINT IF EXISTS organization_subscriptions_status_check;

ALTER TABLE organization_subscriptions
    ADD CONSTRAINT organization_subscriptions_status_check
        CHECK (status IN (
            'ACTIVE',
            'TRIAL',
            'EXPIRED',
            'CANCELLED',
            'PAYMENT_FAILED',
            'GRACE_PERIOD',
            'PENDING',
            'PENDING_UPGRADE'
        ));
