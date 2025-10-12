/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

public enum PermissionType {
    /**
     * Data access permission (CRUD operations on entities)
     * Ví dụ: customer.create, invoice.read
     */
    DATA,

    /**
     * UI access permission (view menus, buttons, pages)
     * Ví dụ: Xem được Settings menu
     */
    UI,

    /**
     * API access permission (call specific endpoints)
     * Ví dụ: Gọi được /api/v1/reports
     */
    API,

    /**
     * Feature access permission (access to specific features)
     * Ví dụ: Export to Excel, Import from CSV
     */
    FEATURE
}
