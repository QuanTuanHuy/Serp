/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Quản lý quyền truy cập của user vào module cụ thể trong organization
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class UserModuleAccessEntity extends BaseEntity {

    private Long userId;

    private Long moduleId;

    private Long organizationId;

    private Boolean isActive;

    private Long grantedBy;

    private Long grantedAt;

    /**
     * Thời hạn của quyền truy cập (optional)
     * null = vĩnh viễn
     */
    private Long expiresAt;

    private String description;

    @JsonIgnore
    public boolean isActiveAccess() {
        return Boolean.TRUE.equals(this.isActive) && !isExpired();
    }

    @JsonIgnore
    public boolean isExpired() {
        if (this.expiresAt == null) {
            return false;
        }
        return System.currentTimeMillis() > this.expiresAt;
    }

    @JsonIgnore
    public void activate(Long grantedByUserId) {
        this.isActive = true;
        this.grantedBy = grantedByUserId;
        this.grantedAt = System.currentTimeMillis();
    }

    @JsonIgnore
    public void deactivate() {
        this.isActive = false;
    }

    @JsonIgnore
    public void setExpiration(Long expirationTimestamp) {
        this.expiresAt = expirationTimestamp;
    }

    @JsonIgnore
    public boolean isPermanent() {
        return this.expiresAt == null;
    }
}
