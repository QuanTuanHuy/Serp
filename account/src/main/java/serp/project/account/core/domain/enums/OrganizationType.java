/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

public enum OrganizationType {
    /**
     * Doanh nghiệp lớn (>500 employees)
     */
    ENTERPRISE,

    /**
     * Doanh nghiệp vừa và nhỏ (10-500 employees)
     */
    SMB,

    /**
     * Công ty khởi nghiệp (<10 employees)
     */
    STARTUP,

    /**
     * Sử dụng cá nhân
     */
    PERSONAL,

    /**
     * Tổ chức phi lợi nhuận
     */
    NON_PROFIT,

    /**
     * Cơ quan chính phủ
     */
    GOVERNMENT
}
