/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.account.core.domain.enums.LicenseStatus;
import serp.project.account.core.domain.enums.LicenseType;

/**
 * Quản lý license/subscription của organization cho từng module
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class ModuleLicenseEntity extends BaseEntity {

    private Long organizationId;

    private Long moduleId;

    private LicenseType licenseType;

    /**
     * Số user tối đa có thể sử dụng module
     * null hoặc -1 = unlimited
     */
    private Integer maxUsers;

    /**
     * Số user hiện tại đang sử dụng module
     */
    private Integer currentUsers;

    private Long startDate;

    /**
     * Ngày hết hạn license
     * null = vĩnh viễn (perpetual license)
     */
    private Long expiresAt;

    private LicenseStatus status;

    /**
     * License key (if applicable)
     */
    private String licenseKey;

    private String notes;

    public boolean isExpired() {
        if (expiresAt == null)
            return false;
        long now = System.currentTimeMillis();
        return now > expiresAt;
    }

    public boolean isActive() {
        return status == LicenseStatus.ACTIVE && !isExpired();
    }

    public boolean canAddMoreUsers() {
        if (maxUsers == null || maxUsers == -1)
            return true;
        return currentUsers < maxUsers;
    }

    public void incrementUserCount() {
        if (maxUsers == null || maxUsers == -1)
            return;
        if (currentUsers == null)
            currentUsers = 0;
        if (currentUsers < maxUsers)
            currentUsers++;
    }
}
