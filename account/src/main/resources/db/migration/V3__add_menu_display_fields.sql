-- Author: QuanTuanHuy
-- Description: Part of Serp Project
-- Purpose: Add menu_type, is_visible, description columns to menu_displays

ALTER TABLE menu_displays
    ADD COLUMN menu_type VARCHAR(50) NOT NULL DEFAULT 'SIDEBAR',
    ADD COLUMN is_visible BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN description VARCHAR(500);

ALTER TABLE menu_displays
    ADD CONSTRAINT chk_menu_displays_menu_type
    CHECK (menu_type IN ('SIDEBAR','TOPBAR','DROPDOWN','ACTION'));

--     ALTER TABLE menu_displays ALTER COLUMN menu_type DROP DEFAULT;
--     ALTER TABLE menu_displays ALTER COLUMN is_visible DROP DEFAULT;
