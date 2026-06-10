-- Author: Nguyen The Anh
-- Description: Part of Serp Project
-- Purpose: Remove unsupported express and special-goods pricing rules from active shipping fee flows.

DELETE FROM tariffs
WHERE service_code = 'HOA_TOC';

DELETE FROM surcharge_rules
WHERE code IN (
    'HANG_GIA_TRI_CAO',
    'CHUNG_TU_QUAN_TRONG',
    'DE_VO',
    'QUA_KHO',
    'CHAT_LONG'
);

DELETE FROM vas_rules
WHERE code = 'BAO_HIEM';
