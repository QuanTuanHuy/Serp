/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

public enum ModuleStatus {
    /**
     * Module đang hoạt động bình thường
     */
    ACTIVE,

    /**
     * Module đang trong giai đoạn beta testing
     */
    BETA,

    /**
     * Module đã lỗi thời, sẽ bị remove trong tương lai
     */
    DEPRECATED,

    /**
     * Module đang maintenance, tạm thời không khả dụng
     */
    MAINTENANCE,

    /**
     * Module đã bị vô hiệu hóa
     */
    DISABLED
}
