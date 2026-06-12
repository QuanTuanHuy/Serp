-- Author: Nguyen The Anh
-- Description: Part of Serp Project
-- Purpose: Normalize legacy express orders after express service removal.

UPDATE orders
SET order_type = 'STANDARD_ORDER'
WHERE order_type = 'EXPRESS_ORDER';
