-- Author: Nguyen The Anh
-- Description: Remove deprecated MAINTENANCE status from hubs

UPDATE hubs
SET status = 'INACTIVE'
WHERE status = 'MAINTENANCE';
